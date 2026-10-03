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
        quest.setRepeatable(json.has("repeatable") && json.get("repeatable").getAsBoolean());
        if (json.has("version")) quest.setVersion(json.get("version").getAsInt());
        if (json.get("id").isJsonPrimitive() && json.get("id").getAsJsonPrimitive().isNumber()) {
            quest.setNumericId(json.get("id").getAsInt());
            quest.setType(QuestType.SAGA);
            quest.setSagaId(sagaId);
        } else {
            quest.setStringId(json.get("id").getAsString());
            quest.setType(QuestType.SIDEQUEST);
        }
        // Original default training files had no repeatability flag.
        if (!json.has("repeatable") && "rukia_basic_training".equals(quest.getStringId())) quest.setRepeatable(true);
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
        if (json.has("quest_giver")) {
            String questGiver = json.get("quest_giver").getAsString();
            if (!questGiver.matches("[a-z0-9_]{1,64}")) {
                throw new IllegalArgumentException("Invalid quest giver: " + questGiver);
            }
            quest.setQuestGiver(questGiver);
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
        if (quest.isRepeatable() && quest.getType() != QuestType.SIDEQUEST) throw new IllegalArgumentException("Only sidequests can repeat");
        if (quest.getObjectives().isEmpty() || quest.getObjectives().size() > 16 || quest.getRewards().size() > 16)
            throw new IllegalArgumentException("Quest must have 1..16 objectives and at most 16 rewards");
        return quest;
    }

    private static QuestObjective parseObjective(JsonObject json) {
        if (json.get("count").getAsInt() < 1 || json.get("count").getAsInt() > 10000)
            throw new IllegalArgumentException("Objective count must be 1..10000");

        String id = json.has("entity")
                ? json.get("entity").getAsString()
                : json.get("item").getAsString();

        if (!id.matches("#?[a-z0-9_.-]+:[a-z0-9_./-]+"))
            throw new IllegalArgumentException("Invalid resource ID: " + id);

        String type = json.get("type").getAsString().toUpperCase(java.util.Locale.ROOT);

        return switch (type) {
            case "KILL" -> new KillObjective(
                    json.get("entity").getAsString(),
                    json.get("count").getAsInt(),
                    json.has("spawn")
                            ? KillObjective.SpawnMode.valueOf(
                            json.get("spawn").getAsString().toUpperCase()
                    )
                            : KillObjective.SpawnMode.NATURAL,
                    json.has("count_mode")
                            ? KillObjective.CountMode.valueOf(
                            json.get("count_mode").getAsString().toUpperCase()
                    )
                            : KillObjective.CountMode.ANY_MATCHING
            );

            case "ITEM" -> new ItemObjective(
                    json.get("item").getAsString(),
                    json.get("count").getAsInt()
            );

            default -> throw new IllegalArgumentException(
                    "Unsupported objective type: " + type
            );
        };
    }

    private static QuestReward parseReward(JsonObject json) {
        if (json.has("amount") && (!Float.isFinite(json.get("amount").getAsFloat()) || json.get("amount").getAsFloat() < 0 || json.get("amount").getAsFloat() > 1000000))
            throw new IllegalArgumentException("Invalid points reward");
        if (json.has("skill") && (!"zanpakuto".equals(json.get("skill").getAsString()) || json.get("level").getAsInt() < 0 || json.get("level").getAsInt() > 2))
            throw new IllegalArgumentException("Invalid skill reward");
        if (json.has("formName") && (!"zanpakuto".equals(json.get("formGroup").getAsString()) || !java.util.Set.of("sealed","shikai","bankai").contains(json.get("formName").getAsString())))
            throw new IllegalArgumentException("Invalid transformation reward");
        if (json.has("item") && (!json.get("item").getAsString().matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || json.get("count").getAsInt() < 1 || json.get("count").getAsInt() > 64))
            throw new IllegalArgumentException("Invalid item reward");
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
