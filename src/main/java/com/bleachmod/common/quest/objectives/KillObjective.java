package com.bleachmod.common.quest.objectives;

import com.bleachmod.Reference;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class KillObjective extends QuestObjective {
    public enum SpawnMode {
        NATURAL,
        QUEST
    }

    public enum CountMode {
        ANY_MATCHING,
        QUEST_SPAWNED_ONLY
    }

    private final String entityId;
    private final SpawnMode spawnMode;
    private final CountMode countMode;

    public KillObjective(String entityId, int required, SpawnMode spawnMode, CountMode countMode) {
        super(ObjectiveType.KILL, required);
        this.entityId = entityId;
        this.spawnMode = spawnMode == null ? SpawnMode.NATURAL : spawnMode;
        this.countMode = countMode == null ? CountMode.ANY_MATCHING : countMode;
    }

    public String getEntityId() {
        return entityId;
    }

    public SpawnMode getSpawnMode() {
        return spawnMode;
    }

    public CountMode getCountMode() {
        return countMode;
    }

    public boolean matches(Entity entity, String questKey, int objectiveIndex) {
        if (!matchesType(entity)) {
            return false;
        }
        if (countMode == CountMode.ANY_MATCHING) {
            return true;
        }
        CompoundTag data = entity.getPersistentData();
        return questKey.equals(data.getString(Reference.TAG_QUEST_KEY))
                && data.getInt(Reference.TAG_QUEST_OBJECTIVE_INDEX) == objectiveIndex;
    }

    private boolean matchesType(Entity entity) {
        if (entityId.startsWith("#")) {
            ResourceLocation tagId = com.bleachmod.common.util.ResourceIds.parse(entityId.substring(1));
            if (tagId == null) {
                return false;
            }
            TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, tagId);
            return entity.getType().is(tag);
        }
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return id != null && id.toString().equals(entityId);
    }

    @Override
    public Component describe() {
        return Component.translatable("bleachmod.objective.kill", getRequired(), entityId);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "KILL");
        json.addProperty("entity", entityId);
        json.addProperty("count", getRequired());
        json.addProperty("spawn", spawnMode.name());
        json.addProperty("count_mode", countMode.name());
        return json;
    }
}
