package com.bleachmod.common.technique;

import com.bleachmod.Reference;
import com.bleachmod.common.data.PlayerData;
import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.common.network.s2c.ActionFeedbackS2C;
import com.bleachmod.common.network.s2c.ResourceSyncS2C;
import com.bleachmod.init.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class TechniqueService {
    public static final int SLOT_1 = 1;
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
    public static final int FLAME_DASH_DURATION_TICKS = 5;
    public static final double FLAME_DASH_SPEED = 1.15D;
    public static final float FLAME_DASH_CONTACT_DAMAGE = 4.0F;
    public static final int FLAME_DASH_CONTACT_FIRE_SECONDS = 2;

    private TechniqueService() {
    }

    public static void executeSlot(ServerPlayer player, PlayerData data, int slot) {
        if (slot == SLOT_1) {
            executeSlotOne(player, data);
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

    private static void executeIgnition(ServerPlayer player, PlayerData data) {
        if (!player.getMainHandItem().is(ModItems.ASAUCHI.get())) {
            data.getStatus().setIgnitionActive(false);
            sendFeedback(player, Component.translatable("message.bleachmod.technique.ignition.requires_asauchi"));
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
        if (!player.getMainHandItem().is(ModItems.ASAUCHI.get())
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

    public static void tickFlameDash(ServerPlayer player, PlayerData data) {
        if (data.getStatus().getFlameDashTicks() <= 0) {
            return;
        }
        if (!player.isAlive() || player.isSpectator()
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

    public static boolean isIgnitionActive(PlayerData data) {
        return data.getStatus().isIgnitionActive();
    }

    public static float ignitionDamageBonus(PlayerData data) {
        return isIgnitionActive(data) ? IGNITION_ZANJUTSU_BONUS : 0.0F;
    }

    public static void applyIgnitionHit(ServerPlayer player, PlayerData data, LivingEntity target) {
        if (!isIgnitionActive(data) || !player.getMainHandItem().is(ModItems.ASAUCHI.get())) {
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
