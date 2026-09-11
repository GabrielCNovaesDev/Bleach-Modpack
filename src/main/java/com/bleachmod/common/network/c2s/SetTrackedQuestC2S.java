package com.bleachmod.common.network.c2s;

import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.network.SyncHelper;
import com.bleachmod.common.quest.QuestStatus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SetTrackedQuestC2S(String questId) {
    public static void encode(SetTrackedQuestC2S msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.questId == null ? "" : msg.questId);
    }

    public static SetTrackedQuestC2S decode(FriendlyByteBuf buf) {
        return new SetTrackedQuestC2S(buf.readUtf(256));
    }

    public static void handle(SetTrackedQuestC2S msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            PlayerCapability.get(player).ifPresent(data -> {
                if (!data.getStatus().hasCreatedCharacter() || !player.isAlive() || !data.getStatus().allowAction(player.level().getGameTime())) return;
                String id = msg.questId == null || msg.questId.isBlank() ? null : msg.questId;
                if (id != null && data.getPlayerQuestData().getStatus(id) != QuestStatus.ACCEPTED) {
                    return;
                }
                data.getPlayerQuestData().setTrackedQuestId(id);
                SyncHelper.progression(player);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
