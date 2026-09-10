package com.bleachmod.common.network.s2c;

import com.bleachmod.client.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ActionFeedbackS2C(String json) {
    public static ActionFeedbackS2C of(Component component) {
        return new ActionFeedbackS2C(Component.Serializer.toJson(component));
    }

    public static void encode(ActionFeedbackS2C msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.json);
    }

    public static ActionFeedbackS2C decode(FriendlyByteBuf buf) {
        return new ActionFeedbackS2C(buf.readUtf());
    }

    public static void handle(ActionFeedbackS2C msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleFeedback(msg)));
        ctx.get().setPacketHandled(true);
    }
}
