package com.bleachmod.common.data;

import net.minecraft.nbt.CompoundTag;

public class StatusData {
    private long lastActionTick = Long.MIN_VALUE;
    private int flameBurstCooldownTicks;

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

    public void tickTransientState() {
        if (flameBurstCooldownTicks > 0) {
            flameBurstCooldownTicks--;
        }
    }

    public void clearTransientState() {
        flameBurstCooldownTicks = 0;
        actionCharging = false;
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
        // Cooldowns are intentionally transient and are not loaded from NBT.
        flameBurstCooldownTicks = 0;
    }
}
