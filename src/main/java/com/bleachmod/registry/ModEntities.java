package com.bleachmod.registry;

import com.bleachmod.Reference;
import com.bleachmod.entity.HollowEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

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
}