package com.bleachmod.common.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
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
        // Reserved legacy packet registration: retired X cannot execute or consume resources.
        // Keep its discriminator so subsequent packet IDs do not move.
        context.setPacketHandled(true);
    }
}
