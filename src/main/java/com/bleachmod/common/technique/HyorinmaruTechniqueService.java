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

/** Provisional four-slot ice/control kit with bounded, temporary snowy terrain. */
public final class HyorinmaruTechniqueService {
    private record Field(Vec3 origin, Vec3 forward, boolean barrier, boolean bankai, String form,
                         net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension, long expires) { }
    private static final Map<UUID, Field> FIELDS = new HashMap<>();
    private static final Map<UUID, com.bleachmod.entity.IceDragonEntity> DRAGONS = new HashMap<>();
    private static final int[] COOLDOWNS = {100, 160, 240, 300};
    private static final float[] COSTS = {15, 20, 25, 30};
    public static final int SHIKAI_ZONE_RADIUS = 10, BANKAI_ZONE_RADIUS = 30;
    private HyorinmaruTechniqueService() { }
    public static void cancel(ServerPlayer player) {
        FIELDS.remove(player.getUUID()); GlacialSnowData.get(player.serverLevel()).release(player.serverLevel(), player.getUUID());
        IceArmorService.cancel(player); IceControlService.cancel(player); BlizzardWeatherData.cancel(player);
        var dragon = DRAGONS.remove(player.getUUID()); if (dragon != null) dragon.discard();
    }
    public static void clear() { FIELDS.clear(); DRAGONS.values().forEach(net.minecraft.world.entity.Entity::discard); DRAGONS.clear(); IceArmorService.clear(); IceControlService.clear(); }
    public static void unload(net.minecraft.world.level.Level level) {
        FIELDS.values().removeIf(f -> f.dimension.equals(level.dimension())); IceArmorService.unload(level); IceControlService.unload(level);
        DRAGONS.values().removeIf(dragon -> { if (dragon.level() != level) return false; dragon.discard(); return true; });
    }
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
        StatusData status = data.getStatus();
        int cooldown = switch (slot) {
            case 1 -> status.getTechniqueSlot1CooldownTicks(); case 2 -> status.getTechniqueSlot2CooldownTicks();
            case 3 -> status.getTechniqueSlot3CooldownTicks(); default -> status.getTechniqueSlot4CooldownTicks();
        };
        if (cooldown > 0) {
            player.displayClientMessage(Component.translatable("message.bleachmod.technique.cooldown", (cooldown + 19) / 20), true); return;
        }
        boolean bankai = Reference.FORM_BANKAI.equals(form);
        float cost = released ? COSTS[slot - 1] + (bankai ? 5 : 0) : switch (slot) { case 3 -> 12; case 4 -> 18; default -> 8; };
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
        else if (!released && slot == 3) IceArmorService.begin(player, data);
        else if (!released) duelControl(player, forward);
        else {
            Field field = new Field(slot == 3 ? player.position().add(forward.scale(3)) : player.position(), forward,
                    slot == 3, bankai, form, player.level().dimension(), player.level().getGameTime() + (slot == 3 ? 60 : 80));
            GlacialSnowData.get(player.serverLevel()).release(player.serverLevel(), player.getUUID());
            FIELDS.put(player.getUUID(), field); // One field per owner, including free-cast test mode.
        }
        player.serverLevel().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.GLASS_BREAK, player.getSoundSource(), 0.7F, bankai ? 0.7F : 1.2F);
        player.displayClientMessage(Component.translatable("message.bleachmod.hyorinmaru." + (!released && slot >= 3 ? "sealed_" : "slot_") + slot), true);
        SyncHelper.resources(player);
    }
    private static void strike(ServerPlayer p, int slot, boolean released, boolean bankai, Vec3 forward) {
        double range = !released ? 6 : slot == 1 ? (bankai ? 14 : 10) : (bankai ? 8 : 6);
        double halfAngle = slot == 1 ? Math.toRadians(15) : Math.toRadians(35);
        for (LivingEntity target : p.serverLevel().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(range),
                e -> TechniqueTargets.allowed(p, e))) {
            Vec3 delta = target.position().subtract(p.position());
            double forwardDistance = delta.dot(forward);
            double lateral = Math.abs(delta.x * forward.z - delta.z * forward.x);
            if (forwardDistance < 0 || forwardDistance > range || lateral > 0.7 + forwardDistance * Math.tan(halfAngle)
                    || Math.abs(delta.y) > 3 || !p.hasLineOfSight(target)) continue;
            float damage = !released ? 3 : slot == 1 ? (bankai ? 12 : 8) : (bankai ? 6 : 4);
            if (target.hurt(p.damageSources().playerAttack(p), damage)) {
                if (released) IceControlService.chill(p, target, slot == 1 ? 60 : 80, bankai ? 2 : 1);
                else target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
            }
        }
        for (double step = 0.5; step <= range; step += 0.5) {
            Vec3 point = p.position().add(forward.scale(step)).add(0, 1, 0);
            if (!p.serverLevel().hasChunkAt(net.minecraft.core.BlockPos.containing(point))) continue;
            p.serverLevel().sendParticles(ParticleTypes.SNOWFLAKE,
                    point.x, point.y, point.z, bankai ? 18 : released ? 12 : 8, slot == 1 ? 0.3 : step * Math.tan(halfAngle), 0.6, 0.25, 0.04);
            p.serverLevel().sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.45F, 0.85F, 1), bankai ? 1.6F : 1),
                    point.x, point.y, point.z, released ? 4 : 2, 0.2, 0.4, 0.2, 0.01);
        }
        p.serverLevel().playSound(null, p.blockPosition(), net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, p.getSoundSource(), 0.8F, 0.8F);
        if (released && slot == 1) {
            var old = DRAGONS.remove(p.getUUID()); if (old != null) old.discard();
            var dragon = com.bleachmod.registry.ModEntities.ICE_DRAGON.get().create(p.serverLevel());
            if (dragon != null) { dragon.configure(p, forward, bankai); if (p.serverLevel().addFreshEntity(dragon)) DRAGONS.put(p.getUUID(), dragon); }
        }
    }
    private static void duelControl(ServerPlayer p, Vec3 forward) {
        LivingEntity target = p.serverLevel().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(5), e -> {
            Vec3 delta = e.position().subtract(p.position()); double ahead = delta.dot(forward);
            return TechniqueTargets.allowed(p, e) && ahead >= 0 && ahead <= 5 && Math.abs(delta.y) <= 2
                    && Math.abs(delta.x * forward.z - delta.z * forward.x) <= 0.7 + ahead * Math.tan(Math.toRadians(20)) && p.hasLineOfSight(e);
        }).stream().min(Comparator.comparingDouble(p::distanceToSqr)).orElse(null);
        if (target == null) return;
        IceControlService.chill(p, target, 60, 3);
        target.push(forward.x * 0.45, 0.1, forward.z * 0.45); target.hurtMarked = true;
        p.serverLevel().sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1, target.getZ(), 45, 0.5, 0.8, 0.5, 0.05);
    }
    public static void tick(ServerPlayer p, PlayerData d) {
        IceArmorService.tick(p); BlizzardWeatherData.tickCaster(p, d);
        var dragon = DRAGONS.get(p.getUUID()); if (dragon != null && dragon.isRemoved()) DRAGONS.remove(p.getUUID());
        Field f = FIELDS.get(p.getUUID());
        if (f == null) return;
        long time = p.level().getGameTime();
        if (!p.isAlive() || p.isSpectator() || !equipped(p, d) || !d.getStatus().hasCreatedCharacter()
                || !f.dimension.equals(p.level().dimension()) || !f.form.equals(d.getCharacter().getActiveForm()) || time >= f.expires) {
            FIELDS.remove(p.getUUID()); GlacialSnowData.get(p.serverLevel()).release(p.serverLevel(), p.getUUID());
            return; // Field expiration must not cancel independent Bankai weather or armor.
        }
        if (time % 4 == 0) {
            int samples = f.barrier ? 16 : f.bankai ? 64 : 32;
            double zoneRadius = f.bankai ? BANKAI_ZONE_RADIUS : SHIKAI_ZONE_RADIUS;
            for (int i = 0; i < samples; i++) {
                double angle = i * 2.399963229728653 + time * 0.07;
                double radius = zoneRadius * Math.sqrt((i + 0.5) / samples);
                Vec3 point = f.barrier ? f.origin.add(-f.forward.z * (i / 2.5 - 3), (i % 4) * 0.8, f.forward.x * (i / 2.5 - 3))
                        : f.origin.add(Math.cos(angle) * radius, 0.2, Math.sin(angle) * radius);
                if (p.serverLevel().hasChunkAt(net.minecraft.core.BlockPos.containing(point)))
                    p.serverLevel().sendParticles(ParticleTypes.SNOWFLAKE, point.x, point.y + (f.bankai ? 1 : 0.5), point.z, f.bankai ? 14 : 8, 0.6, f.bankai ? 2 : 0.8, 0.6, 0.04);
            }
            if (!f.barrier) snowPatches(p, f, zoneRadius);
        }
        if (time % 10 != 0) return;
        double range = f.barrier ? 4 : f.bankai ? BANKAI_ZONE_RADIUS : SHIKAI_ZONE_RADIUS;
        for (LivingEntity target : p.serverLevel().getEntitiesOfClass(LivingEntity.class, new AABB(f.origin, f.origin).inflate(range, 5, range),
                e -> TechniqueTargets.allowed(p, e))) {
            Vec3 delta = target.position().subtract(f.origin);
            if (!p.hasLineOfSight(target)) continue;
            if (f.barrier) {
                double lateral = Math.abs(delta.x * f.forward.z - delta.z * f.forward.x);
                if (Math.abs(delta.dot(f.forward)) > 0.8 || lateral > 3 || Math.abs(delta.y) > 3) continue;
                IceControlService.chill(p, target, 30, f.bankai ? 2 : 1);
                target.push(f.forward.x * 0.22, 0.04, f.forward.z * 0.22); target.hurtMarked = true;
            } else if (TechniqueGeometry.intersectsCylinder(f.origin, range, 4, 4, target.getBoundingBox())
                    && target.hurt(p.damageSources().playerAttack(p), f.bankai ? 3 : 2)) {
                IceControlService.chill(p, target, 30, f.bankai ? 2 : 1);
            }
        }
    }
    private static void snowPatches(ServerPlayer p, Field f, double radius) {
        // Fixed attempt budget, clustered carpets across the disk; never scans all 2,800 Bankai columns.
        var level = p.serverLevel(); var snow = GlacialSnowData.get(level);
        if (snow.ownerCount(p.getUUID()) >= GlacialSnowData.OWNER_LIMIT) return;
        for (int i = 0; i < (f.bankai ? 12 : 6); i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = Math.sqrt(level.random.nextDouble()) * Math.max(0, radius - 2);
            var center = net.minecraft.core.BlockPos.containing(f.origin.add(Math.cos(angle) * distance, 3, Math.sin(angle) * distance));
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                var column = center.offset(dx, 0, dz);
                for (int dy = 0; dy <= 7; dy++) {
                    var pos = column.below(dy); if (!level.hasChunkAt(pos)) break;
                    if (snow.place(level, pos, p.getUUID(), f.expires, f.bankai ? 1 + level.random.nextInt(3) : 1)) break;
                }
            }
        }
    }
}
