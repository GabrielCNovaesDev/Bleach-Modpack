package com.bleachmod.common.quest;

import com.bleachmod.common.quest.objectives.QuestObjective;
import com.bleachmod.common.quest.rewards.QuestReward;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Quest {
    private boolean repeatable;
    public boolean isRepeatable() { return repeatable; }
    public void setRepeatable(boolean value) { repeatable = value; }
    private int version = 1;
    public void setVersion(int version) {
        if (version < 1) throw new IllegalArgumentException("Quest version must be positive");
        this.version = version;
    }
    public String progressDefinition() {
        JsonObject json = new JsonObject();
        json.addProperty("version", version);
        json.addProperty("repeatable", repeatable);
        json.addProperty("parallel", parallelObjectives);
        if (!questGiver.isBlank()) json.addProperty("quest_giver", questGiver);
        JsonArray objectivesJson = new JsonArray(), rewardsJson = new JsonArray();
        objectives.forEach(o -> objectivesJson.add(o.toJson()));
        rewards.forEach(r -> rewardsJson.add(r.toJson()));
        json.add("objectives", objectivesJson);
        json.add("rewards", rewardsJson);
        return json.toString();
    }
    private int numericId = -1;
    private String stringId;
    private QuestType type = QuestType.SIDEQUEST;
    private String title = "";
    private String description = "";
    private String category = "";
    private String questGiver = "";
    private boolean parallelObjectives;
    private final List<QuestObjective> objectives = new ArrayList<>();
    private final List<QuestReward> rewards = new ArrayList<>();
    private QuestPrerequisites prerequisites = new QuestPrerequisites();
    private String sagaId;

    public int getNumericId() {
        return numericId;
    }

    public void setNumericId(int numericId) {
        this.numericId = numericId;
    }

    public String getStringId() {
        return stringId;
    }

    public void setStringId(String stringId) {
        this.stringId = stringId;
    }

    public QuestType getType() {
        return type;
    }

    public void setType(QuestType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQuestGiver() {
        return questGiver;
    }

    public void setQuestGiver(String questGiver) {
        this.questGiver = questGiver == null ? "" : questGiver;
    }

    public boolean isParallelObjectives() {
        return parallelObjectives;
    }

    public void setParallelObjectives(boolean parallelObjectives) {
        this.parallelObjectives = parallelObjectives;
    }

    public List<QuestObjective> getObjectives() {
        return objectives;
    }

    public List<QuestReward> getRewards() {
        return rewards;
    }

    public QuestPrerequisites getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(QuestPrerequisites prerequisites) {
        this.prerequisites = prerequisites == null ? new QuestPrerequisites() : prerequisites;
    }

    public String getSagaId() {
        return sagaId;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }

    public String getQuestKey() {
        if (type == QuestType.SAGA && sagaId != null && numericId >= 0) {
            return sagaId + ":" + numericId;
        }
        return stringId;
    }

    public boolean hasKillObjective() {
        return objectives.stream().anyMatch(o -> o.getType() == QuestObjective.ObjectiveType.KILL);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("repeatable", repeatable);
        json.addProperty("version", version);
        if (type == QuestType.SAGA) {
            json.addProperty("id", numericId);
        } else {
            json.addProperty("id", stringId);
        }
        json.addProperty("title", title);
        json.addProperty("type", type.name());
        json.addProperty("description", description);
        json.addProperty("category", category);
        if (!questGiver.isBlank()) {
            json.addProperty("quest_giver", questGiver);
        }
        json.addProperty("parallel_objectives", parallelObjectives);
        if (!prerequisites.getConditions().isEmpty()) {
            json.add("prerequisites", prerequisites.toJson());
        }
        JsonArray objArray = new JsonArray();
        objectives.forEach(o -> objArray.add(o.toJson()));
        json.add("objectives", objArray);
        JsonArray rewardArray = new JsonArray();
        rewards.forEach(r -> rewardArray.add(r.toJson()));
        json.add("rewards", rewardArray);
        return json;
    }
}
