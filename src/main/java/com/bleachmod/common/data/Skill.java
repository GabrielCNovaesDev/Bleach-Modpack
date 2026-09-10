package com.bleachmod.common.data;

import net.minecraft.nbt.CompoundTag;

public class Skill {
    private String name;
    private int level;
    private boolean active;
    private int maxLevel;

    public Skill(String name, int maxLevel) {
        this.name = name;
        this.maxLevel = Math.max(0, maxLevel);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(0, Math.min(level, maxLevel));
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = Math.max(0, maxLevel);
        setLevel(level);
    }

    public boolean isUnlockedAtLevel(int required) {
        return level >= required;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putInt("level", level);
        tag.putBoolean("active", active);
        tag.putInt("maxLevel", maxLevel);
        return tag;
    }

    public static Skill load(CompoundTag tag) {
        Skill skill = new Skill(tag.getString("name"), tag.getInt("maxLevel"));
        skill.setLevel(tag.getInt("level"));
        skill.setActive(tag.getBoolean("active"));
        return skill;
    }
}
