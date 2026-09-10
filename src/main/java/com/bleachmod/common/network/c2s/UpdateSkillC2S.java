package com.bleachmod.common.network.c2s;

import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.common.network.SyncHelper;
import com.bleachmod.common.network.s2c.ActionFeedbackS2C;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UpdateSkillC2S(String skill, SkillAction action) {
    public enum SkillAction {
        PURCHASE
    }

    public static void encode(UpdateSkillC2S msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.skill);
        buf.writeEnum(msg.action);
    }

    public static UpdateSkillC2S decode(FriendlyByteBuf buf) {
        return new UpdateSkillC2S(buf.readUtf(), buf.readEnum(SkillAction.class));
    }

    public static void handle(UpdateSkillC2S msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            PlayerCapability.get(player).ifPresent(data -> {
                if (msg.action == SkillAction.PURCHASE && data.getSkills().tryPurchase(msg.skill, data.getResources())) {
                    SyncHelper.full(player);
                } else {
                    NetworkHandler.sendToPlayer(ActionFeedbackS2C.of(Component.translatable("message.bleachmod.skill.cannot_purchase")), player);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
