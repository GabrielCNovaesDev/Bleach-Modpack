package com.bleachmod.common.quest;

import com.bleachmod.common.data.PlayerData;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuestPrerequisites {
    public enum Operator {
        AND,
        OR
    }

    public enum ConditionType {
        SAGA_QUEST,
        QUEST,
        SKILL,
        RACE
    }

    public record Condition(ConditionType type, String sagaId, int questId, String questKey, String skill, int minLevel, String race) {
        public boolean evaluate(PlayerData data) {
            return switch (type) {
                case SAGA_QUEST -> data.getPlayerQuestData().isCompleted(sagaId + ":" + questId);
                case QUEST -> data.getPlayerQuestData().isCompleted(questKey);
                case SKILL -> data.getSkills().isUnlockedAtLevel(skill, minLevel);
                case RACE -> race == null || race.isBlank() || race.equalsIgnoreCase(data.getCharacter().getRace());
            };
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("type", type.name());
            if (sagaId != null) {
                json.addProperty("sagaId", sagaId);
            }
            if (questId > 0) {
                json.addProperty("questId", questId);
            }
            if (questKey != null) {
                json.addProperty("questId", questKey);
            }
            if (skill != null) {
                json.addProperty("skill", skill);
                json.addProperty("minLevel", minLevel);
            }
            if (race != null) {
                json.addProperty("race", race);
            }
            return json;
        }
    }

    private Operator operator = Operator.AND;
    private final List<Condition> conditions = new ArrayList<>();

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator == null ? Operator.AND : operator;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public boolean evaluate(PlayerData data) {
        if (conditions.isEmpty()) {
            return true;
        }
        if (operator == Operator.OR) {
            return conditions.stream().anyMatch(c -> c.evaluate(data));
        }
        return conditions.stream().allMatch(c -> c.evaluate(data));
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("operator", operator.name());
        JsonArray array = new JsonArray();
        conditions.forEach(c -> array.add(c.toJson()));
        json.add("conditions", array);
        return json;
    }

    public static QuestPrerequisites fromJson(JsonObject json) {
        QuestPrerequisites prereq = new QuestPrerequisites();
        if (json == null) {
            return prereq;
        }
        if (json.has("operator")) {
            prereq.setOperator(Operator.valueOf(json.get("operator").getAsString().toUpperCase(Locale.ROOT)));
        }
        if (json.has("conditions")) {
            json.getAsJsonArray("conditions").forEach(el -> {
                JsonObject obj = el.getAsJsonObject();
                ConditionType type = ConditionType.valueOf(obj.get("type").getAsString().toUpperCase(Locale.ROOT));
                String sagaId = obj.has("sagaId") ? obj.get("sagaId").getAsString() : null;
                int numericId = obj.has("questId") && obj.get("questId").isJsonPrimitive() && obj.get("questId").getAsJsonPrimitive().isNumber()
                        ? obj.get("questId").getAsInt() : 0;
                String questKey = obj.has("questId") && obj.get("questId").isJsonPrimitive() && obj.get("questId").getAsJsonPrimitive().isString()
                        ? obj.get("questId").getAsString() : null;
                String skill = obj.has("skill") ? obj.get("skill").getAsString() : null;
                int minLevel = obj.has("minLevel") ? obj.get("minLevel").getAsInt() : 0;
                String race = obj.has("race") ? obj.get("race").getAsString() : null;
                prereq.conditions.add(new Condition(type, sagaId, numericId, questKey, skill, minLevel, race));
            });
        }
        return prereq;
    }
}
