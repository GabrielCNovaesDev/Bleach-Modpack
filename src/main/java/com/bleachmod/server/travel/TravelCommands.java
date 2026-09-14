package com.bleachmod.server.travel;

import com.bleachmod.Reference;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import static net.minecraft.commands.Commands.*;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID)
public final class TravelCommands {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(literal("bleachtravel").requires(s->s.hasPermission(2))
            .then(literal("inspect").executes(ctx->{
                ctx.getSource().sendSuccess(()->Component.literal(DimensionTravelService.get(ctx.getSource().getServer()).describe()),false); return 1;
            }))
            .then(literal("reload").executes(ctx->{
                try {
                    DimensionTravelService.get(ctx.getSource().getServer()).reload();
                    ctx.getSource().sendSuccess(()->Component.translatable("bleachmod.travel.reloaded"),true); return 1;
                } catch(Exception e) {
                    ctx.getSource().sendFailure(Component.literal(e.getMessage())); return 0;
                }
            }))
            .then(literal("rescue").then(argument("player",EntityArgument.player()).executes(ctx->{
                DimensionTravelService.get(ctx.getSource().getServer()).rescue(EntityArgument.getPlayer(ctx,"player")); return 1;
            }))));
    }
}
