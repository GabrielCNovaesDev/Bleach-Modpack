package com.bleachmod.common.technique;

import com.bleachmod.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Air-only snow patches with persisted expiry and bounded work; never force loads chunks. */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class GlacialSnowData extends SavedData {
    public static final int OWNER_LIMIT = 384, LEVEL_LIMIT = 2048;
    private record Patch(int layers, Map<UUID, Long> owners) { }
    private final Map<BlockPos, Patch> patches = new HashMap<>();
    public long ownerCount(UUID owner) { return patches.values().stream().filter(p -> p.owners.containsKey(owner)).count(); }
    public static GlacialSnowData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(GlacialSnowData::load, GlacialSnowData::new, "bleachmod_glacial_snow");
    }
    public boolean place(ServerLevel level, BlockPos pos, UUID owner, long expires, int layers) {
        if (!level.hasChunkAt(pos) || !level.isInWorldBounds(pos)) return false;
        Patch patch = patches.get(pos);
        if (patch != null && !level.getBlockState(pos).equals(Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, patch.layers))) {
            patches.remove(pos); setDirty(); return false;
        }
        if (patch == null && (!level.getBlockState(pos).isAir() || patches.size() >= LEVEL_LIMIT)) return false;
        if ((patch == null || !patch.owners.containsKey(owner))
                && ownerCount(owner) >= OWNER_LIMIT) return false;
        if (patch == null) {
            var state = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, Math.max(1, Math.min(3, layers)));
            if (!state.canSurvive(level, pos) || !level.setBlock(pos, state, 3)) return false;
            patch = new Patch(state.getValue(SnowLayerBlock.LAYERS), new HashMap<>());
            patches.put(pos.immutable(), patch);
        }
        patch.owners.put(owner, expires); setDirty(); return true;
    }
    public void release(ServerLevel level, UUID owner) {
        patches.values().forEach(p -> p.owners.remove(owner)); setDirty(); expire(level);
    }
    public void expire(ServerLevel level) {
        var iterator = patches.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next(); if (!level.hasChunkAt(entry.getKey())) continue;
            var state = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, entry.getValue().layers);
            if (!level.getBlockState(entry.getKey()).equals(state)) { iterator.remove(); setDirty(); continue; }
            if (entry.getValue().owners.values().removeIf(expiry -> expiry <= level.getGameTime())) setDirty();
            if (entry.getValue().owners.isEmpty()) { level.removeBlock(entry.getKey(), false); iterator.remove(); setDirty(); }
        }
    }
    @SubscribeEvent public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel level && level.getGameTime() % 10 == 0) get(level).expire(level);
    }
    @Override public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        patches.forEach((pos, patch) -> {
            CompoundTag entry = new CompoundTag(); entry.putLong("pos", pos.asLong()); entry.putInt("layers", patch.layers);
            ListTag owners = new ListTag(); patch.owners.forEach((id, until) -> {
                CompoundTag o = new CompoundTag(); o.putUUID("id", id); o.putLong("until", until); owners.add(o);
            }); entry.put("owners", owners); list.add(entry);
        }); tag.put("patches", list); return tag;
    }
    public static GlacialSnowData load(CompoundTag tag) {
        GlacialSnowData data = new GlacialSnowData();
        for (Tag raw : tag.getList("patches", Tag.TAG_COMPOUND)) {
            if (data.patches.size() >= LEVEL_LIMIT) break;
            CompoundTag entry = (CompoundTag) raw; Map<UUID, Long> owners = new HashMap<>();
            for (Tag value : entry.getList("owners", Tag.TAG_COMPOUND)) {
                CompoundTag o = (CompoundTag) value; if (o.hasUUID("id")) owners.put(o.getUUID("id"), o.getLong("until"));
            }
            data.patches.put(BlockPos.of(entry.getLong("pos")), new Patch(Math.max(1, Math.min(3, entry.getInt("layers"))), owners));
        } return data;
    }
}
