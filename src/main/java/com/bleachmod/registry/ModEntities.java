package com.bleachmod.registry;

import com.bleachmod.Reference;
import com.bleachmod.entity.HollowEntity;
import com.bleachmod.entity.QuestNpcEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.bleachmod.entity.HollowEntity;
import com.bleachmod.entity.HollowBossEntity;
import com.bleachmod.entity.QuestNpcEntity;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModEntities {

    private static final Map<String, RegistryObject<EntityType<QuestNpcEntity>>> NPCS = new LinkedHashMap<>();

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(
                    ForgeRegistries.ENTITY_TYPES,
                    Reference.MOD_ID
            );

    public static final RegistryObject<EntityType<HollowEntity>> HOLLOW =
            ENTITY_TYPES.register(
                    "hollow",
                    () -> EntityType.Builder
                            .of(HollowEntity::new, MobCategory.MONSTER)
                            .sized(0.8F, 2.0F)
                            .build("hollow")
            );

    public static final RegistryObject<EntityType<HollowBossEntity>> HOLLOW_BOSS =
            ENTITY_TYPES.register(
                    "hollow_boss",
                    () -> EntityType.Builder
                            .of(HollowBossEntity::new, MobCategory.MONSTER)
                            .sized(0.8F, 2.0F)
                            .build("hollow_boss")
            );

    public static final RegistryObject<EntityType<QuestNpcEntity>> NPC_SADO_YASUTORA = registerNpc("sado_yasutora");
    public static final RegistryObject<EntityType<QuestNpcEntity>> NPC_ICHIGO = registerNpc("ichigo");
    public static final RegistryObject<EntityType<QuestNpcEntity>> NPC_ORIHIME = registerNpc("orihime");
    public static final RegistryObject<EntityType<QuestNpcEntity>> NPC_URYUU = registerNpc("uryuu");
    public static final RegistryObject<EntityType<QuestNpcEntity>> NPC_RUKIA = registerNpc("rukia");
    public static final RegistryObject<EntityType<QuestNpcEntity>> NPC_BYAKUYA = registerNpc("byakuya");
    public static final RegistryObject<EntityType<QuestNpcEntity>> NPC_URAHARA = registerNpc("urahara");
    public static final RegistryObject<EntityType<QuestNpcEntity>> NPC_ULQUIORRA = registerNpc("ulquiorra");

    private static RegistryObject<EntityType<QuestNpcEntity>> registerNpc(String npcId) {
        RegistryObject<EntityType<QuestNpcEntity>> result = ENTITY_TYPES.register(
                "npc_" + npcId,
                () -> EntityType.Builder.of(QuestNpcEntity::new, MobCategory.CREATURE)
                        .sized(0.6F, 1.8F)
                        .clientTrackingRange(10)
                        .build("npc_" + npcId)
        );
        NPCS.put(npcId, result);
        return result;
    }

    public static Map<String, RegistryObject<EntityType<QuestNpcEntity>>> questNpcs() {
        return Map.copyOf(NPCS);
    }

    public static String getNpcId(EntityType<?> type) {
        return NPCS.entrySet().stream()
                .filter(entry -> entry.getValue().isPresent() && entry.getValue().get() == type)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("");
    }
}
