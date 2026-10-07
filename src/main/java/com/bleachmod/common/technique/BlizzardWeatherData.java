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
        boolean existing = owners.containsKey(owner);
        owners.put(owner, level.getGameTime() + 40);
        if (managed || existing) return;
        var weather = level.getServer().getWorldData().overworldData();
        oldRain = weather.isRaining(); oldThunder = weather.isThundering();
        oldClearTime = weather.getClearWeatherTime(); oldRainTime = weather.getRainTime(); oldThunderTime = weather.getThunderTime();
        oldRainLevel = level.getRainLevel(1); oldThunderLevel = level.getThunderLevel(1);
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
        }
        managed = false; setDirty();
    }
    private boolean stillOurs(ServerLevel level) {
        var weather = level.getServer().getWorldData().overworldData();
        return weather.isRaining() && !weather.isThundering() && weather.getClearWeatherTime() == 0
                && Math.abs(weather.getRainTime() - (level.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE) ? controlledUntil - level.getGameTime() : 120000)) <= 3;
    }
    public void tick(ServerLevel level) {
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
        if (p.tickCount % 4 != 0) return;
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
    @Override public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("managed", managed); tag.putBoolean("oldRain", oldRain); tag.putBoolean("oldThunder", oldThunder);
        tag.putInt("oldClearTime", oldClearTime); tag.putInt("oldRainTime", oldRainTime); tag.putInt("oldThunderTime", oldThunderTime);
        tag.putFloat("oldRainLevel", oldRainLevel); tag.putFloat("oldThunderLevel", oldThunderLevel); tag.putLong("controlledUntil", controlledUntil);
        return tag;
    }
    public static BlizzardWeatherData load(CompoundTag tag) {
        var data = new BlizzardWeatherData();
        data.managed = tag.getBoolean("managed"); data.oldRain = tag.getBoolean("oldRain"); data.oldThunder = tag.getBoolean("oldThunder");
        data.oldClearTime = tag.getInt("oldClearTime"); data.oldRainTime = tag.getInt("oldRainTime"); data.oldThunderTime = tag.getInt("oldThunderTime");
        data.oldRainLevel = tag.getFloat("oldRainLevel"); data.oldThunderLevel = tag.getFloat("oldThunderLevel"); data.controlledUntil = tag.getLong("controlledUntil");
        return data;
    }
}
