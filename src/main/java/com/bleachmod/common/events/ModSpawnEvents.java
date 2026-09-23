package com.bleachmod.common.events;

import com.bleachmod.Reference;
import com.bleachmod.registry.ModEntities;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ModSpawnEvents {

    @Mod.EventBusSubscriber(
            modid = Reference.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.FORGE
    )
    public static class ForgeEvents {

        @SubscribeEvent
        public static void modifyPotentialSpawns(
                LevelEvent.PotentialSpawns event
        ) {

            // Só monstros
            if (event.getMobCategory() != MobCategory.MONSTER) {
                return;
            }

            // Precisamos de um Level real
            if (!(event.getLevel() instanceof Level level)) {
                return;
            }

            // Somente Overworld
            if (!level.dimension().equals(Level.OVERWORLD)) {
                return;
            }

            int minCount;
            int maxCount;

            // Dia = 2-3
            // Noite = 4-6
            if (level.isDay()) {
                minCount = 2;
                maxCount = 3;
            } else {
                minCount = 4;
                maxCount = 6;
            }

            // Adiciona o Hollow
            event.addSpawnerData(
                    new MobSpawnSettings.SpawnerData(
                            ModEntities.HOLLOW.get(),
                            80,
                            minCount,
                            maxCount
                    )
            );
        }
    }
}