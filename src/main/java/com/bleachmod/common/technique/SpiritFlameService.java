package com.bleachmod.common.technique;

import com.bleachmod.Reference;
import com.bleachmod.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Limited, non-spreading world fire. Per-dimension game time survives server restarts. */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class SpiritFlameService extends SavedData {
    public static final int RADIUS = 6, LIFETIME = 100, OWNER_LIMIT = 32, LEVEL_LIMIT = 512;
    private final Map<BlockPos, Map<UUID, Long>> flames = new HashMap<>();
    public static SpiritFlameService get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(SpiritFlameService::load, SpiritFlameService::new, "bleachmod_spirit_flames");
    }
    public static SpiritFlameService load(CompoundTag tag) {
        SpiritFlameService data = new SpiritFlameService();
        for (Tag raw : tag.getList("flames", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) raw;
            BlockPos pos = BlockPos.of(entry.getLong("pos"));
            Map<UUID, Long> owners = new HashMap<>();
            for (Tag ownerRaw : entry.getList("owners", Tag.TAG_COMPOUND)) {
                CompoundTag owner = (CompoundTag) ownerRaw;
                if (owner.hasUUID("id")) owners.put(owner.getUUID("id"), owner.getLong("expires"));
            }
            if (!owners.isEmpty()) data.flames.put(pos, owners);
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        ListTag entries = new ListTag();
        flames.forEach((pos, owners) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong("pos", pos.asLong());
            ListTag values = new ListTag();
            owners.forEach((id, expires) -> {
                CompoundTag owner = new CompoundTag(); owner.putUUID("id", id); owner.putLong("expires", expires); values.add(owner);
            });
            entry.put("owners", values); entries.add(entry);
        });
        tag.put("flames", entries); return tag;
    }
    public boolean place(ServerLevel level, BlockPos pos, UUID owner) {
        if (!level.hasChunkAt(pos) || !level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) return false;
        var state = level.getBlockState(pos);
        boolean existing = state.is(ModBlocks.SPIRIT_FLAME.get()) && flames.containsKey(pos);
        if (!existing && (!state.isAir() || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP))) return false;
        if ((!flames.containsKey(pos) && flames.size() >= LEVEL_LIMIT)
                || (!flames.getOrDefault(pos, Map.of()).containsKey(owner)
                && flames.values().stream().filter(owners -> owners.containsKey(owner)).count() >= OWNER_LIMIT)) return false;
        if (!existing && !level.setBlock(pos, ModBlocks.SPIRIT_FLAME.get().defaultBlockState(), 3)) return false;
        flames.computeIfAbsent(pos.immutable(), unused -> new HashMap<>()).put(owner, level.getGameTime() + LIFETIME);
        setDirty(); return true;
    }
    public void expire(ServerLevel level) {
        var iterator = flames.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            // Keep unloaded positions recorded; never force-load a chunk to clean it.
            if (!level.hasChunkAt(entry.getKey())) continue;
            if (!level.getBlockState(entry.getKey()).is(ModBlocks.SPIRIT_FLAME.get())) {
                iterator.remove(); setDirty(); continue;
            }
            if (entry.getValue().entrySet().removeIf(owner -> owner.getValue() <= level.getGameTime())) setDirty();
            if (entry.getValue().isEmpty()) {
                level.removeBlock(entry.getKey(), false); iterator.remove(); setDirty();
            }
        }
    }
    public static void emit(ServerPlayer player, int attempts) {
        ServerLevel level = player.serverLevel();
        for (int i = 0; i < attempts; i++) {
            int x = level.random.nextInt(RADIUS * 2 + 1) - RADIUS;
            int z = level.random.nextInt(RADIUS * 2 + 1) - RADIUS;
            if (x * x + z * z > RADIUS * RADIUS || x * x + z * z < 4) continue;
            BlockPos center = player.blockPosition().offset(x, 2, z);
            for (int dy = 0; dy <= 4; dy++) {
                BlockPos pos = center.below(dy);
                if (!level.hasChunkAt(pos)) break;
                if (get(level).place(level, pos, player.getUUID())) break;
            }
        }
    }
    public static void contact(Level world, BlockPos pos, Entity entity) {
        if (!(world instanceof ServerLevel level) || !(entity instanceof LivingEntity target) || target.tickCount % 20 != 0) return;
        Map<UUID, Long> owners = get(level).flames.get(pos);
        if (owners == null) return;
        for (var entry : owners.entrySet()) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (entry.getValue() > level.getGameTime() && owner != null && owner.serverLevel() == level
                    && TechniqueTargets.allowed(owner, target)) {
                target.hurt(level.damageSources().playerAttack(owner), 4); return;
            }
        }
    }
    @SubscribeEvent public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel level && level.getGameTime() % 20 == 0)
            get(level).expire(level);
    }
}
