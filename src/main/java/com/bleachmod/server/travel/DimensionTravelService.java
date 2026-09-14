package com.bleachmod.server.travel;

import com.bleachmod.BleachMod;
import com.bleachmod.Reference;
import com.bleachmod.common.data.*;
import com.bleachmod.common.network.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** All mutations run on the server thread. No static player state survives server shutdown. */
public final class DimensionTravelService {
    public static final String ENTRY = "bleachmod:soul_society_entry", RETURN = "bleachmod:return";
    private static final Map<MinecraftServer, DimensionTravelService> SERVERS = new IdentityHashMap<>();
    private static final TicketType<UUID> TICKET = TicketType.create("bleach_travel", Comparator.comparing(UUID::toString), 240);
    private final MinecraftServer server;
    private TravelConfig config = new TravelConfig(false, 20, 100, null);
    private SoulSocietyMapInstaller.InstalledMap map;
    private int revision = 1;
    private final Map<UUID, Long> queries = new HashMap<>(), requests = new HashMap<>();
    private final Map<UUID, Integer> lastIds = new HashMap<>();
    private final Map<UUID, Pending> pending = new HashMap<>();
    private final Set<UUID> committing = new HashSet<>();

    private static final class Pending {
        final ServerPlayer player;
        final int id, revision;
        final String target;
        final TravelData.Location origin;
        final boolean rescue;
        final long started;
        ServerLevel level;
        BlockPos center;
        float yaw;
        boolean fallback;
        final List<ChunkPos> chunks = new ArrayList<>();
        CompletableFuture<?> ready;
        Pending(ServerPlayer player, TravelPackets.Request request, boolean rescue, long started) {
            this.player=player; id=request.requestId(); revision=request.revision(); target=request.target();
            this.rescue=rescue; this.started=started;
            origin=new TravelData.Location(player.level().dimension().location().toString(), player.getX(), Math.max(-64, Math.min(319, player.getY())), player.getZ(), player.getYRot(), player.getXRot());
        }
    }
    private DimensionTravelService(MinecraftServer server) { this.server=server; }
    public static DimensionTravelService get(MinecraftServer server) { return SERVERS.computeIfAbsent(server, DimensionTravelService::new); }
    public static void stop(MinecraftServer server) {
        var service=SERVERS.remove(server);
        if(service!=null) { service.pending.values().forEach(service::release); service.pending.clear(); }
    }
    public void setMap(SoulSocietyMapInstaller.InstalledMap map) { this.map=map; }
    public void reload() throws IOException {
        TravelConfig prepared=TravelConfig.load(server);
        config=prepared; revision++;
    }
    public String describe() { return "Soul Society: map="+(map==null?"missing":map.sha256())+", enabled="+config.enabled()+", pending="+pending.size()+", revision="+revision; }
    public int revision() { return revision; }
    private long now() { return server.overworld().getGameTime(); }
    private TravelData.Location arrival() { return config.arrivalOverride()!=null?config.arrivalOverride():map==null?null:map.arrival(); }
    private String reason(ServerPlayer player, String target, boolean rescue) {
        if (!ENTRY.equals(target) && !RETURN.equals(target)) return "unknown_target";
        if (!player.isAlive() || player.isRemoved() || player.isSleeping() || player.isPassenger() || player.isVehicle() || (!rescue && player.isSpectator())) return "invalid_state";
        var data=PlayerData.get(player).orElse(null);
        if(data==null) return "not_allowed";
        if(rescue) return "";
        if(!data.getStatus().hasCreatedCharacter() || !Reference.RACE_SHINIGAMI.equals(data.getCharacter().getRace())) return "not_allowed";
        if(ENTRY.equals(target)) {
            if(!config.enabled()) return "disabled";
            if(!player.level().dimension().equals(Level.OVERWORLD)) return "not_allowed";
            if(map==null || arrival()==null) return "map_not_ready";
            if(server.getLevel(SoulSociety.LEVEL)==null) return "destination_unavailable";
            if(data.getResources().getCurrentReiatsu()<config.entryCost()) return "insufficient_reiatsu";
        } else if(!player.level().dimension().equals(SoulSociety.LEVEL)) return "not_allowed";
        if(data.getTravel().remaining(now())>0) return "cooldown";
        return "";
    }
    public void query(ServerPlayer player) {
        if(!allow(queries,player.getUUID(),10)) return;
        String target=player.level().dimension().equals(SoulSociety.LEVEL)?RETURN:ENTRY;
        int cooldown=PlayerData.get(player).map(d->d.getTravel().remaining(now())).orElse(0);
        NetworkHandler.sendToPlayer(new TravelPackets.Catalog(revision,target,ENTRY.equals(target)?config.entryCost():0,cooldown,
            pending.containsKey(player.getUUID())?"busy":reason(player,target,false)),player);
    }
    private boolean allow(Map<UUID,Long> times, UUID player, int ticks) {
        long time=now(); Long last=times.get(player);
        if(last!=null && time-last<ticks) return false;
        times.put(player,time); return true;
    }
    public void request(ServerPlayer player, TravelPackets.Request request) { begin(player,request,false); }
    public void rescue(ServerPlayer player) { begin(player,new TravelPackets.Request(-1,RETURN,revision),true); }
    private void begin(ServerPlayer player, TravelPackets.Request request, boolean rescue) {
        if(!allow(requests,player.getUUID(),10)) return;
        if(pending.containsKey(player.getUUID())) { reply(player,request.requestId(),"busy"); return; }
        if(!rescue && Objects.equals(lastIds.get(player.getUUID()),request.requestId())) { reply(player,request.requestId(),"duplicate"); return; }
        lastIds.put(player.getUUID(),request.requestId());
        String failure=request.revision()!=revision?"stale_catalog":reason(player,request.target(),rescue);
        if(!failure.isEmpty()) { reply(player,request.requestId(),failure); return; }
        if(pending.size()>=8) { reply(player,request.requestId(),"busy"); return; }
        Pending job=new Pending(player,request,rescue,now());
        pending.put(player.getUUID(),job);
        try {
            if(ENTRY.equals(request.target())) {
                var destination=arrival(); job.level=server.getLevel(SoulSociety.LEVEL);
                job.center=BlockPos.containing(destination.x(),destination.y(),destination.z()); job.yaw=destination.yaw();
            } else {
                job.level=server.overworld();
                var back=PlayerData.get(player).orElseThrow().getTravel().getReturnLocation();
                if(!rescue && back!=null && back.dimension().equals(Level.OVERWORLD.location().toString())) {
                    job.center=BlockPos.containing(back.x(),back.y(),back.z()); job.yaw=back.yaw();
                } else setFallback(job);
            }
            prepare(job); reply(player,job.id,"preparing");
        } catch(RuntimeException e) { fail(job,"internal_error",e); }
    }
    private void setFallback(Pending job) { job.fallback=true; job.center=server.overworld().getSharedSpawnPos(); job.yaw=server.overworld().getSharedSpawnAngle(); }
    private void prepare(Pending job) {
        List<CompletableFuture<?>> futures=new ArrayList<>();
        for(int x=(job.center.getX()-5)>>4;x<=(job.center.getX()+5)>>4;x++) for(int z=(job.center.getZ()-5)>>4;z<=(job.center.getZ()+5)>>4;z++) {
            ChunkPos chunk=new ChunkPos(x,z); job.chunks.add(chunk);
            job.level.getChunkSource().addRegionTicket(TICKET,chunk,2,job.player.getUUID());
            futures.add(job.level.getChunkSource().getChunkFuture(x,z,ChunkStatus.FULL,true));
        }
        job.ready=CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }
    public void tick() {
        for(Pending job:List.copyOf(pending.values())) {
            if(job.player.isRemoved() || server.getPlayerList().getPlayer(job.player.getUUID())!=job.player) { cancel(job.player); continue; }
            if(now()-job.started>200) { fail(job,"timeout",null); continue; }
            if(!job.player.level().dimension().location().toString().equals(job.origin.dimension())) { fail(job,"travel_cancelled",null); continue; }
            if(job.ready==null || !job.ready.isDone()) continue;
            try {
                if(job.ready.isCompletedExceptionally()) { fail(job,"destination_unavailable",null); continue; }
                String failure=job.revision!=revision?"stale_catalog":reason(job.player,job.target,job.rescue);
                if(!failure.isEmpty()) { fail(job,failure,null); continue; }
                var safe=SafeLandingService.find(job.level,job.player,job.center);
                if(safe.isEmpty() && RETURN.equals(job.target) && !job.fallback) {
                    release(job); setFallback(job); prepare(job); continue;
                }
                if(safe.isEmpty()) { fail(job,"unsafe_landing",null); continue; }
                commit(job,safe.get());
            } catch(RuntimeException e) { fail(job,"internal_error",e); }
        }
    }
    private void commit(Pending job, Vec3 feet) {
        ServerPlayer player=job.player;
        var data=PlayerData.get(player).orElseThrow();
        float cost=ENTRY.equals(job.target)&&!job.rescue?config.entryCost():0;
        // Reserve after safe landing is known. A cancelled/failed teleport refunds this debit.
        // This also prevents Forge callbacks during teleport from spending the same balance.
        if(!data.getResources().consumeReiatsu(cost)) { fail(job,"insufficient_reiatsu",null); return; }
        committing.add(player.getUUID());
        try {
            try { player.teleportTo(job.level,feet.x,feet.y,feet.z,job.yaw,0); }
            catch(RuntimeException e) { BleachMod.LOGGER.error("Travel {} teleport callback failed",job.id,e); }
            if(player.serverLevel()!=job.level || player.position().distanceToSqr(feet)>0.01) {
                data.getResources().addReiatsu(cost); SyncHelper.resources(player);
                fail(job,"travel_cancelled",null); return;
            }
            if(ENTRY.equals(job.target)) data.getTravel().setReturnLocation(job.origin);
            data.getTravel().setCooldown(now(),config.cooldownTicks());
            data.resetTransientState(); player.fallDistance=0; player.setDeltaMovement(Vec3.ZERO);
            SyncHelper.full(player);
            reply(player,job.id,"success");
            BleachMod.LOGGER.info("Travel {}: {} -> {} ({} ticks)",player.getUUID(),job.origin.dimension(),job.target,now()-job.started);
            release(job); pending.remove(player.getUUID());
        } finally { committing.remove(player.getUUID()); }
    }
    public boolean isCommitting(ServerPlayer player) { return committing.contains(player.getUUID()); }
    public void cancel(ServerPlayer player) {
        Pending job=pending.remove(player.getUUID()); if(job!=null) release(job);
    }
    public void logout(ServerPlayer player) {
        cancel(player); queries.remove(player.getUUID()); requests.remove(player.getUUID()); lastIds.remove(player.getUUID());
    }
    private void release(Pending job) {
        if(job.level!=null) for(ChunkPos chunk:job.chunks) job.level.getChunkSource().removeRegionTicket(TICKET,chunk,2,job.player.getUUID());
        job.chunks.clear();
    }
    private void fail(Pending job, String reason, Exception error) {
        release(job); pending.remove(job.player.getUUID()); reply(job.player,job.id,reason);
        if(error!=null) BleachMod.LOGGER.error("Travel {} failed for {}",job.id,job.player.getUUID(),error);
    }
    private void reply(ServerPlayer player,int id,String reason) {
        BleachMod.LOGGER.debug("Travel request {} for {}: {} at tick {}",id,player.getUUID(),reason,now());
        NetworkHandler.sendToPlayer(new TravelPackets.Result(id,reason),player);
        if(!"preparing".equals(reason)) player.displayClientMessage(net.minecraft.network.chat.Component.translatable("bleachmod.travel."+reason),true);
    }
}
