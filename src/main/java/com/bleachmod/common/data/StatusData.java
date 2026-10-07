package com.bleachmod.common.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StatusData {
    private long lastActionTick = Long.MIN_VALUE;
    private boolean cooldownsDisabled;
    private int[] hudCooldowns = new int[4];
    private boolean hudCooldownsDisabled;

    /** Separate owner-only network snapshot, never written by save(). */
    public CompoundTag techniqueHud() {
        CompoundTag tag = new CompoundTag();
        tag.putIntArray("cooldowns", new int[]{techniqueSlot1CooldownTicks, techniqueSlot2CooldownTicks,
                techniqueSlot3CooldownTicks, techniqueSlot4CooldownTicks});
        tag.putBoolean("disabled", cooldownsDisabled);
        return tag;
    }
    public void readTechniqueHud(CompoundTag tag) {
        int[] values = tag.getIntArray("cooldowns");
        if (values.length != 4) return;
        hudCooldowns = java.util.Arrays.stream(values).map(value -> Math.max(0, value)).toArray();
        hudCooldownsDisabled = tag.getBoolean("disabled");
    }
    public int[] getHudCooldowns() { return hudCooldowns.clone(); }
    public boolean areHudCooldownsDisabled() { return hudCooldownsDisabled; }

    public boolean areCooldownsDisabled() { return cooldownsDisabled; }
    public void setCooldownsDisabled(boolean disabled) {
        cooldownsDisabled = disabled;
        clearCooldowns();
    }
    public void clearCooldowns() {
        techniqueSlot1CooldownTicks = 0;
        techniqueSlot2CooldownTicks = 0;
        techniqueSlot3CooldownTicks = 0;
        techniqueSlot4CooldownTicks = 0;
    }
    private int techniqueSlot1CooldownTicks;
    private int techniqueSlot2CooldownTicks;
    private int techniqueSlot3CooldownTicks;
    private int techniqueSlot4CooldownTicks;
    private int flameDashTicks;
    private boolean ignitionActive;
    private final Set<UUID> flameDashHitEntities = new HashSet<>();
    private final Set<BlockPos> flameBarrageGroundPositions = new HashSet<>();
    private int flameBarrageGroundTicks;
    private final Set<BlockPos> flameFanGroundPositions = new HashSet<>();
    private int flameFanGroundTicks;

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
        techniqueSlot1CooldownTicks = cooldownsDisabled ? 0 : Math.max(0, ticks);
    }

    public int getTechniqueSlot2CooldownTicks() {
        return techniqueSlot2CooldownTicks;
    }

    public void setTechniqueSlot2CooldownTicks(int ticks) {
        techniqueSlot2CooldownTicks = cooldownsDisabled ? 0 : Math.max(0, ticks);
    }

    public int getTechniqueSlot4CooldownTicks() {
        return techniqueSlot4CooldownTicks;
    }

    public int getTechniqueSlot3CooldownTicks() {
        return techniqueSlot3CooldownTicks;
    }

    public void setTechniqueSlot3CooldownTicks(int ticks) {
        techniqueSlot3CooldownTicks = cooldownsDisabled ? 0 : Math.max(0, ticks);
    }

    /** Changing form cancels active effects but must not allow a cooldown bypass. */
    public void clearTransformationState() {
        int slot1 = techniqueSlot1CooldownTicks;
        int slot2 = techniqueSlot2CooldownTicks;
        int slot3 = techniqueSlot3CooldownTicks;
        int slot4 = techniqueSlot4CooldownTicks;
        clearTransientState();
        techniqueSlot1CooldownTicks = slot1;
        techniqueSlot2CooldownTicks = slot2;
        techniqueSlot3CooldownTicks = slot3;
        techniqueSlot4CooldownTicks = slot4;
    }

    public void setTechniqueSlot4CooldownTicks(int ticks) {
        techniqueSlot4CooldownTicks = cooldownsDisabled ? 0 : Math.max(0, ticks);
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

    public int getFlameFanGroundTicks() {
        return flameFanGroundTicks;
    }

    public void setFlameFanGroundTicks(int ticks) {
        flameFanGroundTicks = Math.max(0, ticks);
        if (flameFanGroundTicks == 0) {
            flameFanGroundPositions.clear();
        }
    }

    public void setFlameFanGroundPositions(Set<BlockPos> positions, int ticks) {
        flameFanGroundPositions.clear();
        flameFanGroundPositions.addAll(positions);
        flameFanGroundTicks = Math.max(0, ticks);
    }

    public Set<BlockPos> getFlameFanGroundPositions() {
        return Set.copyOf(flameFanGroundPositions);
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

    /** Clears technique test state without changing form, reiatsu, ignition or progression. */
    public void clearTechniqueTestState() {
        lastActionTick = Long.MIN_VALUE;
        techniqueSlot1CooldownTicks = 0;
        techniqueSlot2CooldownTicks = 0;
        techniqueSlot3CooldownTicks = 0;
        techniqueSlot4CooldownTicks = 0;
        flameDashTicks = 0;
        flameDashHitEntities.clear();
        flameBarrageGroundTicks = 0;
        flameBarrageGroundPositions.clear();
        flameFanGroundTicks = 0;
        flameFanGroundPositions.clear();
    }

    public void tickTransientState() {
        if (techniqueSlot3CooldownTicks > 0) {
            techniqueSlot3CooldownTicks--;
        }

        if (techniqueSlot1CooldownTicks > 0) {
            techniqueSlot1CooldownTicks--;
        }
        if (techniqueSlot2CooldownTicks > 0) {
            techniqueSlot2CooldownTicks--;
        }
        if (techniqueSlot4CooldownTicks > 0) {
            techniqueSlot4CooldownTicks--;
        }
        if (flameBarrageGroundTicks > 0) {
            flameBarrageGroundTicks--;
            if (flameBarrageGroundTicks == 0) {
                flameBarrageGroundPositions.clear();
            }
        }
        if (flameFanGroundTicks > 0) {
            flameFanGroundTicks--;
            if (flameFanGroundTicks == 0) {
                flameFanGroundPositions.clear();
            }
        }
    }

    public void clearTransientState() {
        techniqueSlot1CooldownTicks = 0;
        techniqueSlot2CooldownTicks = 0;
        techniqueSlot3CooldownTicks = 0;
        techniqueSlot4CooldownTicks = 0;
        flameBarrageGroundTicks = 0;
        flameBarrageGroundPositions.clear();
        flameFanGroundTicks = 0;
        flameFanGroundPositions.clear();
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
        cooldownsDisabled = false;
        if (tag.contains("hasCreatedCharacter")) {
            hasCreatedCharacter = tag.getBoolean("hasCreatedCharacter");
        }
        if (tag.contains("actionCharging")) {
            actionCharging = tag.getBoolean("actionCharging");
        }
        // Cooldowns, contact targets and temporary flame visuals are intentionally transient and are not loaded from NBT.
        techniqueSlot1CooldownTicks = 0;
        techniqueSlot2CooldownTicks = 0;
        techniqueSlot3CooldownTicks = 0;
        techniqueSlot4CooldownTicks = 0;
        flameBarrageGroundTicks = 0;
        flameBarrageGroundPositions.clear();
        flameFanGroundTicks = 0;
        flameFanGroundPositions.clear();
        flameDashTicks = 0;
        flameDashHitEntities.clear();
    }
}
