package com.bleachmod.common.network.s2c;

import com.bleachmod.client.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record OpenNpcQuestScreenS2C(String npcId, int entityId) {
    public static void encode(OpenNpcQuestScreenS2C msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.npcId, 64);
        buf.writeInt(msg.entityId);
    }

    public static OpenNpcQuestScreenS2C decode(FriendlyByteBuf buf) {
        return new OpenNpcQuestScreenS2C(buf.readUtf(64), buf.readInt());
    }

    public static void handle(OpenNpcQuestScreenS2C msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandler.handleOpenNpcQuestScreen(msg)));
        ctx.get().setPacketHandled(true);
    }
}
