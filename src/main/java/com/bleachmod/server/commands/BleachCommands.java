package com.bleachmod.server.commands;

import com.bleachmod.Reference;
import com.bleachmod.common.evolution.FormRegistry;
import com.bleachmod.common.network.SyncHelper;
import com.bleachmod.common.quest.QuestRegistry;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class BleachCommands {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("bleachreload")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("quests").executes(ctx -> {
                    QuestRegistry.loadAll(ctx.getSource().getServer());
                    FormRegistry.loadAll(ctx.getSource().getServer());
                    for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                        SyncHelper.questRegistry(player);
                    }
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.bleachmod.reload.quests"), true);
                    return 1;
                })));
    }
}
