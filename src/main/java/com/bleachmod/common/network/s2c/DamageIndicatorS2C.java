package com.bleachmod.common.network.s2c;

import com.bleachmod.client.hud.DamageHud;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record DamageIndicatorS2C(float damage) {
    public static void encode(DamageIndicatorS2C msg, FriendlyByteBuf buf) { buf.writeFloat(msg.damage); }
    public static DamageIndicatorS2C decode(FriendlyByteBuf buf) { return new DamageIndicatorS2C(buf.readFloat()); }
    public static void handle(DamageIndicatorS2C msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> DamageHud.show(msg.damage)));
        context.get().setPacketHandled(true);
    }
}
