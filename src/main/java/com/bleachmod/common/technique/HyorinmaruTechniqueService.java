package com.bleachmod.common.technique;

import com.bleachmod.Reference;
import com.bleachmod.common.data.*;
import com.bleachmod.common.evolution.TransformationsHelper;
import com.bleachmod.common.network.SyncHelper;
import com.bleachmod.init.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.*;
import java.util.*;

/** Provisional four-slot ice/control kit. No frozen terrain, hard stun or flight. */
public final class HyorinmaruTechniqueService {
    private record Field(Vec3 origin, Vec3 forward, boolean barrier, boolean bankai, String form,
                         net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension, long expires) { }
    private static final Map<UUID, Field> FIELDS = new HashMap<>();
    private static final int[] COOLDOWNS = {100, 160, 240, 300};
    private static final float[] COSTS = {15, 20, 25, 30};
    private HyorinmaruTechniqueService() { }
    public static void cancel(ServerPlayer player) { FIELDS.remove(player.getUUID()); }
    public static void clear() { FIELDS.clear(); }
    public static void unload(net.minecraft.world.level.Level level) { FIELDS.values().removeIf(f -> f.dimension.equals(level.dimension())); }
    public static boolean isActive(ServerPlayer player) { return FIELDS.containsKey(player.getUUID()); }
    public static boolean equipped(ServerPlayer p, PlayerData d) {
        return CharacterData.HYORINMARU.equals(d.getCharacter().getZanpakutoIdentity()) && p.getMainHandItem().is(ModItems.HYORINMARU.get());
    }
    public static void executeSlot(ServerPlayer player, PlayerData data, int slot) {
        if (slot < 1 || slot > 4 || !data.getStatus().hasCreatedCharacter() || !player.isAlive() || player.isSpectator()) return;
        if (!equipped(player, data) || !Reference.RACE_SHINIGAMI.equals(data.getCharacter().getRace())
                || !Reference.GROUP_ZANPAKUTO.equals(data.getCharacter().getActiveFormGroup())) {
            player.displayClientMessage(Component.translatable("message.bleachmod.technique.requires_hyorinmaru"), true); return;
        }
        String form = data.getCharacter().getActiveForm();
        boolean released = Reference.FORM_SHIKAI.equals(form) || Reference.FORM_BANKAI.equals(form);
        if (!Reference.FORM_SEALED.equals(form) && !released) return;
        if (released && !TransformationsHelper.isUnlocked(data, Reference.GROUP_ZANPAKUTO, form)) {
            player.displayClientMessage(Component.translatable("message.bleachmod.hyorinmaru.locked"), true); return;
        }
        if (!released && slot >= 3) {
            player.displayClientMessage(Component.translatable("message.bleachmod.hyorinmaru.locked"), true); return;
        }
        StatusData status = data.getStatus();
        int cooldown = switch (slot) {
            case 1 -> status.getTechniqueSlot1CooldownTicks(); case 2 -> status.getTechniqueSlot2CooldownTicks();
            case 3 -> status.getTechniqueSlot3CooldownTicks(); default -> status.getTechniqueSlot4CooldownTicks();
        };
        if (cooldown > 0) {
            player.displayClientMessage(Component.translatable("message.bleachmod.technique.cooldown", (cooldown + 19) / 20), true); return;
        }
        boolean bankai = Reference.FORM_BANKAI.equals(form);
        float cost = released ? COSTS[slot - 1] + (bankai ? 5 : 0) : 5;
        if (!data.getResources().consumeReiatsu(cost)) {
            player.displayClientMessage(Component.translatable("message.bleachmod.technique.no_reiatsu", (int) cost), true); return;
        }
        switch (slot) {
            case 1 -> status.setTechniqueSlot1CooldownTicks(COOLDOWNS[0]);
            case 2 -> status.setTechniqueSlot2CooldownTicks(COOLDOWNS[1]);
            case 3 -> status.setTechniqueSlot3CooldownTicks(COOLDOWNS[2]);
            case 4 -> status.setTechniqueSlot4CooldownTicks(COOLDOWNS[3]);
        }
        Vec3 forward = Vec3.directionFromRotation(0, player.getYRot());
        if (slot <= 2) strike(player, slot, released, bankai, forward);
        else {
            Field field = new Field(slot == 3 ? player.position().add(forward.scale(3)) : player.position(), forward,
                    slot == 3, bankai, form, player.level().dimension(), player.level().getGameTime() + (slot == 3 ? 60 : 80));
            FIELDS.put(player.getUUID(), field); // One field per owner, including free-cast test mode.
            tick(player, data);
        }
        player.displayClientMessage(Component.translatable("message.bleachmod.hyorinmaru.slot_" + slot), true);
        SyncHelper.resources(player);
    }
    private static void strike(ServerPlayer p, int slot, boolean released, boolean bankai, Vec3 forward) {
        double range = !released ? 3 : slot == 1 ? (bankai ? 14 : 10) : (bankai ? 8 : 6);
        double halfAngle = slot == 1 ? Math.toRadians(15) : Math.toRadians(35);
        for (LivingEntity target : p.serverLevel().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(range),
                e -> TechniqueTargets.allowed(p, e))) {
            Vec3 delta = target.position().subtract(p.position());
            double forwardDistance = delta.dot(forward);
            double lateral = Math.abs(delta.x * forward.z - delta.z * forward.x);
            if (forwardDistance < 0 || forwardDistance > range || lateral > 0.7 + forwardDistance * Math.tan(halfAngle)
                    || Math.abs(delta.y) > 3 || !p.hasLineOfSight(target)) continue;
            float damage = !released ? 3 : slot == 1 ? (bankai ? 12 : 8) : (bankai ? 6 : 4);
            if (target.hurt(p.damageSources().playerAttack(p), damage) && released) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slot == 1 ? 60 : 80, bankai ? 2 : 1));
            }
        }
        for (double step = 0.5; step <= range; step += 0.5) {
            Vec3 point = p.position().add(forward.scale(step)).add(0, 1, 0);
            if (!p.serverLevel().hasChunkAt(net.minecraft.core.BlockPos.containing(point))) continue;
            p.serverLevel().sendParticles(released ? ParticleTypes.SNOWFLAKE : ParticleTypes.CRIT,
                    point.x, point.y, point.z, 4, slot == 1 ? 0.2 : step * Math.tan(halfAngle), 0.3, 0.2, 0.015);
        }
    }
    public static void tick(ServerPlayer p, PlayerData d) {
        Field f = FIELDS.get(p.getUUID());
        if (f == null) return;
        long time = p.level().getGameTime();
        if (!p.isAlive() || p.isSpectator() || !equipped(p, d) || !d.getStatus().hasCreatedCharacter()
                || !f.dimension.equals(p.level().dimension()) || !f.form.equals(d.getCharacter().getActiveForm()) || time >= f.expires) {
            cancel(p); return;
        }
        if (time % 4 == 0) {
            for (int i = 0; i < 16; i++) {
                double angle = i * Math.PI * 2 / 16;
                Vec3 point = f.barrier ? f.origin.add(-f.forward.z * (i / 2.5 - 3), (i % 4) * 0.8, f.forward.x * (i / 2.5 - 3))
                        : f.origin.add(Math.cos(angle) * (f.bankai ? 6 : 4), 0.2, Math.sin(angle) * (f.bankai ? 6 : 4));
                if (p.serverLevel().hasChunkAt(net.minecraft.core.BlockPos.containing(point)))
                    p.serverLevel().sendParticles(ParticleTypes.SNOWFLAKE, point.x, point.y, point.z, 3, 0.15, 0.2, 0.15, 0.01);
            }
        }
        if (time % 10 != 0) return;
        double range = f.barrier ? 4 : f.bankai ? 6 : 4;
        for (LivingEntity target : p.serverLevel().getEntitiesOfClass(LivingEntity.class, new AABB(f.origin, f.origin).inflate(range, 3, range),
                e -> TechniqueTargets.allowed(p, e))) {
            Vec3 delta = target.position().subtract(f.origin);
            if (!p.hasLineOfSight(target)) continue;
            if (f.barrier) {
                double lateral = Math.abs(delta.x * f.forward.z - delta.z * f.forward.x);
                if (Math.abs(delta.dot(f.forward)) > 0.8 || lateral > 3 || Math.abs(delta.y) > 3) continue;
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1));
                target.push(f.forward.x * 0.22, 0.04, f.forward.z * 0.22); target.hurtMarked = true;
            } else if (delta.x * delta.x + delta.z * delta.z <= range * range && Math.abs(delta.y) <= 2
                    && target.hurt(p.damageSources().playerAttack(p), f.bankai ? 3 : 2)) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, f.bankai ? 2 : 1));
            }
        }
    }
}
