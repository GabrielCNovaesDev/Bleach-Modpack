package com.bleachmod.common.technique;

import com.bleachmod.Reference;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Keep vanilla freezing synced while a short control lease exists. Vanilla handles frost overlay/damage/thaw. */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class IceControlService {
    private record Lease(LivingEntity target, Map<UUID, Long> owners) { }
    private static final Map<UUID, Lease> TARGETS = new HashMap<>();
    private IceControlService() { }
    public static void chill(ServerPlayer owner, LivingEntity target, int duration, int amplifier) {
        if (!TechniqueTargets.allowed(owner, target)) return;
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier));
        if (!target.canFreeze()) return;
        if (TARGETS.values().stream().filter(l -> l.owners.containsKey(owner.getUUID())).count() >= 32
                && !TARGETS.containsKey(target.getUUID())) return;
        if (TARGETS.size() >= 512 && !TARGETS.containsKey(target.getUUID())) return;
        Lease lease = TARGETS.computeIfAbsent(target.getUUID(), id -> new Lease(target, new HashMap<>()));
        lease.owners.put(owner.getUUID(), target.level().getGameTime() + duration);
        target.setTicksFrozen(Math.max(target.getTicksFrozen(), target.getTicksRequiredToFreeze() + 20));
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        TARGETS.values().removeIf(lease -> {
            lease.owners.values().removeIf(until -> until <= lease.target.level().getGameTime());
            if (lease.owners.isEmpty() || !lease.target.isAlive() || lease.target.isRemoved()) return true;
            lease.target.setTicksFrozen(Math.max(lease.target.getTicksFrozen(), lease.target.getTicksRequiredToFreeze() + 20));
            return false;
        });
    }
    public static void cancel(ServerPlayer owner) { TARGETS.values().forEach(l -> l.owners.remove(owner.getUUID())); TARGETS.values().removeIf(l -> l.owners.isEmpty()); }
    public static void clear() { TARGETS.clear(); }
    public static void unload(net.minecraft.world.level.Level level) { TARGETS.values().removeIf(l -> l.target.level() == level); }
}
