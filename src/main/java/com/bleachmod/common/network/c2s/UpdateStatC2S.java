package com.bleachmod.common.network.c2s;

import com.bleachmod.common.data.PlayerCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UpdateStatC2S(StatAction action, boolean value) {
    public enum StatAction {
        ACTION_CHARGE
    }

    public static void encode(UpdateStatC2S msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
        buf.writeBoolean(msg.value);
    }

    public static UpdateStatC2S decode(FriendlyByteBuf buf) {
        return new UpdateStatC2S(buf.readEnum(StatAction.class), buf.readBoolean());
    }

    public static void handle(UpdateStatC2S msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            PlayerCapability.get(player).ifPresent(data -> {
                if (msg.action == StatAction.ACTION_CHARGE) {
                    data.getStatus().setActionCharging(msg.value);
                    if (!msg.value) {
                        data.getResources().setActionCharge(0);
                    }
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
