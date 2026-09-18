package com.bleachmod.common.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StatusData {
    private long lastActionTick = Long.MIN_VALUE;
    private int flameBurstCooldownTicks;
    private int techniqueSlot1CooldownTicks;
    private int techniqueSlot2CooldownTicks;
    private int flameDashTicks;
    private boolean ignitionActive;
    private final Set<UUID> flameDashHitEntities = new HashSet<>();
    private final Set<BlockPos> flameBarrageGroundPositions = new HashSet<>();
    private int flameBarrageGroundTicks;

    public boolean allowAction(long tick) {
        if (lastActionTick != Long.MIN_VALUE && tick >= lastActionTick && tick - lastActionTick < 4) return false;
        lastActionTick = tick;
        return true;
    }

    private boolean hasCreatedCharacter;
    private boolean actionCharging;

    public boolean hasCreatedCharacter() {
        return hasCreatedCharacter;
    }

    public void setHasCreatedCharacter(boolean hasCreatedCharacter) {
        this.hasCreatedCharacter = hasCreatedCharacter;
    }

    public boolean isActionCharging() {
        return actionCharging;
    }

    public void setActionCharging(boolean actionCharging) {
        this.actionCharging = actionCharging;
    }

    public int getFlameBurstCooldownTicks() {
        return flameBurstCooldownTicks;
    }

    public void setFlameBurstCooldownTicks(int ticks) {
        flameBurstCooldownTicks = Math.max(0, ticks);
    }

    public boolean isIgnitionActive() {
        return ignitionActive;
    }

    public void setIgnitionActive(boolean active) {
        ignitionActive = active;
    }

    public int getTechniqueSlot1CooldownTicks() {
        return techniqueSlot1CooldownTicks;
    }

    public void setTechniqueSlot1CooldownTicks(int ticks) {
        techniqueSlot1CooldownTicks = Math.max(0, ticks);
    }

    public int getTechniqueSlot2CooldownTicks() {
        return techniqueSlot2CooldownTicks;
    }

    public void setTechniqueSlot2CooldownTicks(int ticks) {
        techniqueSlot2CooldownTicks = Math.max(0, ticks);
    }

    public int getFlameBarrageGroundTicks() {
        return flameBarrageGroundTicks;
    }

    public void setFlameBarrageGroundTicks(int ticks) {
        flameBarrageGroundTicks = Math.max(0, ticks);
        if (flameBarrageGroundTicks == 0) {
            flameBarrageGroundPositions.clear();
        }
    }

    public void setFlameBarrageGroundPositions(Set<BlockPos> positions, int ticks) {
        flameBarrageGroundPositions.clear();
        flameBarrageGroundPositions.addAll(positions);
        flameBarrageGroundTicks = Math.max(0, ticks);
    }

    public Set<BlockPos> getFlameBarrageGroundPositions() {
        return Set.copyOf(flameBarrageGroundPositions);
    }

    public int getFlameDashTicks() {
        return flameDashTicks;
    }

    public void setFlameDashTicks(int ticks) {
        flameDashTicks = Math.max(0, ticks);
    }

    public void beginFlameDash() {
        flameDashHitEntities.clear();
    }

    public boolean markFlameDashHit(UUID entityId) {
        return flameDashHitEntities.add(entityId);
    }

    public void clearFlameDashHits() {
        flameDashHitEntities.clear();
    }

    public void tickTransientState() {
        if (flameBurstCooldownTicks > 0) {
            flameBurstCooldownTicks--;
        }
        if (techniqueSlot1CooldownTicks > 0) {
            techniqueSlot1CooldownTicks--;
        }
        if (techniqueSlot2CooldownTicks > 0) {
            techniqueSlot2CooldownTicks--;
        }
        if (flameBarrageGroundTicks > 0) {
            flameBarrageGroundTicks--;
            if (flameBarrageGroundTicks == 0) {
                flameBarrageGroundPositions.clear();
            }
        }
    }

    public void clearTransientState() {
        flameBurstCooldownTicks = 0;
        techniqueSlot1CooldownTicks = 0;
        techniqueSlot2CooldownTicks = 0;
        flameBarrageGroundTicks = 0;
        flameBarrageGroundPositions.clear();
        flameDashTicks = 0;
        ignitionActive = false;
        actionCharging = false;
        flameDashHitEntities.clear();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("hasCreatedCharacter", hasCreatedCharacter);
        tag.putBoolean("actionCharging", actionCharging);
        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.contains("hasCreatedCharacter")) {
            hasCreatedCharacter = tag.getBoolean("hasCreatedCharacter");
        }
        if (tag.contains("actionCharging")) {
            actionCharging = tag.getBoolean("actionCharging");
        }
        // Cooldowns, contact targets and temporary flame visuals are intentionally transient and are not loaded from NBT.
        flameBurstCooldownTicks = 0;
        techniqueSlot1CooldownTicks = 0;
        techniqueSlot2CooldownTicks = 0;
        flameBarrageGroundTicks = 0;
        flameBarrageGroundPositions.clear();
        flameDashTicks = 0;
        flameDashHitEntities.clear();
    }
}
