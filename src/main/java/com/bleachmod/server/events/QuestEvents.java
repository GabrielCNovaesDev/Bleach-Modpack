package com.bleachmod.server.events;

import com.bleachmod.Reference;
import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.data.PlayerData;
import com.bleachmod.common.quest.PlayerQuestData;
import com.bleachmod.common.quest.Quest;
import com.bleachmod.common.quest.QuestProgress;
import com.bleachmod.common.quest.QuestRegistry;
import com.bleachmod.common.quest.QuestService;
import com.bleachmod.common.quest.QuestStatus;
import com.bleachmod.common.quest.objectives.ItemObjective;
import com.bleachmod.common.quest.objectives.KillObjective;
import com.bleachmod.common.quest.objectives.QuestObjective;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class QuestEvents {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }
        if (event.player.tickCount % 20 != 0) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        PlayerCapability.get(player).ifPresent(data -> processItemObjectives(player, data));
    }

    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer deadPlayer) {
            handlePlayerQuestFailure(deadPlayer);
        }
        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            creditQuestKill(killer, event.getEntity());
        }
    }

    private static void processItemObjectives(ServerPlayer player, PlayerData data) {
        for (String questKey : data.getPlayerQuestData().getAcceptedQuestIds()) {
            Quest quest = QuestRegistry.getQuest(questKey);
            if (quest == null) {
                continue;
            }
            QuestProgress progress = data.getPlayerQuestData().getProgress(questKey);
            if (progress == null) {
                continue;
            }
            for (int i = 0; i < quest.getObjectives().size(); i++) {
                QuestObjective objective = quest.getObjectives().get(i);
                if (!(objective instanceof ItemObjective itemObjective)) {
                    continue;
                }
                if (!QuestService.isObjectiveActive(quest, progress, i)) {
                    continue;
                }
                int count = itemObjective.countInInventory(player);
                QuestService.setObjectiveProgress(player, data, questKey, quest, i, count);
            }
        }
    }

    private static void creditQuestKill(ServerPlayer killer, LivingEntity killed) {
        PlayerCapability.get(killer).ifPresent(data -> {
            for (String questKey : data.getPlayerQuestData().getAcceptedQuestIds()) {
                Quest quest = QuestRegistry.getQuest(questKey);
                if (quest == null) {
                    continue;
                }
                QuestProgress progress = data.getPlayerQuestData().getProgress(questKey);
                if (progress == null || progress.getStatus() != QuestStatus.ACCEPTED) {
                    continue;
                }
                for (int i = 0; i < quest.getObjectives().size(); i++) {
                    if (!(quest.getObjectives().get(i) instanceof KillObjective kill)) {
                        continue;
                    }
                    if (!kill.matches(killed, questKey, i)) {
                        continue;
                    }
                    QuestService.incrementObjective(killer, data, questKey, quest, i, 1);
                }
            }
        });
    }

    private static void handlePlayerQuestFailure(ServerPlayer deadPlayer) {
        PlayerCapability.get(deadPlayer).ifPresent(data -> {
            PlayerQuestData pqd = data.getPlayerQuestData();
            for (String questKey : pqd.getAcceptedQuestIds()) {
                Quest quest = QuestRegistry.getQuest(questKey);
                if (quest != null && quest.hasKillObjective()) {
                    QuestService.failQuest(deadPlayer, data, questKey);
                }
            }
        });
    }
}
