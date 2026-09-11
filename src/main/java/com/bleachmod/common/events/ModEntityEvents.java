package com.bleachmod.common.events;

import com.bleachmod.Reference;
import com.bleachmod.entity.HollowEntity;
import com.bleachmod.registry.ModEntities;

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
    }
}