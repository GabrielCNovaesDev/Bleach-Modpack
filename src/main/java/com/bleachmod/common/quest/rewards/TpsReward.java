package com.bleachmod.common.quest.rewards;

import com.bleachmod.common.data.PlayerData;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class TpsReward extends QuestReward {
    private final float amount;

    public TpsReward(float amount) {
        this.amount = amount;
    }

    @Override
    public void give(ServerPlayer player, PlayerData data) {
        data.getResources().addTrainingPoints(amount);
    }

    @Override
    public Component describe() {
        return Component.translatable("bleachmod.reward.tps", (int) amount);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "TPS");
        json.addProperty("amount", amount);
        return json;
    }
}
