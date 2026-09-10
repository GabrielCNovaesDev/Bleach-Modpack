package com.bleachmod.init;

import com.bleachmod.Reference;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MOD_ID);

    public static final RegistryObject<Item> ASUCHI = ITEMS.register("asuchi",
            () -> new SwordItem(Tiers.IRON, 3, -2.4F, new Item.Properties().stacksTo(1)));

    private ModItems() {
    }
}
