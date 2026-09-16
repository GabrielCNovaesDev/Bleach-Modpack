package com.bleachmod.init;

import com.bleachmod.Reference;
import com.bleachmod.registry.ModEntities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModItems {
    private static final Map<String, RegistryObject<Item>> NPC_SPAWN_EGGS = new LinkedHashMap<>();
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MOD_ID);

    public static final RegistryObject<Item> ASAUCHI = ITEMS.register("asauchi",
            () -> new SwordItem(Tiers.IRON, 3, -2.4F, new Item.Properties().stacksTo(1)));

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

    public static final RegistryObject<Item> NPC_SADO_YASUTORA_SPAWN_EGG = npcEgg("sado_yasutora", ModEntities.NPC_SADO_YASUTORA, 0x29231F, 0xD2A17C);
    public static final RegistryObject<Item> NPC_ICHIGO_SPAWN_EGG = npcEgg("ichigo", ModEntities.NPC_ICHIGO, 0xE87822, 0x171717);
    public static final RegistryObject<Item> NPC_ORIHIME_SPAWN_EGG = npcEgg("orihime", ModEntities.NPC_ORIHIME, 0xB05B35, 0xE8D4C2);
    public static final RegistryObject<Item> NPC_URYUU_SPAWN_EGG = npcEgg("uryuu", ModEntities.NPC_URYUU, 0xE8E8E8, 0x37638A);
    public static final RegistryObject<Item> NPC_RUKIA_SPAWN_EGG = npcEgg("rukia", ModEntities.NPC_RUKIA, 0x181818, 0xE2D6C8);
    public static final RegistryObject<Item> NPC_BYAKUYA_SPAWN_EGG = npcEgg("byakuya", ModEntities.NPC_BYAKUYA, 0x241E2D, 0xF1E7DD);
    public static final RegistryObject<Item> NPC_URAHARA_SPAWN_EGG = npcEgg("urahara", ModEntities.NPC_URAHARA, 0x506644, 0xD4C79C);
    public static final RegistryObject<Item> NPC_ULQUIORRA_SPAWN_EGG = npcEgg("ulquiorra", ModEntities.NPC_ULQUIORRA, 0xE8E5DB, 0x315B4D);

    private static RegistryObject<Item> npcEgg(String npcId,
                                                RegistryObject<? extends net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob>> entityType,
                                                int primary, int secondary) {
        RegistryObject<Item> result = ITEMS.register("npc_" + npcId + "_spawn_egg",
                () -> new ForgeSpawnEggItem(entityType, primary, secondary, new Item.Properties()));
        NPC_SPAWN_EGGS.put(npcId, result);
        return result;
    }

    public static Map<String, RegistryObject<Item>> npcSpawnEggs() {
        return Map.copyOf(NPC_SPAWN_EGGS);
    }

    private ModItems() {
    }
}
