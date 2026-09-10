package com.bleachmod.common.network.c2s;
import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.ProgressionService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record PurchaseAttributeC2S(String id) {
    public static void encode(PurchaseAttributeC2S msg,FriendlyByteBuf buf) { buf.writeUtf(msg.id,32); }
    public static PurchaseAttributeC2S decode(FriendlyByteBuf buf) { return new PurchaseAttributeC2S(buf.readUtf(32)); }
    public static void handle(PurchaseAttributeC2S msg,Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(()->{
            var player=context.get().getSender();
            if(player==null||!player.isAlive())return;
            PlayerCapability.get(player).ifPresent(data->{
                if (!data.getStatus().allowAction(player.level().getGameTime())) return;
                if(data.getStatus().hasCreatedCharacter()&&data.getAttributes().purchase(msg.id,data.getResources()))
                    ProgressionService.sync(player,data);
                else player.displayClientMessage(Component.translatable("message.bleachmod.skill.cannot_purchase"),false);
            });
        });
        context.get().setPacketHandled(true);
    }
}
