package com.bleachmod.common.technique;

import com.bleachmod.Reference;
import com.bleachmod.common.data.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Recoverable dimension-wide weather lease, with biome-aware local snow visuals. No block placement. */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class BlizzardWeatherData extends SavedData {
    private static final String FILE = "bleachmod_blizzard_weather";
    private final Map<UUID, Long> owners = new HashMap<>();
    private boolean managed, oldRain, oldThunder;
    private int oldClearTime, oldRainTime, oldThunderTime;
    private long controlledUntil;
    private float oldRainLevel, oldThunderLevel;
    private boolean commandSyncPending;
    public static BlizzardWeatherData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(BlizzardWeatherData::load, BlizzardWeatherData::new, FILE);
    }
    public static boolean eligible(ServerLevel level, BlockPos pos) {
        return Level.OVERWORLD.equals(level.dimension()) && level.dimensionType().hasSkyLight()
                && !level.dimensionType().hasCeiling()
                && supportsBiome(level.getBiome(pos).value(), pos);
    }
    public static boolean supportsBiome(Biome biome, BlockPos pos) { return biome.getPrecipitationAt(pos) != Biome.Precipitation.NONE; }
    public boolean isManaged() { return managed; }
    public void acquire(ServerLevel level, UUID owner) {
        boolean existing = !owners.isEmpty();
        owners.put(owner, level.getGameTime() + 40);
        if (managed || existing) return;
        var weather = level.getServer().getWorldData().overworldData();
        oldRain = weather.isRaining(); oldThunder = weather.isThundering();
        oldClearTime = weather.getClearWeatherTime(); oldRainTime = weather.getRainTime(); oldThunderTime = weather.getThunderTime();
        oldRainLevel = level.getRainLevel(1); oldThunderLevel = thunderIntensity(level);
        start(level); managed = true; setDirty();
    }
    private void start(ServerLevel level) {
        level.setWeatherParameters(0, 120000, true, false); // Snow/rain and cloud cover, without thunder/lightning.
        controlledUntil = level.getGameTime() + 120000; setDirty();
    }
    public void release(ServerLevel level, UUID owner) {
        owners.remove(owner);
        if (owners.isEmpty()) restore(level);
    }
    public void restore(ServerLevel level) {
        if (!managed) return;
        if (stillOurs(level)) {
            var weather = level.getServer().getWorldData().overworldData();
            weather.setClearWeatherTime(oldClearTime); weather.setRainTime(oldRainTime); weather.setThunderTime(oldThunderTime);
            weather.setRaining(oldRain); weather.setThundering(oldThunder);
            level.setRainLevel(oldRainLevel); level.setThunderLevel(oldThunderLevel);
            syncWeather(level);
        }
        managed = false; setDirty();
    }
    private boolean stillOurs(ServerLevel level) {
        var weather = level.getServer().getWorldData().overworldData();
        // Timers are advanced by vanilla and may diverge from game time; they do not establish ownership.
        return weather.isRaining() && !weather.isThundering() && weather.getClearWeatherTime() == 0;
    }
    public void tick(ServerLevel level) {
        if (commandSyncPending) {
            commandSyncPending = false;
            var weather = level.getServer().getWorldData().overworldData();
            // Runs after command execution; clear also repairs a client left raining by older builds.
            if (!weather.isRaining()) { level.setRainLevel(0); level.setThunderLevel(0); }
            syncWeather(level);
        }
        owners.values().removeIf(until -> until <= level.getGameTime());
        if (managed && !stillOurs(level)) { managed = false; setDirty(); } // Respect a subsequent /weather command.
        if (owners.isEmpty()) restore(level); // Also recovers the previous climate after save/restart, before owners rejoin.
        else if (managed && controlledUntil - level.getGameTime() < 60000) start(level);
    }
    public static void tickCaster(ServerPlayer p, PlayerData d) {
        boolean active = p.isAlive() && !p.isSpectator() && d.getStatus().hasCreatedCharacter()
                && CharacterData.HYORINMARU.equals(d.getCharacter().getZanpakutoIdentity())
                && Reference.FORM_BANKAI.equals(d.getCharacter().getActiveForm()) && eligible(p.serverLevel(), p.blockPosition());
        if (!Level.OVERWORLD.equals(p.level().dimension())) return;
        if (!active) { get(p.serverLevel()).release(p.serverLevel(), p.getUUID()); return; }
        get(p.serverLevel()).acquire(p.serverLevel(), p.getUUID());
        if (!get(p.serverLevel()).isManaged() || p.tickCount % 4 != 0) return;
        for (int i = 0; i < 12; i++) {
            double x = p.getX() + (p.getRandom().nextDouble() - 0.5) * 24;
            double z = p.getZ() + (p.getRandom().nextDouble() - 0.5) * 24;
            BlockPos pos = BlockPos.containing(x, p.getY() + 4, z);
            if (!p.serverLevel().hasChunkAt(pos) || !eligible(p.serverLevel(), pos) || !p.level().canSeeSky(pos)) continue;
            p.serverLevel().sendParticles(ParticleTypes.SNOWFLAKE, x, pos.getY(), z, 6, 0.8, 2, 0.8, 0.07);
        }
    }
    public static void cancel(ServerPlayer p) {
        ServerLevel level = p.getServer().getLevel(Level.OVERWORLD);
        if (level != null) get(level).release(level, p.getUUID());
    }
    @SubscribeEvent public static void levelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel level && Level.OVERWORLD.equals(level.dimension())) get(level).tick(level);
    }
    @SubscribeEvent public static void weatherCommand(net.minecraftforge.event.CommandEvent event) {
        var parse = event.getParseResults(); var context = parse.getContext();
        while (context.getChild() != null) context = context.getChild(); // Also handles /execute ... run weather.
        if (!parse.getExceptions().isEmpty() || parse.getReader().canRead() || context.getCommand() == null
                || !context.getSource().hasPermission(2) || context.getNodes().isEmpty()
                || !("weather".equals(context.getNodes().get(0).getNode().getName())
                || "minecraft:weather".equals(context.getNodes().get(0).getNode().getName()))) return;
        ServerLevel level = context.getSource().getServer().getLevel(Level.OVERWORLD);
        if (level != null) {
            var data = get(level);
            // Preserve active leases so the next caster tick cannot immediately overwrite the command.
            data.managed = false; data.commandSyncPending = true; data.setDirty();
        }
    }
    private static void syncWeather(ServerLevel level) {
        var players = level.getServer().getPlayerList();
        var weather = level.getServer().getWorldData().overworldData();
        // Level setters update both current and previous intensity, so vanilla cannot detect this change.
        players.broadcastAll(new net.minecraft.network.protocol.game.ClientboundGameEventPacket(weather.isRaining()
                ? net.minecraft.network.protocol.game.ClientboundGameEventPacket.START_RAINING
                : net.minecraft.network.protocol.game.ClientboundGameEventPacket.STOP_RAINING, 0), level.dimension());
        players.broadcastAll(new net.minecraft.network.protocol.game.ClientboundGameEventPacket(
                net.minecraft.network.protocol.game.ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, level.getRainLevel(1)), level.dimension());
        players.broadcastAll(new net.minecraft.network.protocol.game.ClientboundGameEventPacket(
                net.minecraft.network.protocol.game.ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, thunderIntensity(level)), level.dimension());
    }
    private static float thunderIntensity(ServerLevel level) {
        // getThunderLevel is rain-weighted, while vanilla packets and setters require unweighted intensity.
        float rain = level.getRainLevel(1);
        return rain > 0 ? Math.min(1, level.getThunderLevel(1) / rain) : 0;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("managed", managed); tag.putBoolean("oldRain", oldRain); tag.putBoolean("oldThunder", oldThunder);
        tag.putInt("oldClearTime", oldClearTime); tag.putInt("oldRainTime", oldRainTime); tag.putInt("oldThunderTime", oldThunderTime);
        tag.putFloat("oldRainLevel", oldRainLevel); tag.putFloat("oldThunderLevel", oldThunderLevel); tag.putLong("controlledUntil", controlledUntil);
        tag.putBoolean("rawThunder", true);
        return tag;
    }
    public static BlizzardWeatherData load(CompoundTag tag) {
        var data = new BlizzardWeatherData();
        data.managed = tag.getBoolean("managed"); data.oldRain = tag.getBoolean("oldRain"); data.oldThunder = tag.getBoolean("oldThunder");
        data.oldClearTime = tag.getInt("oldClearTime"); data.oldRainTime = tag.getInt("oldRainTime"); data.oldThunderTime = tag.getInt("oldThunderTime");
        data.oldRainLevel = tag.getFloat("oldRainLevel"); data.oldThunderLevel = tag.getFloat("oldThunderLevel"); data.controlledUntil = tag.getLong("controlledUntil");
        if (!tag.getBoolean("rawThunder") && data.oldRainLevel > 0) data.oldThunderLevel = Math.min(1, data.oldThunderLevel / data.oldRainLevel);
        return data;
    }
}
