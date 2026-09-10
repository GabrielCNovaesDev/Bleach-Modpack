package com.bleachmod.common.network.s2c;

import com.bleachmod.client.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncQuestRegistryS2C(String sagasJson, String questsJson, String formsJson) {
    public static void encode(SyncQuestRegistryS2C msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.sagasJson, 1_000_000);
        buf.writeUtf(msg.questsJson, 1_000_000);
        buf.writeUtf(msg.formsJson, 262144);
    }

    public static SyncQuestRegistryS2C decode(FriendlyByteBuf buf) {
        return new SyncQuestRegistryS2C(buf.readUtf(1_000_000), buf.readUtf(1_000_000), buf.readUtf(262144));
    }

    public static void handle(SyncQuestRegistryS2C msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleQuestRegistry(msg)));
        ctx.get().setPacketHandled(true);
    }
}
