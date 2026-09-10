package com.bleachmod.client;

import com.bleachmod.client.input.ModKeybinds;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class BleachClient {
    private BleachClient() {
    }

    public static void init(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        modBus.addListener(BleachClient::registerKeys);
        modBus.addListener(BleachClient::registerOverlays);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ModKeybinds.JOURNAL);
        event.register(ModKeybinds.CHARGE);
        event.register(ModKeybinds.CYCLE_FORM);
        event.register(ModKeybinds.DESCEND);
    }

    private static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("bleach_reiatsu", com.bleachmod.client.hud.ReiatsuHud.OVERLAY);
        event.registerAboveAll("bleach_tracked_quest", com.bleachmod.client.hud.TrackedQuestHud.OVERLAY);
    }
}
