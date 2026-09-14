package com.bleachmod.common.network;

import com.bleachmod.server.travel.DimensionTravelService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** Two fixed target IDs; clients never transmit locations, player IDs or prices. */
public final class TravelPackets {
    public record Request(int requestId, String target, int revision) {
        public static void encode(Request msg, FriendlyByteBuf buf) { buf.writeInt(msg.requestId); buf.writeUtf(msg.target, 128); buf.writeInt(msg.revision); }
        public static Request decode(FriendlyByteBuf buf) { return new Request(buf.readInt(), buf.readUtf(128), buf.readInt()); }
        public static void handle(Request msg, Supplier<NetworkEvent.Context> ctx) {
            var context = ctx.get();
            context.enqueueWork(() -> {
                var player = context.getSender();
                if (player != null) DimensionTravelService.get(player.server).request(player, msg);
            });
            context.setPacketHandled(true);
        }
    }
    public record Query() {
        public static void encode(Query msg, FriendlyByteBuf buf) {}
        public static Query decode(FriendlyByteBuf buf) { return new Query(); }
        public static void handle(Query msg, Supplier<NetworkEvent.Context> ctx) {
            var context = ctx.get();
            context.enqueueWork(() -> {
                var player = context.getSender();
                if (player != null) DimensionTravelService.get(player.server).query(player);
            });
            context.setPacketHandled(true);
        }
    }
    public record Catalog(int revision, String target, float cost, int cooldown, String reason) {
        public static void encode(Catalog msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.revision); buf.writeUtf(msg.target,128); buf.writeFloat(msg.cost); buf.writeInt(msg.cooldown); buf.writeUtf(msg.reason,64);
        }
        public static Catalog decode(FriendlyByteBuf buf) { return new Catalog(buf.readInt(),buf.readUtf(128),buf.readFloat(),buf.readInt(),buf.readUtf(64)); }
        public static void handle(Catalog msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.bleachmod.client.gui.SenkaimonScreen.catalog(msg)));
            ctx.get().setPacketHandled(true);
        }
    }
    public record Result(int requestId, String reason) {
        public static void encode(Result msg, FriendlyByteBuf buf) { buf.writeInt(msg.requestId); buf.writeUtf(msg.reason,64); }
        public static Result decode(FriendlyByteBuf buf) { return new Result(buf.readInt(),buf.readUtf(64)); }
        public static void handle(Result msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.bleachmod.client.gui.SenkaimonScreen.result(msg)));
            ctx.get().setPacketHandled(true);
        }
    }
    private TravelPackets() {}
}
