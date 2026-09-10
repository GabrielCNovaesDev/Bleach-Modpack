package com.bleachmod.common.events;

import com.bleachmod.common.quest.Quest;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public final class BleachEvents {
    private BleachEvents() {
    }

    @Cancelable
    public static class QuestStartEvent extends Event {
        private final ServerPlayer player;
        private final String questKey;
        private final Quest quest;

        public QuestStartEvent(ServerPlayer player, String questKey, Quest quest) {
            this.player = player;
            this.questKey = questKey;
            this.quest = quest;
        }

        public ServerPlayer getPlayer() {
            return player;
        }

        public String getQuestKey() {
            return questKey;
        }

        public Quest getQuest() {
            return quest;
        }
    }

    @Cancelable
    public static class QuestObjectiveProgressEvent extends Event {
        private final ServerPlayer player;
        private final String questKey;
        private final int objectiveIndex;
        private final int progress;
        private final int required;

        public QuestObjectiveProgressEvent(ServerPlayer player, String questKey, int objectiveIndex, int progress, int required) {
            this.player = player;
            this.questKey = questKey;
            this.objectiveIndex = objectiveIndex;
            this.progress = progress;
            this.required = required;
        }

        public ServerPlayer getPlayer() {
            return player;
        }

        public String getQuestKey() {
            return questKey;
        }

        public int getObjectiveIndex() {
            return objectiveIndex;
        }

        public int getProgress() {
            return progress;
        }

        public int getRequired() {
            return required;
        }
    }

    @Cancelable
    public static class QuestFailEvent extends Event {
        private final ServerPlayer player;
        private final String questKey;

        public QuestFailEvent(ServerPlayer player, String questKey) {
            this.player = player;
            this.questKey = questKey;
        }

        public ServerPlayer getPlayer() {
            return player;
        }

        public String getQuestKey() {
            return questKey;
        }
    }

    @Cancelable
    public static class QuestCompletedEvent extends Event {
        private final ServerPlayer player;
        private final String questKey;
        private final Quest quest;

        public QuestCompletedEvent(ServerPlayer player, String questKey, Quest quest) {
            this.player = player;
            this.questKey = questKey;
            this.quest = quest;
        }

        public ServerPlayer getPlayer() {
            return player;
        }

        public String getQuestKey() {
            return questKey;
        }

        public Quest getQuest() {
            return quest;
        }
    }

    @Cancelable
    public static class QuestRewardClaimEvent extends Event {
        private final ServerPlayer player;
        private final String questKey;
        private final int rewardIndex;

        public QuestRewardClaimEvent(ServerPlayer player, String questKey, int rewardIndex) {
            this.player = player;
            this.questKey = questKey;
            this.rewardIndex = rewardIndex;
        }

        public ServerPlayer getPlayer() {
            return player;
        }

        public String getQuestKey() {
            return questKey;
        }

        public int getRewardIndex() {
            return rewardIndex;
        }
    }

    public static class FormChangeEvent extends Event {
        private final ServerPlayer player;
        private final String oldGroup;
        private final String oldForm;
        private final String newGroup;
        private final String newForm;

        public FormChangeEvent(ServerPlayer player, String oldGroup, String oldForm, String newGroup, String newForm) {
            this.player = player;
            this.oldGroup = oldGroup;
            this.oldForm = oldForm;
            this.newGroup = newGroup;
            this.newForm = newForm;
        }

        public ServerPlayer getPlayer() {
            return player;
        }

        public String getOldGroup() {
            return oldGroup;
        }

        public String getOldForm() {
            return oldForm;
        }

        public String getNewGroup() {
            return newGroup;
        }

        public String getNewForm() {
            return newForm;
        }
    }
}
