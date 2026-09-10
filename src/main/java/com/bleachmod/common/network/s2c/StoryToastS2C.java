package com.bleachmod.common.network.s2c;

import com.bleachmod.client.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StoryToastS2C(ToastType type, String questId, int objectiveIndex, int progress, int required) {
    public enum ToastType {
        START,
        OBJECTIVE,
        COMPLETE,
        FAIL,
        CLAIM
    }

    public static void encode(StoryToastS2C msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.type);
        buf.writeUtf(msg.questId == null ? "" : msg.questId);
        buf.writeVarInt(msg.objectiveIndex);
        buf.writeVarInt(msg.progress);
        buf.writeVarInt(msg.required);
    }

    public static StoryToastS2C decode(FriendlyByteBuf buf) {
        return new StoryToastS2C(buf.readEnum(ToastType.class), buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(StoryToastS2C msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleToast(msg)));
        ctx.get().setPacketHandled(true);
    }
}
