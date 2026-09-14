package com.bleachmod.common.network.c2s;

import com.bleachmod.common.quest.QuestService;
import com.bleachmod.common.quest.Quest;
import com.bleachmod.common.quest.QuestRegistry;
import com.bleachmod.entity.QuestNpcEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record QuestActionC2S(Action action, String questId, int npcEntityId) {
    public enum Action {
        START
    }

    public static void encode(QuestActionC2S msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
        buf.writeUtf(msg.questId);
        buf.writeInt(msg.npcEntityId);
    }

    public static QuestActionC2S decode(FriendlyByteBuf buf) {
        return new QuestActionC2S(buf.readEnum(Action.class), buf.readUtf(256), buf.readInt());
    }

    public static void handle(QuestActionC2S msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.isAlive() || !com.bleachmod.common.data.PlayerCapability.get(player).map(data -> data.getStatus().hasCreatedCharacter() && data.getStatus().allowAction(player.level().getGameTime())).orElse(false)) {
                return;
            }
            if (msg.action == Action.START) {
                Quest quest = QuestRegistry.getQuest(msg.questId);
                if (quest != null && !quest.getQuestGiver().isBlank()) {
                    net.minecraft.world.entity.Entity entity = player.level().getEntity(msg.npcEntityId);
                    if (!(entity instanceof QuestNpcEntity npc)
                            || !quest.getQuestGiver().equals(npc.getNpcId())
                            || player.distanceToSqr(npc) > 36.0D) {
                        com.bleachmod.common.network.NetworkHandler.sendToPlayer(
                                com.bleachmod.common.network.s2c.ActionFeedbackS2C.of(
                                        net.minecraft.network.chat.Component.translatable("message.bleachmod.quest.npc_required")), player);
                        return;
                    }
                }
                QuestService.startQuest(player, msg.questId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
