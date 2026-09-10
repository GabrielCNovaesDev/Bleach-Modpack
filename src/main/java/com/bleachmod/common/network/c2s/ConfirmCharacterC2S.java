package com.bleachmod.common.network.c2s;

import com.bleachmod.Reference;
import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.init.ModItems;
import com.bleachmod.common.network.SyncHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ConfirmCharacterC2S(String race) {
    public static void encode(ConfirmCharacterC2S msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.race);
    }

    public static ConfirmCharacterC2S decode(FriendlyByteBuf buf) {
        return new ConfirmCharacterC2S(buf.readUtf());
    }

    public static void handle(ConfirmCharacterC2S msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            PlayerCapability.get(player).ifPresent(data -> {
                if (data.getStatus().hasCreatedCharacter()) {
                    return;
                }
                if (!Reference.RACE_SHINIGAMI.equalsIgnoreCase(msg.race)) {
                    return;
                }
                data.initializeShinigami();
                ItemStack asauchi = new ItemStack(ModItems.ASAUCHI.get());
                if (!player.getInventory().contains(asauchi)) {
                    if (!player.getInventory().add(asauchi)) {
                        player.drop(asauchi, false);
                    }
                }
                SyncHelper.full(player);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
