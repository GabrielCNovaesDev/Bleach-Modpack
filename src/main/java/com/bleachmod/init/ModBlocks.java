package com.bleachmod.init;

import com.bleachmod.Reference;
import com.bleachmod.common.technique.SpiritFlameService;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MOD_ID);
    public static final RegistryObject<Block> SPIRIT_FLAME = BLOCKS.register("spirit_flame", () -> new Block(
            BlockBehaviour.Properties.of().noCollission().noOcclusion().instabreak().lightLevel(state -> 15)
                    .sound(SoundType.WOOL).noLootTable().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)) {
        @Override public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
            SpiritFlameService.contact(level, pos, entity);
        }
    });
    private ModBlocks() { }
}
