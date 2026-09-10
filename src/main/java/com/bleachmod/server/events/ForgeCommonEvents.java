package com.bleachmod.server.events;

import com.bleachmod.Reference;
import com.bleachmod.common.evolution.FormRegistry;
import com.bleachmod.common.network.SyncHelper;
import com.bleachmod.common.quest.QuestRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ForgeCommonEvents {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        QuestRegistry.loadAll(event.getServer());
        FormRegistry.loadAll(event.getServer());
    }
}
