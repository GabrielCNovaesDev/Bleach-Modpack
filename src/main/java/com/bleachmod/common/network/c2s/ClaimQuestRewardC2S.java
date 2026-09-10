package com.bleachmod.common.network.c2s;

import com.bleachmod.common.quest.QuestService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClaimQuestRewardC2S(String questId, int rewardIndex) {
    public static void encode(ClaimQuestRewardC2S msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.questId);
        buf.writeVarInt(msg.rewardIndex);
    }

    public static ClaimQuestRewardC2S decode(FriendlyByteBuf buf) {
        return new ClaimQuestRewardC2S(buf.readUtf(), buf.readVarInt());
    }

    public static void handle(ClaimQuestRewardC2S msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                QuestService.claimReward(player, msg.questId, msg.rewardIndex);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
