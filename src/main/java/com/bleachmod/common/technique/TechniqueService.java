package com.bleachmod.common.technique;

import com.bleachmod.Reference;
import com.bleachmod.common.data.PlayerData;
import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.common.network.s2c.ActionFeedbackS2C;
import com.bleachmod.common.network.s2c.ResourceSyncS2C;
import com.bleachmod.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public final class TechniqueService {
    public static final int SLOT_1 = 1;
    public static final int SLOT_2 = 2;
    public static final String FLAME_BURST_ID = "flame_burst";
    public static final float FLAME_BURST_COST = 20.0F;
    public static final int FLAME_BURST_COOLDOWN_TICKS = 15 * 20;
    public static final double FLAME_BURST_RADIUS = 3.0D;
    public static final float FLAME_BURST_DAMAGE = 6.0F;

    public static final float IGNITION_DRAIN_PER_TICK = 0.05F;
    public static final float IGNITION_ZANJUTSU_BONUS = 0.15F;
    public static final int IGNITION_FIRE_SECONDS = 3;

    public static final float FLAME_DASH_COST = 20.0F;
    public static final int FLAME_DASH_COOLDOWN_TICKS = 100;
    /** Approximately 20 blocks when the full dash completes without collision. */
    public static final int FLAME_DASH_DURATION_TICKS = 16;
    public static final double FLAME_DASH_SPEED = 1.25D;
    public static final float FLAME_DASH_CONTACT_DAMAGE = 4.0F;
    public static final int FLAME_DASH_CONTACT_FIRE_SECONDS = 2;

    public static final float FLAME_BARRAGE_COST = 15.0F;
    public static final int FLAME_BARRAGE_COOLDOWN_TICKS = 4 * 20;
    public static final double FLAME_BARRAGE_RANGE = 8.0D;
    public static final double FLAME_BARRAGE_HALF_ANGLE_RADIANS = Math.PI / 6.0D;
    public static final float FLAME_BARRAGE_DAMAGE = 4.0F;
    public static final int FLAME_BARRAGE_FIRE_SECONDS = 3;
    public static final int FLAME_BARRAGE_GROUND_DURATION_TICKS = 3 * 20;
    public static final float FLAME_BARRAGE_GROUND_DAMAGE = 1.0F;

    public static final float FLAME_FAN_COST = 25.0F;
    public static final double FLAME_FAN_RANGE = 14.0D;
    public static final double FLAME_FAN_HALF_ANGLE_RADIANS = Math.PI / 4.0D;
    public static final float FLAME_FAN_DAMAGE = 8.0F;
    public static final int FLAME_FAN_FIRE_SECONDS = 4;
    public static final int FLAME_FAN_GROUND_DURATION_TICKS = 4 * 20;
    public static final float FLAME_FAN_GROUND_DAMAGE = 1.0F;

    public static final int SLOT_4 = 4;
    public static final float STEAM_CUT_COST = 45.0F;
    /** Temporarily disabled to allow repeated manual testing. */
    public static final int STEAM_CUT_COOLDOWN_TICKS = 0;
    public static final double STEAM_CUT_RANGE = 100.0D;
    /** 45 degrees total horizontal opening, represented by a 22.5 degree half-angle. */
    public static final double STEAM_CUT_HALF_ANGLE_RADIANS = Math.PI / 8.0D;
    public static final double STEAM_CUT_MIN_HALF_WIDTH = 0.65D;
    public static final int STEAM_CUT_BELOW_PLAYER_BLOCKS = 15;
    public static final int STEAM_CUT_ABOVE_PLAYER_BLOCKS = 20;
    public static final float STEAM_CUT_DAMAGE = 16.0F;

    private TechniqueService() {
    }

    public static void executeSlot(ServerPlayer player, PlayerData data, int slot) {
        if (slot == SLOT_1) {
            executeSlotOne(player, data);
        } else if (slot == SLOT_2) {
            executeSlotTwo(player, data);
        } else if (slot == SLOT_4) {
            executeSlotFour(player, data);
        }
    }

    private static void executeSlotOne(ServerPlayer player, PlayerData data) {
        if (!data.getStatus().hasCreatedCharacter() || !player.isAlive() || player.isSpectator()) {
            return;
        }
        if (Reference.FORM_BANKAI.equalsIgnoreCase(data.getCharacter().getActiveForm())) {
            executeFlameDash(player, data);
        } else {
            executeIgnition(player, data);
        }
    }

    private static void executeSlotTwo(ServerPlayer player, PlayerData data) {
        if (!data.getStatus().hasCreatedCharacter() || !player.isAlive() || player.isSpectator()) {
            return;
        }
        if (Reference.FORM_BANKAI.equalsIgnoreCase(data.getCharacter().getActiveForm())) {
            executeFlameFan(player, data);
            return;
        }
        executeFlameBarrage(player, data);
    }

    private static void executeSlotFour(ServerPlayer player, PlayerData data) {
        if (!data.getStatus().hasCreatedCharacter() || !player.isAlive() || player.isSpectator()) {
            return;
        }
        if (!Reference.FORM_BANKAI.equalsIgnoreCase(data.getCharacter().getActiveForm())) {
            sendFeedback(player, Component.translatable("message.bleachmod.technique.unavailable"));
            return;
        }
        executeConcentratedSteamCut(player, data);
    }

    private static void executeConcentratedSteamCut(ServerPlayer player, PlayerData data) {
        if (!isRyujinJakkaEquipped(player)) {
            sendFeedback(player, Component.translatable("message.bleachmod.technique.requires_ryujin_jakka"));
            return;
        }
        if (data.getStatus().getTechniqueSlot4CooldownTicks() > 0) {
            sendFeedback(player, Component.translatable("message.bleachmod.technique.cooldown",
                    formatSeconds(data.getStatus().getTechniqueSlot4CooldownTicks())));
            return;
        }
        if (!data.getResources().consumeReiatsu(STEAM_CUT_COST)) {
            sendFeedback(player, Component.translatable("message.bleachmod.technique.no_reiatsu",
                    (int) STEAM_CUT_COST));
            return;
        }

        data.getStatus().setTechniqueSlot4CooldownTicks(STEAM_CUT_COOLDOWN_TICKS);
        applyConcentratedSteamCut(player);
        sendFeedback(player, Component.translatable("message.bleachmod.technique.steam_cut"));
        syncResources(player, data);
    }

    private static void applyConcentratedSteamCut(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 origin = player.position();
        Vec3 direction = player.getLookAngle().normalize();
        Set<UUID> hitEntities = new HashSet<>();
        DamageSource source = player.damageSources().indirectMagic(player, player);

        for (int step = 1; step <= (int) STEAM_CUT_RANGE; step++) {
            Vec3 center = origin.add(direction.scale(step));
            double halfWidth = steamCutHalfWidthAt(step);
            destroySteamCutBlocks(level, center, origin.y, halfWidth);
            AABB entityArea = new AABB(
                    center.x - halfWidth,
                    center.y - STEAM_CUT_BELOW_PLAYER_BLOCKS,
                    center.z - halfWidth,
                    center.x + halfWidth,
                    center.y + STEAM_CUT_ABOVE_PLAYER_BLOCKS,
                    center.z + halfWidth);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, entityArea,
                    entity -> entity != player && entity.isAlive())) {
                if (hitEntities.add(target.getUUID())) {
                    target.hurt(source, STEAM_CUT_DAMAGE);
                }
            }
            spawnSteamCutLine(level, center, direction);
        }

        Vec3 impact = origin.add(direction.scale(STEAM_CUT_RANGE));
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, impact.x, impact.y, impact.z,
                18, 0.75D, 0.75D, 0.75D, 0.045D);
        level.sendParticles(ParticleTypes.CLOUD, impact.x, impact.y, impact.z,
                110, 1.1D, 1.4D, 1.1D, 0.09D);
        level.sendParticles(ParticleTypes.CRIT, impact.x, impact.y, impact.z,
                36, 0.7D, 0.7D, 0.7D, 0.12D);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                player.getSoundSource(), 0.8F, 1.25F);
        level.playSound(null, impact.x, impact.y, impact.z, SoundEvents.GENERIC_EXPLODE,
                player.getSoundSource(), 0.65F, 1.35F);
    }

    private static double steamCutHalfWidthAt(int step) {
        return Math.max(STEAM_CUT_MIN_HALF_WIDTH, step * Math.tan(STEAM_CUT_HALF_ANGLE_RADIANS));
    }

    private static void destroySteamCutBlocks(ServerLevel level, Vec3 center, double playerY, double halfWidth) {
        int minX = (int) Math.floor(center.x - halfWidth);
        int maxX = (int) Math.floor(center.x + halfWidth);
        int minY = (int) Math.floor(playerY - STEAM_CUT_BELOW_PLAYER_BLOCKS);
        int maxY = (int) Math.floor(playerY + STEAM_CUT_ABOVE_PLAYER_BLOCKS);
        int minZ = (int) Math.floor(center.z - halfWidth);
        int maxZ = (int) Math.floor(center.z + halfWidth);
        for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            if (isProtectedSteamCutBlock(level, pos)) {
                continue;
            }
            level.destroyBlock(pos, false);
        }
    }

    private static boolean isProtectedSteamCutBlock(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.isAir()
                || state.is(Blocks.BEDROCK)
                || state.is(Blocks.COMMAND_BLOCK)
                || state.is(Blocks.CHAIN_COMMAND_BLOCK)
                || state.is(Blocks.REPEATING_COMMAND_BLOCK)
                || state.getDestroySpeed(level, pos) < 0.0F;
    }

    private static void spawnSteamCutLine(ServerLevel level, Vec3 center, Vec3 direction) {
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y, center.z,
                4, 0.18D, 0.25D, 0.18D, 0.025D);
        level.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z,
                24, 0.35D, 1.25D, 0.35D, 0.055D);
        Vec3 perpendicular = new Vec3(-direction.z, direction.x, 0.0D);
        if (perpendicular.lengthSqr() > 0.0001D) {
            perpendicular = perpendicular.normalize();
            level.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z,
                    3, perpendicular.x * 0.3D, perpendicular.y * 0.3D, perpendicular.z * 0.3D, 0.02D);
        }
    }

    private static void executeFlameFan(ServerPlayer player, PlayerData data) {
        if (!isRyujinJakkaEquipped(player)) {
            sendFeedback(player, Component.translatable("message.bleachmod.technique.requires_ryujin_jakka"));
            return;
        }
        if (data.getStatus().getTechniqueSlot2CooldownTicks() > 0) {
            sendFeedback(player, Component.translatable("message.bleachmod.technique.cooldown",
                    formatSeconds(data.getStatus().getTechniqueSlot2CooldownTicks())));
            return;
        }
        if (!data.getResources().consumeReiatsu(FLAME_FAN_COST)) {
            sendFeedback(player, Component.translatable("message.bleachmod.technique.no_reiatsu",
                    (int) FLAME_FAN_COST));
            return;
        }

        data.getStatus().setTechniqueSlot2CooldownTicks(FLAME_BARRAGE_COOLDOWN_TICKS);
        applyFlameFan(player, data);
        sendFeedback(player, Component.translatable("message.bleachmod.technique.flame_fan"));
        syncResources(player, data);
    }

    private static void applyFlameFan(ServerPlayer player, PlayerData data) {
        ServerLevel level = player.serverLevel();
        Vec3 origin = player.getEyePosition();
        Vec3 direction = player.getLookAngle().normalize();
        double minimumDot = Math.cos(FLAME_FAN_HALF_ANGLE_RADIANS);
        DamageSource source = player.damageSources().playerAttack(player);
        AABB area = player.getBoundingBox().inflate(FLAME_FAN_RANGE);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != player && entity.isAlive())) {
            Vec3 toTarget = target.getBoundingBox().getCenter().subtract(origin);
            double distance = toTarget.length();
            boolean pointBlankContact = distance <= 0.75D;
            if (distance > FLAME_FAN_RANGE || (!pointBlankContact
                    && (distance == 0.0D || toTarget.normalize().dot(direction) < minimumDot))) {
                continue;
            }
            target.hurt(source, FLAME_FAN_DAMAGE);
            target.setSecondsOnFire(FLAME_FAN_FIRE_SECONDS);
            spawnFlameFanImpact(level, target);
        }

        Set<BlockPos> groundPositions = spawnFlameFanParticles(level, player, direction);
        data.getStatus().setFlameFanGroundPositions(groundPositions, FLAME_FAN_GROUND_DURATION_TICKS);
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT,
                player.getSoundSource(), 1.0F, 0.9F);
    }

    private static void spawnFlameFanImpact(ServerLevel level, LivingEntity target) {
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(), 32, 0.45D, 0.55D, 0.45D, 0.045D);
    }

    private static Set<BlockPos> spawnFlameFanParticles(ServerLevel level, ServerPlayer player, Vec3 direction) {
        Set<BlockPos> groundPositions = new HashSet<>();
        Vec3 horizontal = new Vec3(direction.x, 0.0D, direction.z);
        if (horizontal.lengthSqr() < 0.0001D) {
            horizontal = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            horizontal = horizontal.normalize();
        }
        Vec3 side = new Vec3(-horizontal.z, 0.0D, horizontal.x);
        for (int step = 1; step <= (int) FLAME_FAN_RANGE; step++) {
            double halfWidth = step * Math.tan(FLAME_FAN_HALF_ANGLE_RADIANS);
            int sideSamples = Math.max(2, (int) Math.ceil(halfWidth));
            for (int sideStep = -sideSamples; sideStep <= sideSamples; sideStep++) {
                Vec3 point = player.position().add(horizontal.scale(step))
                        .add(side.scale(sideStep * halfWidth / sideSamples));
                BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        BlockPos.containing(point.x, player.getY(), point.z));
                groundPositions.add(surface);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, surface.getX() + 0.5D,
                        surface.getY() + 0.35D, surface.getZ() + 0.5D,
                        18, 0.34D, 0.18D, 0.34D, 0.04D);
            }
        }
        return groundPositions;
    }

    private static void executeFlameBarrage(ServerPlayer player, PlayerData data) {
        if (!isRyujinJakkaEquipped(player)) {
            sendFeedback(player, Component.translatable("message.bleachmod.technique.requires_ryujin_jakka"));
            return;
        }
        if (data.getStatus().getTechniqueSlot2CooldownTicks() > 0) {
            sendFeedback(player, Component.translatable("message.bleachmod.technique.cooldown",
                    formatSeconds(data.getStatus().getTechniqueSlot2CooldownTicks())));
            return;
        }
        if (!data.getResources().consumeReiatsu(FLAME_BARRAGE_COST)) {
            sendFeedback(player, Component.translatable("message.bleachmod.technique.no_reiatsu",
                    (int) FLAME_BARRAGE_COST));
            return;
        }

        data.getStatus().setTechniqueSlot2CooldownTicks(FLAME_BARRAGE_COOLDOWN_TICKS);
        applyFlameBarrage(player, data);
        sendFeedback(player, Component.translatable("message.bleachmod.technique.flame_barrage"));
        syncResources(player, data);
    }

    private static void applyFlameBarrage(ServerPlayer player, PlayerData data) {
        ServerLevel level = player.serverLevel();
        Vec3 origin = player.getEyePosition();
        Vec3 direction = player.getLookAngle().normalize();
        double minimumDot = Math.cos(FLAME_BARRAGE_HALF_ANGLE_RADIANS);
        DamageSource source = player.damageSources().playerAttack(player);
        AABB area = player.getBoundingBox().inflate(FLAME_BARRAGE_RANGE);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != player && entity.isAlive())) {
            Vec3 toTarget = target.getBoundingBox().getCenter().subtract(origin);
            double distance = toTarget.length();
            boolean pointBlankContact = distance <= 0.75D;
            if (distance > FLAME_BARRAGE_RANGE || (!pointBlankContact
                    && (distance == 0.0D || toTarget.normalize().dot(direction) < minimumDot))) {
                continue;
            }
            target.hurt(source, FLAME_BARRAGE_DAMAGE);
            target.setSecondsOnFire(FLAME_BARRAGE_FIRE_SECONDS);
            spawnBarrageImpact(level, target);
        }

        Set<BlockPos> groundPositions = spawnBarrageSurface(level, player, direction, true);
        data.getStatus().setFlameBarrageGroundPositions(groundPositions, FLAME_BARRAGE_GROUND_DURATION_TICKS);
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT,
                player.getSoundSource(), 0.7F, 1.3F);
    }

    private static void spawnBarrageImpact(ServerLevel level, LivingEntity target) {
        level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 0.15D, target.getZ(),
                12, 0.3D, 0.08D, 0.3D, 0.03D);
        level.sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY() + 0.4D, target.getZ(),
                4, 0.18D, 0.12D, 0.18D, 0.01D);
    }

    private static Set<BlockPos> spawnBarrageSurface(ServerLevel level, ServerPlayer player, Vec3 direction,
                                                      boolean dense) {
        Set<BlockPos> positions = new HashSet<>();
        Vec3 horizontal = new Vec3(direction.x, 0.0D, direction.z);
        if (horizontal.lengthSqr() < 0.0001D) {
            return positions;
        }
        horizontal = horizontal.normalize();
        Vec3 side = new Vec3(-horizontal.z, 0.0D, horizontal.x);
        for (int step = 0; step <= (int) FLAME_BARRAGE_RANGE; step++) {
            double halfWidth = step * Math.tan(FLAME_BARRAGE_HALF_ANGLE_RADIANS);
            int sideSamples = dense ? Math.max(1, (int) Math.ceil(halfWidth)) : 1;
            for (int sideStep = -sideSamples; sideStep <= sideSamples; sideStep++) {
                Vec3 point = player.position().add(horizontal.scale(step)).add(side.scale(sideStep * halfWidth / sideSamples));
                BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        BlockPos.containing(point.x, player.getY(), point.z));
                positions.add(surface);
                sendBarrageGroundParticles(level, surface, dense);
            }
        }
        return positions;
    }

    private static void sendBarrageGroundParticles(ServerLevel level, BlockPos surface, boolean dense) {
        level.sendParticles(ParticleTypes.FLAME, surface.getX() + 0.5D, surface.getY() + 0.18D,
                surface.getZ() + 0.5D, dense ? 14 : 4, 0.28D, 0.12D, 0.28D, 0.035D);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, surface.getX() + 0.5D, surface.getY() + 0.28D,
                surface.getZ() + 0.5D, dense ? 5 : 1, 0.2D, 0.12D, 0.2D, 0.02D);
        level.sendParticles(ParticleTypes.SMOKE, surface.getX() + 0.5D, surface.getY() + 0.38D,
                surface.getZ() + 0.5D, dense ? 3 : 1, 0.18D, 0.14D, 0.18D, 0.012D);
    }

    private static void executeIgnition(ServerPlayer player, PlayerData data) {
        if (!isRyujinJakkaEquipped(player)) {
            data.getStatus().setIgnitionActive(false);
            sendFeedback(player, Component.translatable("message.bleachmod.technique.requires_ryujin_jakka"));
            return;
        }

        boolean active = !data.getStatus().isIgnitionActive();
        data.getStatus().setIgnitionActive(active);
        if (active) {
            sendFeedback(player, Component.translatable("message.bleachmod.technique.ignition.on"));
            spawnIgnitionBurst(player.serverLevel(), player);
        } else {
            sendFeedback(player, Component.translatable("message.bleachmod.technique.ignition.off"));
        }
        syncResources(player, data);
    }

    private static void executeFlameDash(ServerPlayer player, PlayerData data) {
        if (!isRyujinJakkaEquipped(player)) {
            sendFeedback(player, Component.translatable("message.bleachmod.technique.requires_ryujin_jakka"));
            return;
        }
        if (data.getStatus().getTechniqueSlot1CooldownTicks() > 0) {
            sendFeedback(player, Component.translatable("message.bleachmod.technique.cooldown",
                    formatSeconds(data.getStatus().getTechniqueSlot1CooldownTicks())));
            return;
        }
        if (!data.getResources().consumeReiatsu(FLAME_DASH_COST)) {
            sendFeedback(player, Component.translatable("message.bleachmod.technique.no_reiatsu",
                    (int) FLAME_DASH_COST));
            return;
        }

        data.getStatus().setIgnitionActive(false);
        data.getStatus().beginFlameDash();
        data.getStatus().setTechniqueSlot1CooldownTicks(FLAME_DASH_COOLDOWN_TICKS);
        data.getStatus().setFlameDashTicks(FLAME_DASH_DURATION_TICKS);
        player.startAutoSpinAttack(FLAME_DASH_DURATION_TICKS);
        spawnDashBurst(player.serverLevel(), player);
        sendFeedback(player, Component.translatable("message.bleachmod.technique.flame_dash"));
        syncResources(player, data);
    }

    public static void tickIgnition(ServerPlayer player, PlayerData data) {
        if (!data.getStatus().isIgnitionActive()) {
            return;
        }
        if (!isRyujinJakkaEquipped(player)
                || Reference.FORM_BANKAI.equalsIgnoreCase(data.getCharacter().getActiveForm())) {
            data.getStatus().setIgnitionActive(false);
            sendFeedback(player, Component.translatable("message.bleachmod.technique.ignition.off"));
            return;
        }
        if (!data.getResources().consumeReiatsu(IGNITION_DRAIN_PER_TICK)) {
            data.getStatus().setIgnitionActive(false);
            sendFeedback(player, Component.translatable("message.bleachmod.technique.ignition.empty"));
            return;
        }
        if (player.tickCount % 4 == 0) {
            player.serverLevel().sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0D,
                    player.getZ(), 2, 0.18D, 0.25D, 0.18D, 0.01D);
        }
    }

    public static void tickFlameFanGround(ServerPlayer player, PlayerData data) {
        if (data.getStatus().getFlameFanGroundTicks() <= 0) {
            return;
        }
        if (!player.isAlive() || player.isSpectator() || !isRyujinJakkaEquipped(player)
                || !Reference.FORM_BANKAI.equalsIgnoreCase(data.getCharacter().getActiveForm())) {
            data.getStatus().setFlameFanGroundTicks(0);
            return;
        }

        ServerLevel level = player.serverLevel();
        for (BlockPos surface : data.getStatus().getFlameFanGroundPositions()) {
            if (player.tickCount % 2 == 0) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, surface.getX() + 0.5D,
                        surface.getY() + 0.35D, surface.getZ() + 0.5D,
                        16, 0.34D, 0.18D, 0.34D, 0.04D);
            }
            if (player.tickCount % 10 != 0) {
                continue;
            }
            AABB area = new AABB(surface).inflate(0.65D, 0.6D, 0.65D);
            DamageSource source = player.damageSources().playerAttack(player);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                    entity -> entity != player && entity.isAlive())) {
                target.hurt(source, FLAME_FAN_GROUND_DAMAGE);
                target.setSecondsOnFire(1);
            }
        }
    }

    public static void tickFlameBarrageGround(ServerPlayer player, PlayerData data) {
        if (data.getStatus().getFlameBarrageGroundTicks() <= 0) {
            return;
        }
        if (!player.isAlive() || player.isSpectator() || !isRyujinJakkaEquipped(player)
                || Reference.FORM_BANKAI.equalsIgnoreCase(data.getCharacter().getActiveForm())) {
            data.getStatus().setFlameBarrageGroundTicks(0);
            return;
        }

        ServerLevel level = player.serverLevel();
        for (BlockPos surface : data.getStatus().getFlameBarrageGroundPositions()) {
            if (player.tickCount % 2 == 0) {
                sendBarrageGroundParticles(level, surface, true);
            }
            if (player.tickCount % 10 != 0) {
                continue;
            }
            AABB area = new AABB(surface).inflate(0.65D, 0.6D, 0.65D);
            DamageSource source = player.damageSources().playerAttack(player);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                    entity -> entity != player && entity.isAlive())) {
                target.hurt(source, FLAME_BARRAGE_GROUND_DAMAGE);
                target.setSecondsOnFire(1);
            }
        }
    }

    public static void tickFlameDash(ServerPlayer player, PlayerData data) {
        if (data.getStatus().getFlameDashTicks() <= 0) {
            return;
        }
        if (!player.isAlive() || player.isSpectator()
                || !isRyujinJakkaEquipped(player)
                || !Reference.FORM_BANKAI.equalsIgnoreCase(data.getCharacter().getActiveForm())) {
            stopFlameDash(player, data);
            return;
        }

        Vec3 direction = player.getLookAngle().normalize();
        Vec3 displacement = direction.scale(FLAME_DASH_SPEED);
        AABB currentBox = player.getBoundingBox();
        AABB sweptBox = currentBox.expandTowards(displacement).inflate(0.18D);
        applyFlameDashContactDamage(player, data, sweptBox);

        if (!player.level().noCollision(player, currentBox.move(displacement))) {
            stopFlameDash(player, data);
            return;
        }
        player.setDeltaMovement(displacement);
        player.hurtMarked = true;
        spawnDashTrail(player.serverLevel(), player, direction);
        data.getStatus().setFlameDashTicks(data.getStatus().getFlameDashTicks() - 1);
        if (data.getStatus().getFlameDashTicks() <= 0) {
            player.setDeltaMovement(Vec3.ZERO);
            data.getStatus().clearFlameDashHits();
        }
    }

    private static void applyFlameDashContactDamage(ServerPlayer player, PlayerData data, AABB sweptBox) {
        ServerLevel level = player.serverLevel();
        DamageSource source = player.damageSources().playerAttack(player);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, sweptBox,
                entity -> entity != player && entity.isAlive())) {
            if (!data.getStatus().markFlameDashHit(target.getUUID())) {
                continue;
            }
            target.hurt(source, FLAME_DASH_CONTACT_DAMAGE);
            target.setSecondsOnFire(FLAME_DASH_CONTACT_FIRE_SECONDS);
            level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + target.getBbHeight() * 0.5D,
                    target.getZ(), 14, 0.3D, 0.4D, 0.3D, 0.03D);
            level.playSound(null, target.blockPosition(), SoundEvents.FIRECHARGE_USE,
                    player.getSoundSource(), 0.5F, 1.25F);
        }
    }

    private static void stopFlameDash(ServerPlayer player, PlayerData data) {
        data.getStatus().setFlameDashTicks(0);
        data.getStatus().clearFlameDashHits();
        player.setDeltaMovement(Vec3.ZERO);
    }

    public static boolean isRyujinJakkaEquipped(ServerPlayer player) {
        return player.getMainHandItem().is(ModItems.RYUJIN_JAKKA.get());
    }

    public static boolean isIgnitionActive(PlayerData data) {
        return data.getStatus().isIgnitionActive();
    }

    public static float ignitionDamageBonus(PlayerData data) {
        return isIgnitionActive(data) ? IGNITION_ZANJUTSU_BONUS : 0.0F;
    }

    public static void applyIgnitionHit(ServerPlayer player, PlayerData data, LivingEntity target) {
        if (!isIgnitionActive(data) || !isRyujinJakkaEquipped(player)) {
            return;
        }
        target.setSecondsOnFire(IGNITION_FIRE_SECONDS);
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(), 8, 0.25D, 0.35D, 0.25D, 0.02D);
        level.playSound(null, target.blockPosition(), SoundEvents.FIRECHARGE_USE,
                player.getSoundSource(), 0.35F, 1.2F);
    }

    private static void spawnIgnitionBurst(ServerLevel level, ServerPlayer player) {
        level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0D,
                player.getZ(), 18, 0.45D, 0.5D, 0.45D, 0.03D);
        level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE,
                player.getSoundSource(), 0.6F, 1.0F);
    }

    private static void spawnDashBurst(ServerLevel level, ServerPlayer player) {
        level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.8D,
                player.getZ(), 42, 0.65D, 0.55D, 0.65D, 0.12D);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.8D,
                player.getZ(), 16, 0.45D, 0.45D, 0.45D, 0.08D);
        level.sendParticles(ParticleTypes.LAVA, player.getX(), player.getY() + 0.8D,
                player.getZ(), 10, 0.5D, 0.35D, 0.5D, 0.04D);
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT,
                player.getSoundSource(), 0.95F, 1.15F);
    }

    private static void spawnDashTrail(ServerLevel level, ServerPlayer player, Vec3 direction) {
        Vec3 back = direction.scale(-0.65D);
        double x = player.getX() + back.x;
        double y = player.getY() + 0.8D;
        double z = player.getZ() + back.z;
        level.sendParticles(ParticleTypes.FLAME, x, y, z, 18,
                0.45D, 0.45D, 0.45D, 0.09D);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 5,
                0.28D, 0.28D, 0.28D, 0.06D);
        level.sendParticles(ParticleTypes.LAVA, x, y, z, 2,
                0.2D, 0.2D, 0.2D, 0.02D);
    }

    public static void executeFlameBurst(ServerPlayer player, PlayerData data) {
        if (!data.getStatus().hasCreatedCharacter() || !player.isAlive() || player.isSpectator()) {
            return;
        }
        if (data.getStatus().getFlameBurstCooldownTicks() > 0) {
            sendFeedback(player, Component.translatable("message.bleachmod.technique.cooldown",
                    formatSeconds(data.getStatus().getFlameBurstCooldownTicks())));
            return;
        }
        if (!data.getResources().consumeReiatsu(FLAME_BURST_COST)) {
            sendFeedback(player, Component.translatable("message.bleachmod.technique.no_reiatsu",
                    (int) FLAME_BURST_COST));
            return;
        }

        data.getStatus().setFlameBurstCooldownTicks(FLAME_BURST_COOLDOWN_TICKS);
        ServerLevel level = player.serverLevel();
        spawnEffects(level, player);
        DamageSource source = player.damageSources().indirectMagic(player, player);
        AABB area = player.getBoundingBox().inflate(FLAME_BURST_RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != player && entity.isAlive())) {
            target.hurt(source, FLAME_BURST_DAMAGE);
        }
        sendFeedback(player, Component.translatable("message.bleachmod.technique.flame_burst"));
        syncResources(player, data);
    }

    private static void spawnEffects(ServerLevel level, ServerPlayer player) {
        level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0D, player.getZ(),
                45, 1.8D, 0.8D, 1.8D, 0.08D);
        level.sendParticles(ParticleTypes.LAVA, player.getX(), player.getY() + 1.0D, player.getZ(),
                8, 1.2D, 0.4D, 1.2D, 0.02D);
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT,
                player.getSoundSource(), 1.0F, 0.85F);
    }

    private static void syncResources(ServerPlayer player, PlayerData data) {
        NetworkHandler.sendToPlayer(new ResourceSyncS2C(player.getId(), data.getResources().save()), player);
    }

    private static int formatSeconds(int ticks) {
        return Math.max(1, (int) Math.ceil(ticks / 20.0D));
    }

    private static void sendFeedback(ServerPlayer player, Component message) {
        NetworkHandler.sendToPlayer(ActionFeedbackS2C.of(message), player);
    }
}
