package com.bleachmod;

import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.init.ModCreativeTabs;
import com.bleachmod.init.ModItems;
import com.bleachmod.registry.ModEntities;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class BleachCommon {

    private BleachCommon() {
    }

    public static void init(FMLJavaModLoadingContext context) {

        IEventBus modBus = context.getModEventBus();

        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);

        modBus.addListener(BleachCommon::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {

        event.enqueueWork(NetworkHandler::register);

        BleachMod.LOGGER.info("Bleach Common initialized");
    }
}