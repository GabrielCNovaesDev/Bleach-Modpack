package com.bleachmod.common.quest;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public class QuestProgress {
    private String questId;
    private QuestStatus status = QuestStatus.NOT_STARTED;
    private final Map<Integer, Integer> objectives = new HashMap<>();
    private final Map<Integer, Integer> objectiveRequirements = new HashMap<>();
    private final Map<Integer, Boolean> rewards = new HashMap<>();
    private int failureCount;

    public QuestProgress(String questId) {
        this.questId = questId;
    }

    public String getQuestId() {
        return questId;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public void setStatus(QuestStatus status) {
        this.status = status;
    }

    public int getObjectiveProgress(int index) {
        return objectives.getOrDefault(index, 0);
    }

    public void setObjectiveProgress(int index, int value) {
        objectives.put(index, Math.max(0, value));
    }

    public int getRequired(int index, int fallback) {
        return objectiveRequirements.getOrDefault(index, fallback);
    }

    public void setRequired(int index, int required) {
        objectiveRequirements.put(index, required);
    }

    public boolean isRewardClaimed(int index) {
        return rewards.getOrDefault(index, false);
    }

    public void claimReward(int index) {
        rewards.put(index, true);
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void markFailed() {
        failureCount++;
        objectives.clear();
        status = QuestStatus.FAILED;
    }

    public void initializeRequirements(Quest quest) {
        objectives.clear();
        objectiveRequirements.clear();
        for (int i = 0; i < quest.getObjectives().size(); i++) {
            objectiveRequirements.put(i, quest.getObjectives().get(i).getRequired());
            objectives.put(i, 0);
        }
    }

    public boolean allObjectivesComplete(Quest quest) {
        for (int i = 0; i < quest.getObjectives().size(); i++) {
            int required = getRequired(i, quest.getObjectives().get(i).getRequired());
            if (getObjectiveProgress(i) < required) {
                return false;
            }
        }
        return true;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("questId", questId);
        tag.putString("status", status.name());
        tag.putInt("failureCount", failureCount);
        tag.put("objectives", intMap(objectives));
        tag.put("objectiveRequirements", intMap(objectiveRequirements));
        CompoundTag rewardTag = new CompoundTag();
        rewards.forEach((k, v) -> rewardTag.putBoolean(String.valueOf(k), v));
        tag.put("rewards", rewardTag);
        return tag;
    }

    public static QuestProgress load(CompoundTag tag) {
        QuestProgress progress = new QuestProgress(tag.getString("questId"));
        progress.status = QuestStatus.fromName(tag.getString("status"));
        progress.failureCount = tag.getInt("failureCount");
        readIntMap(tag.getCompound("objectives"), progress.objectives);
        readIntMap(tag.getCompound("objectiveRequirements"), progress.objectiveRequirements);
        CompoundTag rewardTag = tag.getCompound("rewards");
        for (String key : rewardTag.getAllKeys()) {
            progress.rewards.put(Integer.parseInt(key), rewardTag.getBoolean(key));
        }
        return progress;
    }

    private static CompoundTag intMap(Map<Integer, Integer> map) {
        CompoundTag tag = new CompoundTag();
        map.forEach((k, v) -> tag.putInt(String.valueOf(k), v));
        return tag;
    }

    private static void readIntMap(CompoundTag tag, Map<Integer, Integer> dest) {
        dest.clear();
        for (String key : tag.getAllKeys()) {
            dest.put(Integer.parseInt(key), tag.getInt(key));
        }
    }
}
