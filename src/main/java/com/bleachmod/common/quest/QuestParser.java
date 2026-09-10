package com.bleachmod.common.quest;

import com.bleachmod.common.quest.objectives.ItemObjective;
import com.bleachmod.common.quest.objectives.KillObjective;
import com.bleachmod.common.quest.objectives.QuestObjective;
import com.bleachmod.common.quest.rewards.ItemReward;
import com.bleachmod.common.quest.rewards.QuestReward;
import com.bleachmod.common.quest.rewards.SkillReward;
import com.bleachmod.common.quest.rewards.TpsReward;
import com.bleachmod.common.quest.rewards.TransformationReward;
import com.google.gson.JsonObject;

public final class QuestParser {
    private QuestParser() {
    }

    public static Quest parseQuest(JsonObject json, String sagaId) {
        Quest quest = new Quest();
        if (json.get("id").isJsonPrimitive() && json.get("id").getAsJsonPrimitive().isNumber()) {
            quest.setNumericId(json.get("id").getAsInt());
            quest.setType(QuestType.SAGA);
            quest.setSagaId(sagaId);
        } else {
            quest.setStringId(json.get("id").getAsString());
            quest.setType(QuestType.SIDEQUEST);
        }
        if (json.has("type")) {
            quest.setType(QuestType.fromName(json.get("type").getAsString()));
            if (quest.getType() == QuestType.SAGA) {
                quest.setSagaId(sagaId);
            }
        }
        quest.setTitle(json.get("title").getAsString());
        if (json.has("description")) {
            quest.setDescription(json.get("description").getAsString());
        }
        if (json.has("category")) {
            quest.setCategory(json.get("category").getAsString());
        }
        if (json.has("parallel_objectives")) {
            quest.setParallelObjectives(json.get("parallel_objectives").getAsBoolean());
        }
        if (json.has("prerequisites")) {
            quest.setPrerequisites(QuestPrerequisites.fromJson(json.getAsJsonObject("prerequisites")));
        }
        if (json.has("objectives")) {
            json.getAsJsonArray("objectives").forEach(el -> quest.getObjectives().add(parseObjective(el.getAsJsonObject())));
        }
        if (json.has("rewards")) {
            json.getAsJsonArray("rewards").forEach(el -> quest.getRewards().add(parseReward(el.getAsJsonObject())));
        }
        return quest;
    }

    private static QuestObjective parseObjective(JsonObject json) {
        String type = json.get("type").getAsString().toUpperCase();
        return switch (type) {
            case "KILL" -> new KillObjective(
                    json.get("entity").getAsString(),
                    json.get("count").getAsInt(),
                    json.has("spawn") ? KillObjective.SpawnMode.valueOf(json.get("spawn").getAsString().toUpperCase()) : KillObjective.SpawnMode.NATURAL,
                    json.has("count_mode") ? KillObjective.CountMode.valueOf(json.get("count_mode").getAsString().toUpperCase()) : KillObjective.CountMode.ANY_MATCHING
            );
            case "ITEM" -> new ItemObjective(json.get("item").getAsString(), json.get("count").getAsInt());
            default -> throw new IllegalArgumentException("Unsupported objective type: " + type);
        };
    }

    private static QuestReward parseReward(JsonObject json) {
        String type = json.get("type").getAsString().toUpperCase();
        return switch (type) {
            case "TPS" -> new TpsReward(json.get("amount").getAsFloat());
            case "SKILL" -> new SkillReward(json.get("skill").getAsString(), json.get("level").getAsInt());
            case "TRANSFORMATION" -> new TransformationReward(
                    json.get("formGroup").getAsString(),
                    json.get("formName").getAsString(),
                    json.has("mastery") ? json.get("mastery").getAsDouble() : 100.0D
            );
            case "ITEM" -> new ItemReward(json.get("item").getAsString(), json.get("count").getAsInt());
            default -> throw new IllegalArgumentException("Unsupported reward type: " + type);
        };
    }
}
