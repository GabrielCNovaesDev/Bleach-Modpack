package com.bleachmod.common.quest;

import com.bleachmod.Reference;
import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.data.PlayerData;
import com.bleachmod.common.events.BleachEvents;
import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.common.network.SyncHelper;
import com.bleachmod.common.network.s2c.ActionFeedbackS2C;
import com.bleachmod.common.network.s2c.StoryToastS2C;
import com.bleachmod.common.quest.objectives.KillObjective;
import com.bleachmod.common.quest.objectives.QuestObjective;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;

public final class QuestService {
    private QuestService() {
    }

    public static void startQuest(ServerPlayer player, String questKey) {
        PlayerData data = PlayerCapability.get(player).orElse(null);
        if (data == null) {
            return;
        }
        Quest quest = QuestRegistry.getQuest(questKey);
        if (quest == null) {
            NetworkHandler.sendToPlayer(ActionFeedbackS2C.of(Component.translatable("message.bleachmod.quest.unknown")), player);
            return;
        }
        QuestStatus status = data.getPlayerQuestData().getStatus(questKey);
        if (status == QuestStatus.ACCEPTED || status == QuestStatus.SUCCESS) {
            NetworkHandler.sendToPlayer(ActionFeedbackS2C.of(Component.translatable("message.bleachmod.quest.already_active")), player);
            return;
        }
        if (status != QuestStatus.FAILED && !QuestAvailabilityChecker.isAvailable(data, questKey)) {
            NetworkHandler.sendToPlayer(ActionFeedbackS2C.of(Component.translatable("message.bleachmod.quest.unavailable")), player);
            return;
        }
        if (MinecraftForge.EVENT_BUS.post(new BleachEvents.QuestStartEvent(player, questKey, quest))) {
            return;
        }
        data.getPlayerQuestData().acceptQuest(questKey, quest);
        spawnQuestMobs(player, questKey, quest);
        NetworkHandler.sendToPlayer(new StoryToastS2C(StoryToastS2C.ToastType.START, questKey, -1, 0, 0), player);
        SyncHelper.progression(player);
    }

    public static void incrementObjective(ServerPlayer player, PlayerData data, String questKey, Quest quest, int index, int amount) {
        QuestProgress progress = data.getPlayerQuestData().getProgress(questKey);
        if (progress == null || progress.getStatus() != QuestStatus.ACCEPTED) {
            return;
        }
        if (!isObjectiveActive(quest, progress, index)) {
            return;
        }
        int required = progress.getRequired(index, quest.getObjectives().get(index).getRequired());
        int current = progress.getObjectiveProgress(index);
        if (current >= required) {
            return;
        }
        int next = Math.min(required, current + amount);
        if (MinecraftForge.EVENT_BUS.post(new BleachEvents.QuestObjectiveProgressEvent(player, questKey, index, next, required))) {
            return;
        }
        progress.setObjectiveProgress(index, next);
        if (next >= required) {
            NetworkHandler.sendToPlayer(new StoryToastS2C(StoryToastS2C.ToastType.OBJECTIVE, questKey, index, next, required), player);
        }
        checkAndComplete(player, data, questKey, quest);
        SyncHelper.progression(player);
    }

    public static void setObjectiveProgress(ServerPlayer player, PlayerData data, String questKey, Quest quest, int index, int value) {
        QuestProgress progress = data.getPlayerQuestData().getProgress(questKey);
        if (progress == null || progress.getStatus() != QuestStatus.ACCEPTED) {
            return;
        }
        if (!isObjectiveActive(quest, progress, index)) {
            return;
        }
        int required = progress.getRequired(index, quest.getObjectives().get(index).getRequired());
        int clamped = Math.min(required, Math.max(0, value));
        if (clamped == progress.getObjectiveProgress(index)) {
            return;
        }
        if (MinecraftForge.EVENT_BUS.post(new BleachEvents.QuestObjectiveProgressEvent(player, questKey, index, clamped, required))) {
            return;
        }
        boolean newlyComplete = progress.getObjectiveProgress(index) < required && clamped >= required;
        progress.setObjectiveProgress(index, clamped);
        if (newlyComplete) {
            NetworkHandler.sendToPlayer(new StoryToastS2C(StoryToastS2C.ToastType.OBJECTIVE, questKey, index, clamped, required), player);
        }
        checkAndComplete(player, data, questKey, quest);
        SyncHelper.progression(player);
    }

