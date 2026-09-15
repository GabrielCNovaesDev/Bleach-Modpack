package com.bleachmod.common.technique;

import com.bleachmod.common.data.PlayerData;
import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.common.network.s2c.ActionFeedbackS2C;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public final class TechniqueService {
    public static final String FLAME_BURST_ID = "flame_burst";
    public static final float FLAME_BURST_COST = 20.0F;
    public static final int FLAME_BURST_COOLDOWN_TICKS = 15 * 20;
    public static final double FLAME_BURST_RADIUS = 3.0D;
    public static final float FLAME_BURST_DAMAGE = 6.0F;

    private TechniqueService() {
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
        if (data.getResources().getCurrentReiatsu() < FLAME_BURST_COST
                || !data.getResources().consumeReiatsu(FLAME_BURST_COST)) {
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
        NetworkHandler.sendToPlayer(ActionFeedbackS2C.of(Component.translatable("message.bleachmod.technique.flame_burst")), player);
        NetworkHandler.sendToPlayer(new com.bleachmod.common.network.s2c.ResourceSyncS2C(player.getId(), data.getResources().save()), player);
    }

    private static void spawnEffects(ServerLevel level, ServerPlayer player) {
        double x = player.getX();
        double y = player.getY() + 1.0D;
        double z = player.getZ();
        level.sendParticles(ParticleTypes.FLAME, x, y, z, 45, 1.8D, 0.8D, 1.8D, 0.08D);
        level.sendParticles(ParticleTypes.LAVA, x, y, z, 8, 1.2D, 0.4D, 1.2D, 0.02D);
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT,
                player.getSoundSource(), 1.0F, 0.85F);
    }

    private static int formatSeconds(int ticks) {
        return Math.max(1, (int) Math.ceil(ticks / 20.0D));
    }

    private static void sendFeedback(ServerPlayer player, Component message) {
        NetworkHandler.sendToPlayer(ActionFeedbackS2C.of(message), player);
    }
}
