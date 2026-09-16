package com.bleachmod.registry;

import com.bleachmod.Reference;

import net.minecraft.world.item.Item;

import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(
                    ForgeRegistries.ITEMS,
                    Reference.MOD_ID
            );

    public static final RegistryObject<Item> HOLLOW_SPAWN_EGG =
            ITEMS.register(
                    "hollow_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.HOLLOW,
                            0x1A1A1A,
                            0xE6E6E6,
                            new Item.Properties()
                    )
            );
}