    public static void checkAndComplete(ServerPlayer player, PlayerData data, String questKey, Quest quest) {
        QuestProgress progress = data.getPlayerQuestData().getProgress(questKey);
        if (progress == null || progress.getStatus() != QuestStatus.ACCEPTED) {
            return;
        }
        if (!progress.allObjectivesComplete(quest)) {
            return;
        }
        if (MinecraftForge.EVENT_BUS.post(new BleachEvents.QuestCompletedEvent(player, questKey, quest))) {
            return;
        }
        data.getPlayerQuestData().completeQuest(questKey);
        NetworkHandler.sendToPlayer(new StoryToastS2C(StoryToastS2C.ToastType.COMPLETE, questKey, -1, 0, 0), player);
        SyncHelper.progression(player);
    }

    public static void failQuest(ServerPlayer player, PlayerData data, String questKey) {
        if (!data.getPlayerQuestData().isAccepted(questKey)) {
            return;
        }
        if (MinecraftForge.EVENT_BUS.post(new BleachEvents.QuestFailEvent(player, questKey))) {
            return;
        }
        data.getPlayerQuestData().failQuest(questKey);
        NetworkHandler.sendToPlayer(new StoryToastS2C(StoryToastS2C.ToastType.FAIL, questKey, -1, 0, 0), player);
        SyncHelper.progression(player);
    }

    public static void claimReward(ServerPlayer player, String questKey, int rewardIndex) {
        PlayerData data = PlayerCapability.get(player).orElse(null);
        if (data == null) {
            return;
        }
        Quest quest = QuestRegistry.getQuest(questKey);
        if (quest == null || !data.getPlayerQuestData().isCompleted(questKey)) {
            NetworkHandler.sendToPlayer(ActionFeedbackS2C.of(Component.translatable("message.bleachmod.quest.cannot_claim")), player);
            return;
        }
        if (rewardIndex < 0 || rewardIndex >= quest.getRewards().size()) {
            return;
        }
        QuestProgress progress = data.getPlayerQuestData().getOrCreateProgress(questKey);
        if (progress.isRewardClaimed(rewardIndex)) {
            NetworkHandler.sendToPlayer(ActionFeedbackS2C.of(Component.translatable("message.bleachmod.quest.already_claimed")), player);
            return;
        }
        if (MinecraftForge.EVENT_BUS.post(new BleachEvents.QuestRewardClaimEvent(player, questKey, rewardIndex))) {
            return;
        }
        quest.getRewards().get(rewardIndex).give(player, data);
        progress.claimReward(rewardIndex);
        NetworkHandler.sendToPlayer(new StoryToastS2C(StoryToastS2C.ToastType.CLAIM, questKey, rewardIndex, 0, 0), player);
        SyncHelper.full(player);
    }

    public static boolean isObjectiveActive(Quest quest, QuestProgress progress, int index) {
        if (quest.isParallelObjectives()) {
            return true;
        }
        for (int i = 0; i < index; i++) {
            int required = progress.getRequired(i, quest.getObjectives().get(i).getRequired());
            if (progress.getObjectiveProgress(i) < required) {
                return false;
            }
        }
        return true;
    }

    private static void spawnQuestMobs(ServerPlayer player, String questKey, Quest quest) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        for (int i = 0; i < quest.getObjectives().size(); i++) {
            QuestObjective objective = quest.getObjectives().get(i);
            if (!(objective instanceof KillObjective kill) || kill.getSpawnMode() != KillObjective.SpawnMode.QUEST) {
                continue;
            }
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(com.bleachmod.common.util.ResourceIds.parse(kill.getEntityId()));
            if (type == null) {
                continue;
            }
            for (int n = 0; n < kill.getRequired(); n++) {
                BlockPos pos = player.blockPosition().offset(player.getRandom().nextInt(7) - 3, 0, player.getRandom().nextInt(7) - 3);
                var entity = type.create(level);
                if (!(entity instanceof Mob mob)) {
                    if (entity != null) {
                        entity.discard();
                    }
                    continue;
                }
                mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), 0);
                mob.getPersistentData().putString(Reference.TAG_QUEST_KEY, questKey);
                mob.getPersistentData().putInt(Reference.TAG_QUEST_OBJECTIVE_INDEX, i);
                mob.getPersistentData().putUUID(Reference.TAG_QUEST_OWNER, player.getUUID());
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
                level.addFreshEntity(mob);
            }
        }
    }

}
