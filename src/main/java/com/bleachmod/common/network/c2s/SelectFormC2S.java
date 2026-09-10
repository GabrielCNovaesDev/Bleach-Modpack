package com.bleachmod.common.network.c2s;

import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.evolution.TransformationsHelper;
import com.bleachmod.common.network.SyncHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SelectFormC2S(String group, String form) {
    public static void encode(SelectFormC2S msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.group);
        buf.writeUtf(msg.form);
    }

    public static SelectFormC2S decode(FriendlyByteBuf buf) {
        return new SelectFormC2S(buf.readUtf(), buf.readUtf());
    }

    public static void handle(SelectFormC2S msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            PlayerCapability.get(player).ifPresent(data -> {
                if (!TransformationsHelper.isSelectable(data, msg.group, msg.form)) {
                    return;
                }
                data.getCharacter().setSelectedForm(msg.group, msg.form);
                SyncHelper.appearance(player);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
