package com.bleachmod.common.quest.rewards;

import com.bleachmod.common.data.PlayerData;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public abstract class QuestReward {
    public abstract void give(ServerPlayer player, PlayerData data);

    public abstract Component describe();

    public abstract JsonObject toJson();
}
