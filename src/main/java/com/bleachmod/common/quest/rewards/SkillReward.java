package com.bleachmod.common.quest.rewards;

import com.bleachmod.common.data.PlayerData;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SkillReward extends QuestReward {
    private final String skill;
    private final int level;

    public SkillReward(String skill, int level) {
        this.skill = skill;
        this.level = level;
    }

    @Override
    public void give(ServerPlayer player, PlayerData data) {
        data.updateTransformationSkillLimits(data.getCharacter().getRace());
        data.getSkills().setSkillLevel(skill, level);
    }

    @Override
    public Component describe() {
        return Component.translatable("bleachmod.reward.skill", skill, level);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "SKILL");
        json.addProperty("skill", skill);
        json.addProperty("level", level);
        return json;
    }
}
