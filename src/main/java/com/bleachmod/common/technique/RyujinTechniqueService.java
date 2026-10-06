package com.bleachmod.common.technique;

import com.bleachmod.Reference;
import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.data.PlayerData;
import com.bleachmod.common.events.BleachEvents;
import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.common.network.SyncHelper;
import com.bleachmod.common.network.s2c.ActionFeedbackS2C;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Server-only runtime. No particles, areas or summons are serialized into player progression. */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class RyujinTechniqueService {
    public static final double WALL_LENGTH = 18;
    public static final double WALL_WIDTH = 2;
    public static final double WALL_HEIGHT = 8;
    public static final int WALL_PARTICLES_PER_SAMPLE = 3;
    public static final int WALL_DRAW_INTERVAL = 4;
    public static final int WALL_DURATION = 100;
    public static final float WALL_COST = 40;
    public static final float WALL_DAMAGE = 18;
    public static final int WALL_COOLDOWN = 400;
    public static final double TORNADO_RADIUS = 6;
    public static final double TORNADO_HEIGHT = 10;
    public static final int TORNADO_DURATION = 200;
    public static final float TORNADO_COST = 40;
    public static final float TORNADO_DAMAGE = 2;
    public static final int TORNADO_COOLDOWN = 300;
    public static final int BAT_COUNT = 5;
    public static final int BAT_DURATION = 800;
    public static final float BAT_HEALTH = 4;
    public static final float BAT_COST = 25;
    public static final float BAT_DAMAGE = 2;
    public static final int BAT_COOLDOWN = 200;
    private static final String BAT_MARKER = "bleachmod_flame_bat";
    private static final String BAT_OWNER = "bleachmod_flame_bat_owner";
    private static final Map<UUID, Runtime> ACTIVE = new HashMap<>();

    private RyujinTechniqueService() { }

    private static final class Runtime {
        final ServerPlayer owner;
        final ServerLevel level;
        Area area;
        Swarm swarm;
        Runtime(ServerPlayer owner) { this.owner = owner; this.level = owner.serverLevel(); }
    }

    private static final class Area {
        final boolean tornado;
        final Vec3 origin;
        final Vec3 direction;
        final long expiresAt;
        final Map<UUID, Long> nextDamage = new HashMap<>();
        int age;
        Area(boolean tornado, Vec3 origin, Vec3 direction, long now) {
            this.tornado = tornado;
            this.origin = origin;
            this.direction = direction;
            this.expiresAt = now + (tornado ? TORNADO_DURATION : WALL_DURATION);
        }
    }

    private static final class Swarm {
        final List<Bat> bats = new ArrayList<>();
        final Map<UUID, Long> nextDamage = new HashMap<>();
        final long expiresAt;
        Swarm(long now) { expiresAt = now + BAT_DURATION; }
    }

    private static boolean validOwner(ServerPlayer player, PlayerData data) {
        String form = data.getCharacter().getActiveForm();
        return player.isAlive() && !player.isSpectator() && data.getStatus().hasCreatedCharacter()
                && Reference.RACE_SHINIGAMI.equals(data.getCharacter().getRace())
                && Reference.GROUP_ZANPAKUTO.equals(data.getCharacter().getActiveFormGroup())
                && (Reference.FORM_SEALED.equals(form) || Reference.FORM_SHIKAI.equals(form)
                    || Reference.FORM_BANKAI.equals(form));
    }

    private static boolean bankai(PlayerData data) {
        return Reference.FORM_BANKAI.equals(data.getCharacter().getActiveForm());
    }

    private static boolean ready(ServerPlayer player, PlayerData data, int cooldown, float cost) {
        if (!validOwner(player, data)) return false;
        if (!TechniqueService.isRyujinJakkaEquipped(player)) {
            feedback(player, "requires_ryujin_jakka");
            return false;
        }
        if (cooldown > 0) {
            feedback(player, "cooldown", (int) Math.ceil(cooldown / 20.0));
            return false;
        }
        if (!data.getResources().canAffordReiatsu(cost)) {
            feedback(player, "no_reiatsu", (int) cost);
            return false;
        }
        return true;
    }

    public static void executeArea(ServerPlayer player, PlayerData data) {
        boolean tornado = bankai(data);
        float cost = tornado ? TORNADO_COST : WALL_COST;
        if (!ready(player, data, data.getStatus().getTechniqueSlot3CooldownTicks(), cost)) return;
        Runtime runtime = ACTIVE.computeIfAbsent(player.getUUID(), id -> new Runtime(player));
        if (runtime.area != null && !data.getStatus().areCooldownsDisabled()) {
            feedback(player, "effect_active"); return;
        }
        if (!data.getResources().consumeReiatsu(cost)) return;
        Vec3 direction = Vec3.directionFromRotation(0, player.getYRot()).normalize();
        Vec3 origin = tornado ? player.position() : player.position().add(direction);
        runtime.area = new Area(tornado, origin, direction, runtime.level.getGameTime());
        data.getStatus().setTechniqueSlot3CooldownTicks(tornado ? TORNADO_COOLDOWN : WALL_COOLDOWN);
        if (!tornado) damageArea(runtime, origin, WALL_DAMAGE, 3);
        drawArea(runtime, origin);
        runtime.level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT,
                player.getSoundSource(), 0.8F, tornado ? 0.7F : 1.0F);
        feedback(player, tornado ? "flame_tornado" : "flame_wall");
        SyncHelper.resources(player);
    }

    public static void executeBats(ServerPlayer player, PlayerData data) {
        if (bankai(data) || !ready(player, data, data.getStatus().getTechniqueSlot4CooldownTicks(), BAT_COST)) return;
        Runtime runtime = ACTIVE.computeIfAbsent(player.getUUID(), id -> new Runtime(player));
        if (runtime.swarm != null) {
            runtime.swarm.bats.removeIf(bat -> !bat.isAlive() || bat.isRemoved());
            if (!runtime.swarm.bats.isEmpty() && !data.getStatus().areCooldownsDisabled()) {
                feedback(player, "bats_active"); return;
            }
            clearSwarm(runtime);
        }
        Swarm swarm = new Swarm(runtime.level.getGameTime());
        runtime.swarm = swarm;
        for (int i = 0; i < BAT_COUNT; i++) {
            Bat bat = EntityType.BAT.create(runtime.level);
            if (bat == null) { clearSwarm(runtime); feedback(player, "summon_failed"); return; }
            double angle = i * Math.PI * 2 / BAT_COUNT;
            Vec3 pos = player.position().add(Math.cos(angle), 1.3, Math.sin(angle));
            bat.moveTo(pos.x, pos.y, pos.z, player.getYRot(), 0);
            bat.setNoAi(true);
            bat.setNoGravity(true);
            bat.setResting(false);
            bat.getAttribute(Attributes.MAX_HEALTH).setBaseValue(BAT_HEALTH);
            bat.setHealth(BAT_HEALTH);
            bat.getPersistentData().putBoolean(BAT_MARKER, true);
            bat.getPersistentData().putUUID(BAT_OWNER, player.getUUID());
            swarm.bats.add(bat);
            if (!runtime.level.noCollision(bat, bat.getBoundingBox()) || !runtime.level.addFreshEntity(bat)) {
                clearSwarm(runtime);
                feedback(player, "summon_failed");
                return;
            }
        }
        if (!data.getResources().consumeReiatsu(BAT_COST)) { clearSwarm(runtime); return; }
        data.getStatus().setTechniqueSlot4CooldownTicks(BAT_COOLDOWN);
        feedback(player, "flame_bats");
        SyncHelper.resources(player);
    }

    private static boolean targetAllowed(ServerPlayer owner, LivingEntity target) {
        return TechniqueTargets.allowed(owner, target);
    }

    private static void damageArea(Runtime runtime, Vec3 center, float damage, int fireSeconds) {
        Area area = runtime.area;
        double radius = TORNADO_RADIUS;
        long now = runtime.level.getGameTime();
        AABB box = area.tornado ? new AABB(center.x - radius, center.y, center.z - radius,
                center.x + radius, center.y + TORNADO_HEIGHT, center.z + radius)
                : TechniqueGeometry.wallBounds(center, area.direction, WALL_LENGTH, WALL_WIDTH, WALL_HEIGHT);
        for (LivingEntity target : runtime.level.getEntitiesOfClass(LivingEntity.class, box,
                entity -> targetAllowed(runtime.owner, entity))) {
            boolean inside = area.tornado
                    ? TechniqueGeometry.intersectsCylinder(center, radius, 0, TORNADO_HEIGHT, target.getBoundingBox())
                    : TechniqueGeometry.intersectsWall(center, area.direction, WALL_LENGTH, WALL_WIDTH,
                            WALL_HEIGHT, target.getBoundingBox());
            if (!inside
                    || area.nextDamage.getOrDefault(target.getUUID(), Long.MIN_VALUE) > now
                    || !runtime.owner.hasLineOfSight(target)) continue;
            area.nextDamage.put(target.getUUID(), now + (area.tornado ? 10 : 20));
            boolean hurt = target.hurt(runtime.owner.damageSources().playerAttack(runtime.owner), damage);
            if (!area.tornado && hurt && target instanceof Mob) {
                Vec3 side = new Vec3(-area.direction.z, 0, area.direction.x);
                double sign = target.position().subtract(center).dot(side) < 0 ? -1 : 1;
                target.knockback(0.25, -side.x * sign, -side.z * sign);
            }
            if (hurt) target.setSecondsOnFire(fireSeconds);
        }
        area.nextDamage.entrySet().removeIf(entry -> entry.getValue() < now);
    }

    private static void drawArea(Runtime runtime, Vec3 center) {
        Area area = runtime.area;
        if (area.tornado) {
            // Two clockwise helices (viewed from above), with actual tangential particle velocity.
            for (int arm = 0; arm < 2; arm++) for (int i = 0; i < 48; i++) {
                double height = TORNADO_HEIGHT * i / 47.0;
                double radius = 1 + (TORNADO_RADIUS - 1) * height / TORNADO_HEIGHT;
                double angle = TechniqueGeometry.tornadoAngle(area.age, i, arm);
                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;
                runtime.level.sendParticles(ParticleTypes.FLAME, x, center.y + height, z,
                        0, -Math.sin(angle), 0.18, Math.cos(angle), 0.14);
                if (i % 3 == 0) runtime.level.sendParticles(ParticleTypes.FLAME, x, center.y + height,
                        z, 3, 0.18, 0.15, 0.18, 0.02);
            }
        } else {
            // Dense lower wall with a bounded emission budget. Samples never apply damage.
            for (int along = 0; along < WALL_LENGTH; along++) for (int height = 0; height < WALL_HEIGHT; height++) {
                Vec3 point = center.add(area.direction.scale(along + 0.5));
                runtime.level.sendParticles(ParticleTypes.FLAME, point.x, center.y + height + 0.5,
                        point.z, WALL_PARTICLES_PER_SAMPLE, Math.abs(area.direction.z) * 0.65 + 0.12, 0.3,
                        Math.abs(area.direction.x) * 0.8 + 0.12, 0.015);
            }
        }
    }

    private static void tickSwarm(Runtime runtime) {
        Swarm swarm = runtime.swarm;
        long now = runtime.level.getGameTime();
        swarm.bats.removeIf(bat -> !bat.isAlive() || bat.isRemoved());
        if (now >= swarm.expiresAt || swarm.bats.isEmpty()) { clearSwarm(runtime); return; }
        List<Mob> targets = runtime.level.getEntitiesOfClass(Mob.class,
                runtime.owner.getBoundingBox().inflate(8), target -> target instanceof Enemy
                        && targetAllowed(runtime.owner, target)
                        && runtime.owner.hasLineOfSight(target));
        for (int i = 0; i < swarm.bats.size(); i++) {
            Bat bat = swarm.bats.get(i);
            Mob target = targets.stream().min(java.util.Comparator.comparingDouble(bat::distanceToSqr)).orElse(null);
            double angle = i * Math.PI * 2 / BAT_COUNT + now * 0.05;
            Vec3 goal = target == null ? runtime.owner.position().add(Math.cos(angle) * 1.5, 1.5, Math.sin(angle) * 1.5)
                    : target.getBoundingBox().getCenter();
            if (bat.distanceToSqr(runtime.owner) > 24 * 24) {
                Vec3 home = runtime.owner.position().add(0, 1.5, 0);
                if (runtime.level.noCollision(bat, bat.getBoundingBox().move(home.subtract(bat.position())))) {
                    bat.teleportTo(home.x, home.y, home.z);
                }
            }
            Vec3 motion = goal.subtract(bat.position());
            Vec3 velocity = motion.lengthSqr() < 0.01 ? Vec3.ZERO : motion.normalize().scale(Math.min(0.35, motion.length()));
            // NoAI mobs do not run normal authoritative travel. Move explicitly, using vanilla collision.
            bat.setDeltaMovement(Vec3.ZERO);
            bat.move(MoverType.SELF, velocity);
            bat.hasImpulse = true;
            if (velocity.horizontalDistanceSqr() > 0.0001) {
                bat.setYRot((float) Math.toDegrees(Math.atan2(-velocity.x, velocity.z)));
                bat.setYHeadRot(bat.getYRot());
            }
            bat.setResting(false);
            if (now % 4 == 0) runtime.level.sendParticles(ParticleTypes.FLAME,
                    bat.getX(), bat.getY() + 0.2, bat.getZ(), 2, 0.1, 0.1, 0.1, 0.01);
            if (target != null && bat.distanceToSqr(target) <= 2.25 && bat.hasLineOfSight(target)
                    && swarm.nextDamage.getOrDefault(target.getUUID(), Long.MIN_VALUE) <= now) {
                // Shared per-target gate prevents five overlapping bats from dealing five hits in a tick.
                swarm.nextDamage.put(target.getUUID(), now + 20);
                if (target.hurt(runtime.owner.damageSources().playerAttack(runtime.owner), BAT_DAMAGE)) target.setSecondsOnFire(2);
                runtime.level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 0.5,
                        target.getZ(), 8, 0.2, 0.2, 0.2, 0.01);
            }
        }
        swarm.nextDamage.entrySet().removeIf(entry -> entry.getValue() < now);
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        // Damage can kill another caster and remove their runtime through LivingDeathEvent.
        // Iterate a snapshot so lifecycle callbacks cannot invalidate this traversal.
        for (Runtime runtime : new ArrayList<>(ACTIVE.values())) {
            ServerPlayer owner = runtime.owner;
            if (ACTIVE.get(owner.getUUID()) != runtime) continue;
            tickEffects(owner);
        }
    }

    /** Same server runtime tick used by integration tests with FakePlayer fixtures. */
    public static void tickEffects(ServerPlayer owner) {
            Runtime runtime = ACTIVE.get(owner.getUUID());
            if (runtime == null) return;
            PlayerData data = PlayerCapability.get(owner).orElse(null);
            if (data == null || !validOwner(owner, data) || owner.serverLevel() != runtime.level
                    || !TechniqueService.isRyujinJakkaEquipped(owner)) {
                clearSwarm(runtime); ACTIVE.remove(owner.getUUID(), runtime); return;
            }
            if (runtime.area != null) {
                Area area = runtime.area;
                if (runtime.level.getGameTime() >= area.expiresAt || area.tornado != bankai(data)) runtime.area = null;
                else {
                    area.age++;
                    Vec3 center = area.tornado ? owner.position() : area.origin;
                    damageArea(runtime, center, area.tornado ? TORNADO_DAMAGE : WALL_DAMAGE, area.tornado ? 2 : 3);
                    if (area.age % (area.tornado ? 2 : WALL_DRAW_INTERVAL) == 0) drawArea(runtime, center);
                }
            }
            if (runtime.swarm != null) {
                if (bankai(data)) clearSwarm(runtime);
                else tickSwarm(runtime);
            }
            if (runtime.area == null && runtime.swarm == null) ACTIVE.remove(owner.getUUID(), runtime);
    }

    private static void clearSwarm(Runtime runtime) {
        if (runtime.swarm == null) return;
        runtime.swarm.bats.forEach(Bat::discard);
        runtime.swarm = null;
    }

    public static void cancel(ServerPlayer player) {
        FlameWaveService.cancel(player);
        HyorinmaruTechniqueService.cancel(player);
        Runtime runtime = ACTIVE.remove(player.getUUID());
        if (runtime != null) clearSwarm(runtime);
    }

    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) cancel(player);
    }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) cancel(player);
    }
    @SubscribeEvent public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) cancel(player);
    }
    @SubscribeEvent public static void form(BleachEvents.FormChangeEvent event) { cancel(event.getPlayer()); }
    @SubscribeEvent public static void stopping(ServerStoppingEvent event) {
        FlameWaveService.clear();
        HyorinmaruTechniqueService.clear();
        ACTIVE.values().forEach(RyujinTechniqueService::clearSwarm);
        ACTIVE.clear();
    }
    @SubscribeEvent public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel level) {
            FlameWaveService.unload(level);
            HyorinmaruTechniqueService.unload(level);
        }
        ACTIVE.values().removeIf(runtime -> {
            if (runtime.level != event.getLevel()) return false;
            clearSwarm(runtime); return true;
        });
    }
    @SubscribeEvent public static void orphan(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Bat bat)
                || !bat.getPersistentData().getBoolean(BAT_MARKER)) return;
        Runtime runtime = bat.getPersistentData().hasUUID(BAT_OWNER)
                ? ACTIVE.get(bat.getPersistentData().getUUID(BAT_OWNER)) : null;
        if (runtime == null || runtime.level != event.getLevel() || runtime.swarm == null
                || !runtime.swarm.bats.contains(bat)) {
            event.setCanceled(true);
            bat.discard();
        }
    }

    private static void feedback(ServerPlayer player, String key, Object... args) {
        NetworkHandler.sendToPlayer(ActionFeedbackS2C.of(
                Component.translatable("message.bleachmod.technique." + key, args)), player);
    }
}
