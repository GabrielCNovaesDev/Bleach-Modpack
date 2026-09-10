package com.bleachmod.common.quest;

import com.bleachmod.common.data.PlayerData;

import java.util.List;

public final class QuestAvailabilityChecker {
    private QuestAvailabilityChecker() {
    }

    public static boolean isAvailable(PlayerData data, String questKey) {
        if (!data.getStatus().hasCreatedCharacter()) {
            return false;
        }
        Quest quest = QuestRegistry.getQuest(questKey);
        if (quest == null) {
            return false;
        }
        QuestStatus status = data.getPlayerQuestData().getStatus(questKey);
        if (status == QuestStatus.ACCEPTED || status == QuestStatus.SUCCESS) {
            return false;
        }
        if (status == QuestStatus.FAILED) {
            return true;
        }
        if (quest.getType() == QuestType.SAGA) {
            if (!previousSagaComplete(data, quest.getSagaId())) {
                return false;
            }
            if (!previousSagaQuestComplete(data, quest)) {
                return false;
            }
        }
        return quest.getPrerequisites().evaluate(data);
    }

    public static boolean previousSagaComplete(PlayerData data, String sagaId) {
        Saga saga = QuestRegistry.getSaga(sagaId);
        if (saga == null || saga.getPreviousSaga() == null || saga.getPreviousSaga().isBlank()) {
            return true;
        }
        List<String> previous = QuestRegistry.sagaQuestKeys(saga.getPreviousSaga());
        if (previous.isEmpty()) {
            return true;
        }
        return previous.stream().allMatch(data.getPlayerQuestData()::isCompleted);
    }

    public static boolean previousSagaQuestComplete(PlayerData data, Quest quest) {
        List<String> order = QuestRegistry.sagaQuestKeys(quest.getSagaId());
        int index = order.indexOf(quest.getQuestKey());
        if (index <= 0) {
            return true;
        }
        return data.getPlayerQuestData().isCompleted(order.get(index - 1));
    }
}
