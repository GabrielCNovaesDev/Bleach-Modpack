package com.bleachmod.common.network.c2s;

import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.server.events.FormModeHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ExecuteActionC2S(ActionType action) {
    public enum ActionType {
        INSTANT_TRANSFORM,
        FORCE_DESCEND
    }

    public static void encode(ExecuteActionC2S msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
    }

    public static ExecuteActionC2S decode(FriendlyByteBuf buf) {
        return new ExecuteActionC2S(buf.readEnum(ActionType.class));
    }

    public static void handle(ExecuteActionC2S msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            PlayerCapability.get(player).ifPresent(data -> {
                if (msg.action == ActionType.INSTANT_TRANSFORM) {
                    FormModeHandler.instantTransform(player, data);
                } else if (msg.action == ActionType.FORCE_DESCEND) {
                    FormModeHandler.descend(player, data);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
