package com.bleachmod.common.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class SkillsData {
    private final Map<String, Skill> skills = new LinkedHashMap<>();

    public Skill get(String name) {
        return skills.get(key(name));
    }

    public void registerOrUpdate(String name, int maxLevel) {
        String id = key(name);
        Skill existing = skills.get(id);
        if (existing == null) {
            skills.put(id, new Skill(name, maxLevel));
        } else {
            existing.setMaxLevel(maxLevel);
        }
    }

    public void setSkillLevel(String name, int level) {
        Skill skill = get(name);
        if (skill == null) {
            return;
        }
        if (level > skill.getLevel()) {
            skill.setLevel(level);
        }
    }

    public boolean isUnlockedAtLevel(String name, int required) {
        Skill skill = get(name);
        return skill != null && skill.isUnlockedAtLevel(required);
    }

    public int getLevel(String name) {
        Skill skill = get(name);
        return skill == null ? 0 : skill.getLevel();
    }

    public int skillCostForNextLevel(String name) {
        Skill skill = get(name);
        if (skill == null || skill.getLevel() >= skill.getMaxLevel()) {
            return Integer.MAX_VALUE;
        }
        int next = skill.getLevel() + 1;
        return next == 1 ? 200 : next == 2 ? 500 : 1000;
    }

    public boolean tryPurchase(String name, ResourcesData resources) {
        Skill skill = get(name);
        if (skill == null || skill.getLevel() >= skill.getMaxLevel()) {
            return false;
        }
        int cost = skillCostForNextLevel(name);
        if (!resources.consumeTrainingPoints(cost)) {
            return false;
        }
        skill.setLevel(skill.getLevel() + 1);
        return true;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Skill skill : skills.values()) {
            list.add(skill.save());
        }
        tag.put("skillsList", list);
        return tag;
    }

    public void load(CompoundTag tag) {
        if (!tag.contains("skillsList")) {
            return;
        }
        skills.clear();
        ListTag list = tag.getList("skillsList", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            Skill skill = Skill.load(list.getCompound(i));
            skills.put(key(skill.getName()), skill);
        }
    }

    private static String key(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT);
    }
}
