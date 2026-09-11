package com.bleachmod.client;

import com.bleachmod.Reference;
import com.bleachmod.client.input.ModKeybinds;
import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class BleachClient {
    private BleachClient() {
    }

    public static void init(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        modBus.addListener(BleachClient::registerKeys);
        modBus.addListener(BleachClient::registerOverlays);
        modBus.addListener(BleachClient::onClientSetup);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ModKeybinds.JOURNAL);
        event.register(ModKeybinds.STATUS);
        event.register(ModKeybinds.WHEEL);
        event.register(ModKeybinds.CHARGE);
        event.register(ModKeybinds.CYCLE_FORM);
        event.register(ModKeybinds.DESCEND);
    }

    private static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("bleach_reiatsu", com.bleachmod.client.hud.ReiatsuHud.OVERLAY);
        event.registerAboveAll("bleach_tracked_quest", com.bleachmod.client.hud.TrackedQuestHud.OVERLAY);
        event.registerAboveAll("bleach_toast", com.bleachmod.client.gui.StoryToastManager.OVERLAY);
        event.registerAboveAll("bleach_damage", com.bleachmod.client.hud.DamageHud.OVERLAY);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(BleachClient::registerItemProperties);
    }

    private static void registerItemProperties() {
        ItemProperties.register(ModItems.ASAUCHI.get(), Reference.id("form"), (stack, level, entity, seed) -> {
            Player player = entity instanceof Player holder ? holder : null;
            if (player == null) {
                return 0.0F;
            }
            return PlayerCapability.get(player).map(data -> {
                String form = data.getCharacter().getActiveForm();
                if (Reference.FORM_BANKAI.equals(form)) {
                    return 2.0F;
                }
                if (Reference.FORM_SHIKAI.equals(form)) {
                    return 1.0F;
                }
                return 0.0F;
            }).orElse(0.0F);
        });
    }
}
