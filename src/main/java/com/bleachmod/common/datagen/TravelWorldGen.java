package com.bleachmod.common.datagen;

import com.bleachmod.Reference;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TravelWorldGen {
    private static final ResourceKey<DimensionType> TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, Reference.id("soul_society"));
    private static final ResourceKey<LevelStem> STEM = ResourceKey.create(Registries.LEVEL_STEM, Reference.id("soul_society"));

    @SubscribeEvent
    public static void gather(GatherDataEvent event) {
        var builder = new RegistrySetBuilder()
            .add(Registries.DIMENSION_TYPE, ctx -> ctx.register(TYPE, new DimensionType(
                OptionalLong.empty(), true, false, false, true, 1.0, true, false,
                -64, 384, 384, BlockTags.INFINIBURN_OVERWORLD,
                net.minecraft.resources.ResourceLocation.tryParse("minecraft:overworld"), 0.0f,
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0))))
            .add(Registries.LEVEL_STEM, ctx -> {
                // Empty layers: imported chunks are retained; new chunks contain no terrain or structures.
                var settings = new FlatLevelGeneratorSettings(Optional.of(HolderSet.direct()),
                    ctx.lookup(Registries.BIOME).getOrThrow(Biomes.PLAINS), List.of());
                ctx.register(STEM, new LevelStem(ctx.lookup(Registries.DIMENSION_TYPE).getOrThrow(TYPE), new FlatLevelSource(settings)));
            });
        event.getGenerator().addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(
            event.getGenerator().getPackOutput(), event.getLookupProvider(), builder, Set.of(Reference.MOD_ID)));
    }
}
