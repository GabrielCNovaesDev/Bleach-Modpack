package com.bleachmod.common.network.c2s;

import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.technique.TechniqueService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ExecuteTechniqueSlotC2S(int slot) {
    public static void encode(ExecuteTechniqueSlotC2S message, FriendlyByteBuf buffer) {
        buffer.writeByte(message.slot);
    }

    public static ExecuteTechniqueSlotC2S decode(FriendlyByteBuf buffer) {
        return new ExecuteTechniqueSlotC2S(buffer.readUnsignedByte());
    }

    public static void handle(ExecuteTechniqueSlotC2S message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || message.slot < 1 || message.slot > 4) {
                return;
            }
            PlayerCapability.get(player).ifPresent(data -> {
                if (data.getStatus().allowAction(player.level().getGameTime())) {
                    TechniqueService.executeSlot(player, data, message.slot);
                }
            });
        });
        context.setPacketHandled(true);
    }
}
