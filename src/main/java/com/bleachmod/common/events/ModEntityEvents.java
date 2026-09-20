package com.bleachmod.common.events;

import com.bleachmod.Reference;
import com.bleachmod.entity.HollowEntity;
import com.bleachmod.entity.QuestNpcEntity;
import com.bleachmod.registry.ModEntities;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = Reference.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class ModEntityEvents {

    @SubscribeEvent
    public static void registerAttributes(
            EntityAttributeCreationEvent event
    ) {
        event.put(
                ModEntities.HOLLOW.get(),
                HollowEntity.createAttributes().build()
        );
        ModEntities.questNpcs().values().forEach(type ->
                event.put(type.get(), QuestNpcEntity.createAttributes().build()));
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(
            SpawnPlacementRegisterEvent event
    ) {
        event.register(
                ModEntities.HOLLOW.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                HollowEntity::canSpawn,
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );
    }
}
