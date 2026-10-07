package com.bleachmod.entity;

import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.technique.HyorinmaruTechniqueService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/** A cosmetic ice familiar using the native Phantom skeleton, with no AI, hitbox damage or persistence. */
public final class IceDragonEntity extends Phantom {
    private UUID owner;
    private net.minecraft.server.level.ServerPlayer caster;
    private Vec3 origin = Vec3.ZERO, direction = Vec3.ZERO;
    private int life;
    public IceDragonEntity(EntityType<? extends Phantom> type, Level level) {
        super(type, level); setNoAi(true); setNoGravity(true); noPhysics = true; setSilent(true);
    }
    public void configure(net.minecraft.server.level.ServerPlayer p, Vec3 aim, boolean bankai) {
        owner = p.getUUID(); caster = p; origin = p.position().add(aim.scale(1.5)).add(0, 1.2, 0); direction = aim;
        life = bankai ? 20 : 14; setPhantomSize(bankai ? 4 : 2); setPos(origin); setYRot(p.getYRot()); setYBodyRot(p.getYRot());
    }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        var p = caster;
        if (p == null || p.level() != level() || !p.isAlive() || tickCount >= life
                || !PlayerCapability.get(p).map(d -> HyorinmaruTechniqueService.equipped(p, d) && !"sealed".equals(d.getCharacter().getActiveForm())).orElse(false)) {
            discard(); return;
        }
        Vec3 next = origin.add(direction.scale(tickCount * 0.7));
        if (!level().hasChunkAt(net.minecraft.core.BlockPos.containing(next))) { discard(); return; }
        setPos(next); setDeltaMovement(Vec3.ZERO);
        ((ServerLevel) level()).sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE, getX(), getY(), getZ(), 10, 0.5, 0.4, 0.5, 0.03);
    }
    @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) { return false; }
    @Override public boolean isPickable() { return false; }
    @Override public boolean isPushable() { return false; }
}
