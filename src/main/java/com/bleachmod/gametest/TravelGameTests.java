package com.bleachmod.gametest;

import com.bleachmod.common.data.*;
import com.bleachmod.server.travel.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.*;
import java.nio.file.*;
import java.util.*;

@GameTestHolder("bleachmod")
@PrefixGameTestTemplate(false)
public final class TravelGameTests {
    @GameTest(template="empty")
    public static void capabilityRevivesWithoutLosingProgress(GameTestHelper helper) {
        var player=player(helper);var oldHandle=PlayerCapability.get(player);var data=oldHandle.orElseThrow(()->new IllegalStateException("Missing data"));
        data.getResources().addTrainingPoints(321);
        data.getTravel().setReturnLocation(new TravelData.Location("minecraft:overworld",4.5,70,8.5,0,0));
        player.invalidateCaps();
        helper.assertTrue(!oldHandle.isPresent() && !PlayerCapability.get(player).isPresent(),"Invalid entity still exposes capability");
        player.reviveCaps();
        var restored=PlayerCapability.get(player).orElseThrow(()->new IllegalStateException("Capability did not revive"));
        helper.assertTrue(restored==data && restored.getResources().getTrainingPoints()==321,"Revive replaced player progress");
        helper.assertTrue(restored.getTravel().getReturnLocation()!=null,"Return lost on revive");helper.succeed();
    }
    @GameTest(template="empty", timeoutTicks=400)
    public static void travelRoundTripAndCooldown(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getServer() instanceof net.minecraft.gametest.framework.GameTestServer,"Run this fixture only with runGameTestServer");
        var server=helper.getLevel().getServer();var service=DimensionTravelService.get(server);
        var destination=server.getLevel(SoulSociety.LEVEL);
        BlockPos arrival=new BlockPos(2000,80,2000);
        for(int x=124;x<=125;x++)for(int z=124;z<=125;z++)destination.getChunk(x,z);
        destination.setBlockAndUpdate(arrival.below(),Blocks.STONE.defaultBlockState());
        destination.setBlockAndUpdate(arrival,Blocks.AIR.defaultBlockState());destination.setBlockAndUpdate(arrival.above(),Blocks.AIR.defaultBlockState());
        // Vanilla's makeMockServerPlayerInLevel has no Netty channel; Forge registry sync needs one.
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        var channel=new io.netty.channel.embedded.EmbeddedChannel(connection);
        var player=new net.minecraft.server.level.ServerPlayer(server,helper.getLevel(),new GameProfile(UUID.randomUUID(),"TravelRoundTrip"));
        server.getPlayerList().placeNewPlayer(connection,player);
        var origin=helper.absolutePos(new BlockPos(1,3,1));
        helper.getLevel().setBlockAndUpdate(origin.below(),Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(origin,Blocks.AIR.defaultBlockState());helper.getLevel().setBlockAndUpdate(origin.above(),Blocks.AIR.defaultBlockState());
        player.moveTo(origin.getX()+0.5,origin.getY(),origin.getZ()+0.5,0,0);
        var data=PlayerData.get(player).orElseThrow();data.initializeShinigami();
        java.util.function.Consumer<net.minecraftforge.event.entity.EntityTravelToDimensionEvent> veto=event->{
            if(event.getEntity()==player && event.getDimension().equals(SoulSociety.LEVEL)) event.setCanceled(true);
        };
        service.setMap(new SoulSocietyMapInstaller.InstalledMap(1,3465,new TravelData.Location(SoulSociety.ID,2000.5,80,2000.5,0,0),"test",8192,1));
        helper.startSequence()
            .thenExecute(()->service.request(player,new com.bleachmod.common.network.TravelPackets.Request(700,DimensionTravelService.ENTRY,service.revision())))
            .thenWaitUntil(()->helper.assertTrue(player.serverLevel()==destination,"Entry still pending"))
            .thenExecute(()->{
                helper.assertTrue(data.getResources().getCurrentReiatsu()<100 && data.getResources().getCurrentReiatsu()>=80,"Entry debit incorrect");
                helper.assertTrue(data.getTravel().getReturnLocation()!=null,"Return not saved");
                helper.assertTrue(data.getTravel().remaining(server.overworld().getGameTime())>0,"Cooldown missing");
                // Duplicate packet/new request during cooldown must not debit again.
                service.request(player,new com.bleachmod.common.network.TravelPackets.Request(700,DimensionTravelService.ENTRY,service.revision()));
            })
            .thenExecuteAfter(110,()->{
                data.getResources().setCurrentReiatsu(0);
                service.request(player,new com.bleachmod.common.network.TravelPackets.Request(701,DimensionTravelService.RETURN,service.revision()));
            })
            .thenWaitUntil(()->helper.assertTrue(player.serverLevel()==server.overworld(),"Return still pending"))
            .thenExecute(()->{
                helper.assertTrue(player.position().distanceToSqr(Vec3.atBottomCenterOf(origin))<1,"Return missed origin");
                helper.assertTrue(data.getResources().getCurrentReiatsu()<20,"Free return created energy");
            })
            .thenExecuteAfter(12,()->{
                data.getTravel().setCooldown(server.overworld().getGameTime(),0);data.getResources().fillReiatsu();
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(veto);
                service.request(player,new com.bleachmod.common.network.TravelPackets.Request(702,DimensionTravelService.ENTRY,service.revision()));
            })
            .thenExecuteAfter(20,()->{
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(veto);
                helper.assertTrue(player.serverLevel()==server.overworld(),"Cancelled teleport changed dimension");
                helper.assertTrue(data.getResources().getCurrentReiatsu()==100,"Cancelled teleport was charged");
                helper.assertTrue(data.getTravel().remaining(server.overworld().getGameTime())==0,"Cancelled teleport added cooldown");
                service.logout(player);server.getPlayerList().remove(player);service.setMap(null);channel.finishAndReleaseAll();
            }).thenSucceed();
    }
    private static FakePlayer player(GameTestHelper helper) {
        var player=new FakePlayer(helper.getLevel(),new GameProfile(UUID.randomUUID(),"TravelTest"));
        PlayerData.get(player).orElseThrow().initializeShinigami();return player;
    }
    @GameTest(template="empty")
    public static void dimensionIsLoaded(GameTestHelper helper) {
        var level=helper.getLevel().getServer().getLevel(SoulSociety.LEVEL);
        helper.assertTrue(level!=null,"Soul Society missing from dynamic registries");
        helper.assertTrue(level.getMinBuildHeight()==-64 && level.getMaxBuildHeight()==320,"Wrong imported-map height");
        helper.succeed();
    }
    @GameTest(template="empty")
    public static void safeLandingRejectsHazardsAndBlockedHead(GameTestHelper helper) {
        var player=player(helper);var level=helper.getLevel();var pos=helper.absolutePos(new BlockPos(1,2,1));
        level.setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(pos.above(),Blocks.AIR.defaultBlockState());
        Vec3 feet=Vec3.atBottomCenterOf(pos);
        helper.assertTrue(SafeLandingService.safe(level,player,feet),"Clear platform rejected");
        level.setBlockAndUpdate(pos.above(),Blocks.STONE.defaultBlockState());
        helper.assertTrue(!SafeLandingService.safe(level,player,feet),"Blocked head accepted");
        level.setBlockAndUpdate(pos.above(),Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(pos.below(),Blocks.MAGMA_BLOCK.defaultBlockState());
        helper.assertTrue(!SafeLandingService.safe(level,player,feet),"Magma accepted");
        level.setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(pos,Blocks.WATER.defaultBlockState());
        helper.assertTrue(!SafeLandingService.safe(level,player,feet),"Water accepted");
        helper.assertTrue(!SafeLandingService.safe(level,player,new Vec3(30_000_001,80,0)),"World border ignored");
        helper.assertTrue(!SafeLandingService.safe(level,player,new Vec3(0,-64,0)),"Minimum build height ignored");
        helper.succeed();
    }
    @GameTest(template="empty")
    public static void noUnsafeFallbackInVoid(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getServer() instanceof net.minecraft.gametest.framework.GameTestServer,"Run this fixture only with runGameTestServer");
        var player=player(helper);var level=helper.getLevel().getServer().getLevel(SoulSociety.LEVEL);
        var center=new BlockPos(1024,80,1024);
        for(int x=63;x<=64;x++)for(int z=63;z<=64;z++)level.getChunk(x,z);
        helper.assertTrue(SafeLandingService.find(level,player,center).isEmpty(),"Void must have no fallback landing");helper.succeed();
    }
    @GameTest(template="empty")
    public static void mapImportIsAtomicAndNeverOverwrites(GameTestHelper helper) throws Exception {
        Path root=Files.createTempDirectory("bleach-map-test-");
        try {
            Path source=root.resolve("source"),target=root.resolve("world/dimensions/bleachmod/soul_society");
            Files.createDirectories(source.resolve("region"));
            Files.createDirectories(source.resolve("entities"));Files.createFile(source.resolve("entities/r.0.0.mca"));
            byte[] region=new byte[8192];Files.write(source.resolve("region/r.0.0.mca"),region);
            CompoundTag data=new CompoundTag();data.putInt("DataVersion",3465);data.putInt("SpawnX",3);data.putInt("SpawnY",70);data.putInt("SpawnZ",-9);
            CompoundTag tag=new CompoundTag();tag.put("Data",data);NbtIo.writeCompressed(tag,source.resolve("level.dat").toFile());
            Files.createDirectories(target.resolve("data"));Files.writeString(target.resolve("data/raids.dat"),"existing dimension metadata");
            var map=SoulSocietyMapInstaller.install(source,target).orElseThrow();
            helper.assertTrue(Files.readString(target.resolve("data/raids.dat")).equals("existing dimension metadata"),"Pre-map metadata lost");
            helper.assertTrue(map.arrival().x()==3.5 && map.arrival().z()==-8.5,"Source spawn not preserved");
            helper.assertTrue(!Files.exists(target.resolve("level.dat")),"Global level data copied");
            Files.writeString(source.resolve("region/r.0.0.mca"),"changed");
            helper.assertTrue(SoulSocietyMapInstaller.install(source,target).orElseThrow().sha256().equals(map.sha256()),"Existing install replaced");
            helper.assertTrue(Files.size(target.resolve("region/r.0.0.mca"))==8192,"Existing chunks replaced");
            Path occupied=root.resolve("occupied");Files.createDirectories(occupied);Files.writeString(occupied.resolve("keep"),"keep");
            Files.write(source.resolve("region/r.0.0.mca"),region);
            boolean rejected=false;try{SoulSocietyMapInstaller.install(source,occupied);}catch(java.io.IOException expected){rejected=true;}
            helper.assertTrue(rejected && Files.readString(occupied.resolve("keep")).equals("keep"),"Occupied dimension overwritten");
            try(var lockChannel=java.nio.channels.FileChannel.open(source.resolve("session.lock"),StandardOpenOption.WRITE);
                var lock=lockChannel.lock()) {
                rejected=false;try{SoulSocietyMapInstaller.install(source,root.resolve("locked-target"));}catch(java.io.IOException expected){rejected=true;}
                helper.assertTrue(rejected && !Files.exists(root.resolve("locked-target")),"Open source world imported");
            }
            data.putInt("DataVersion",9999);NbtIo.writeCompressed(tag,source.resolve("level.dat").toFile());
            rejected=false;try{SoulSocietyMapInstaller.install(source,root.resolve("new-target"));}catch(java.io.IOException expected){rejected=true;}
            helper.assertTrue(rejected && !Files.exists(root.resolve("new-target")),"Incompatible save partially published");
            Path recovered=root.resolve("recovery/soul_society");
            Path backup=recovered.resolveSibling(".soul-society-before-import");
            Files.createDirectories(backup.resolve("data"));Files.writeString(backup.resolve("data/recovery.dat"),"recover me");
            SoulSocietyMapInstaller.install(root.resolve("absent-source"),recovered);
            helper.assertTrue(Files.readString(recovered.resolve("data/recovery.dat")).equals("recover me"),"Interrupted publication lost old metadata");
        } finally {
            try(var paths=Files.walk(root)){for(Path path:paths.sorted(Comparator.reverseOrder()).toList())Files.delete(path);}
        }
        helper.succeed();
    }
    @GameTest(template="empty")
    public static void travelAdministrationRequiresOperator(GameTestHelper helper) {
        var command=helper.getLevel().getServer().getCommands().getDispatcher().getRoot().getChild("bleachtravel");
        var source=player(helper).createCommandSourceStack();
        helper.assertTrue(!command.canUse(source.withPermission(0)) && command.canUse(source.withPermission(2)),"Travel admin permission broken");helper.succeed();
    }
}
