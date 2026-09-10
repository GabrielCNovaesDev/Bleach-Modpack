package com.bleachmod.common.network.s2c;

import com.bleachmod.client.network.ClientPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PlayerSyncS2C(int playerId, CompoundTag nbt) {
    public static void encode(PlayerSyncS2C msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.playerId);
        buf.writeNbt(msg.nbt);
    }

    public static PlayerSyncS2C decode(FriendlyByteBuf buf) {
        return new PlayerSyncS2C(buf.readVarInt(), buf.readNbt());
    }

    public static void handle(PlayerSyncS2C msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handlePlayerSync(msg)));
        ctx.get().setPacketHandled(true);
    }
}
