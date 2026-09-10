package com.bleachmod.common.network.s2c;

import com.bleachmod.client.network.ClientPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ProgressionSyncS2C(int playerId, CompoundTag nbt) {
    public static void encode(ProgressionSyncS2C msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.playerId);
        buf.writeNbt(msg.nbt);
    }

    public static ProgressionSyncS2C decode(FriendlyByteBuf buf) {
        return new ProgressionSyncS2C(buf.readVarInt(), buf.readNbt());
    }

    public static void handle(ProgressionSyncS2C msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleProgressionSync(msg)));
        ctx.get().setPacketHandled(true);
    }
}
