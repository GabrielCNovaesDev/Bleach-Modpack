package com.bleachmod.common.network.c2s;

import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.technique.TechniqueService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ExecuteTechniqueC2S(String techniqueId) {
    public static void encode(ExecuteTechniqueC2S message, FriendlyByteBuf buffer) {
        buffer.writeUtf(message.techniqueId, 64);
    }

    public static ExecuteTechniqueC2S decode(FriendlyByteBuf buffer) {
        return new ExecuteTechniqueC2S(buffer.readUtf(64));
    }

    public static void handle(ExecuteTechniqueC2S message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !TechniqueService.FLAME_BURST_ID.equals(message.techniqueId)) {
                return;
            }
            PlayerCapability.get(player).ifPresent(data -> {
                if (data.getStatus().allowAction(player.level().getGameTime())) {
                    TechniqueService.executeFlameBurst(player, data);
                }
            });
        });
        context.setPacketHandled(true);
    }
}
