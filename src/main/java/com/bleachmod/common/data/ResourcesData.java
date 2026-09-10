package com.bleachmod.common.data;

import com.bleachmod.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class ResourcesData {
    private float currentReiatsu = Reference.BASE_REIATSU;
    private float maxReiatsu = Reference.BASE_REIATSU;
    private int actionCharge;
    private float trainingPoints;

    public float getCurrentReiatsu() {
        return currentReiatsu;
    }

    public void setCurrentReiatsu(float value) {
        this.currentReiatsu = Mth.clamp(value, 0.0F, maxReiatsu);
    }

    public void addReiatsu(float amount) {
        setCurrentReiatsu(currentReiatsu + amount);
    }

    public boolean consumeReiatsu(float amount) {
        if (currentReiatsu < amount) {
            return false;
        }
        setCurrentReiatsu(currentReiatsu - amount);
        return true;
    }

    public float getMaxReiatsu() {
        return maxReiatsu;
    }

    public void setMaxReiatsu(float maxReiatsu) {
        this.maxReiatsu = Math.max(1.0F, maxReiatsu);
        setCurrentReiatsu(currentReiatsu);
    }

    public int getActionCharge() {
        return actionCharge;
    }

    public void setActionCharge(int actionCharge) {
        this.actionCharge = Mth.clamp(actionCharge, 0, 100);
    }

    public void addActionCharge(int amount) {
        setActionCharge(actionCharge + amount);
    }

    public float getTrainingPoints() {
        return trainingPoints;
    }

    public void addTrainingPoints(float amount) {
        this.trainingPoints = Math.max(0.0F, trainingPoints + amount);
    }

    public boolean consumeTrainingPoints(float amount) {
        if (trainingPoints < amount) {
            return false;
        }
        trainingPoints -= amount;
        return true;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("currentReiatsu", currentReiatsu);
        tag.putFloat("maxReiatsu", maxReiatsu);
        tag.putInt("actionCharge", actionCharge);
        tag.putFloat("trainingPoints", trainingPoints);
        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.contains("currentReiatsu")) {
            currentReiatsu = tag.getFloat("currentReiatsu");
        }
        if (tag.contains("maxReiatsu")) {
            maxReiatsu = tag.getFloat("maxReiatsu");
        }
        if (tag.contains("actionCharge")) {
            actionCharge = tag.getInt("actionCharge");
        }
        if (tag.contains("trainingPoints")) {
            trainingPoints = tag.getFloat("trainingPoints");
        }
        setCurrentReiatsu(currentReiatsu);
    }
}
