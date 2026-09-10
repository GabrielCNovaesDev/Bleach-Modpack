package com.bleachmod.common.network.c2s;

import com.bleachmod.common.quest.QuestService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record QuestActionC2S(Action action, String questId) {
    public enum Action {
        START
    }

    public static void encode(QuestActionC2S msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
        buf.writeUtf(msg.questId);
    }

    public static QuestActionC2S decode(FriendlyByteBuf buf) {
        return new QuestActionC2S(buf.readEnum(Action.class), buf.readUtf());
    }

    public static void handle(QuestActionC2S msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            if (msg.action == Action.START) {
                QuestService.startQuest(player, msg.questId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
