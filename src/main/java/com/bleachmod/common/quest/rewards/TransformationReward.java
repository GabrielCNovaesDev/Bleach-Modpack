package com.bleachmod.common.quest.rewards;

import com.bleachmod.common.data.PlayerData;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class TransformationReward extends QuestReward {
    private final String formGroup;
    private final String formName;
    private final double mastery;

    public TransformationReward(String formGroup, String formName, double mastery) {
        this.formGroup = formGroup;
        this.formName = formName;
        if (!Double.isFinite(mastery) || mastery < 0 || mastery > 100) throw new IllegalArgumentException("Invalid mastery");
        this.mastery = mastery;
    }

    @Override
    public void give(ServerPlayer player, PlayerData data) {
        data.getCharacter().setMastery(formGroup, formName, Math.max(data.getCharacter().getMastery(formGroup, formName), mastery));
        data.getCharacter().unlockForm(formName);
        if (data.getCharacter().getSelectedForm().isEmpty() || "sealed".equals(data.getCharacter().getSelectedForm())) {
            data.getCharacter().setSelectedForm(formGroup, formName);
        }
    }

    @Override
    public Component describe() {
        return Component.translatable("bleachmod.reward.transformation", "", Component.translatable("form.bleachmod." + formName));
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "TRANSFORMATION");
        json.addProperty("formGroup", formGroup);
        json.addProperty("formName", formName);
        json.addProperty("mastery", mastery);
        return json;
    }
}
