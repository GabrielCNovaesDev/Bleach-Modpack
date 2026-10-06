package com.bleachmod.common.technique;

import com.bleachmod.common.data.PlayerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** One bounded visual front per owner; damage remains in TechniqueService. No world blocks are changed. */
public final class FlameWaveService {
    public static final int COLLAPSE_TICKS = 6;
    public static final double HEIGHT = 4;
    private static final Map<UUID, Wave> WAVES = new HashMap<>();
    private static final class Wave {
        final Vec3 origin, forward;
        final boolean bankai;
        final String form;
        final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;
        final Set<BlockPos> ground = new HashSet<>();
        int age;
        Wave(ServerPlayer p, PlayerData d, boolean bankai) {
            origin = p.position(); forward = Vec3.directionFromRotation(0, p.getYRot());
            this.bankai = bankai; form = d.getCharacter().getActiveForm(); dimension = p.level().dimension();
        }
    }
    private FlameWaveService() { }
    public static void begin(ServerPlayer player, PlayerData data, boolean bankai) {
        data.getStatus().setFlameBarrageGroundTicks(0);
        data.getStatus().setFlameFanGroundTicks(0);
        WAVES.put(player.getUUID(), new Wave(player, data, bankai));
    }
    public static void cancel(ServerPlayer player) { WAVES.remove(player.getUUID()); }
    public static void clear() { WAVES.clear(); }
    public static void unload(net.minecraft.world.level.Level level) { WAVES.values().removeIf(w -> w.dimension.equals(level.dimension())); }
    public static boolean isActive(ServerPlayer player) { return WAVES.containsKey(player.getUUID()); }
    public static double frontHeight(int age, int range) {
        return age <= range ? HEIGHT : Math.max(0, HEIGHT * (1 - (age - range) / (double) COLLAPSE_TICKS));
    }
    public static void tick(ServerPlayer player, PlayerData data) {
        Wave wave = WAVES.get(player.getUUID());
        if (wave == null) return;
        if (!player.isAlive() || player.isSpectator() || !data.getStatus().hasCreatedCharacter()
                || !TechniqueService.isRyujinJakkaEquipped(player) || !wave.dimension.equals(player.level().dimension())
                || !wave.form.equals(data.getCharacter().getActiveForm())) { cancel(player); return; }
        int range = (int) (wave.bankai ? TechniqueService.FLAME_FAN_RANGE : TechniqueService.FLAME_BARRAGE_RANGE);
        int age = ++wave.age;
        if (age >= range + COLLAPSE_TICKS) {
            if (wave.bankai) data.getStatus().setFlameFanGroundPositions(wave.ground, TechniqueService.FLAME_FAN_GROUND_DURATION_TICKS);
            else data.getStatus().setFlameBarrageGroundPositions(wave.ground, TechniqueService.FLAME_BARRAGE_GROUND_DURATION_TICKS);
            cancel(player); return;
        }
        int step = Math.min(age, range);
        double angle = wave.bankai ? TechniqueService.FLAME_FAN_HALF_ANGLE_RADIANS : TechniqueService.FLAME_BARRAGE_HALF_ANGLE_RADIANS;
        double width = step * Math.tan(angle);
        int samples = Math.max(1, (int) Math.ceil(width));
        Vec3 side = new Vec3(-wave.forward.z, 0, wave.forward.x);
        double height = frontHeight(age, range);
        for (int i = -samples; i <= samples; i++) {
            Vec3 point = wave.origin.add(wave.forward.scale(step)).add(side.scale(i * width / samples));
            BlockPos surface = BlockPos.containing(point);
            if (!player.serverLevel().hasChunkAt(surface)) continue;
            boolean supported = false;
            // Local surface search avoids teleporting flames to a roof or force-loading chunks.
            for (int dy = 2; dy >= -3; dy--) {
                BlockPos candidate = surface.offset(0, dy, 0);
                if (player.level().getBlockState(candidate).isAir() && player.level().getBlockState(candidate.below()).isSolid()) {
                    surface = candidate; supported = true; break;
                }
            }
            if (age <= range && supported) wave.ground.add(surface.immutable());
            for (double y = 0; y < height; y += 0.5) {
                player.serverLevel().sendParticles(ParticleTypes.FLAME, point.x, surface.getY() + y + 0.15, point.z,
                        2, 0.16, 0.12, 0.16, 0.012);
            }
        }
    }
}
