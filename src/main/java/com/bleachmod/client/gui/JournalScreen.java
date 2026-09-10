package com.bleachmod.client.gui;

import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.data.PlayerData;
import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.common.network.c2s.ClaimQuestRewardC2S;
import com.bleachmod.common.network.c2s.QuestActionC2S;
import com.bleachmod.common.network.c2s.SetTrackedQuestC2S;
import com.bleachmod.common.network.c2s.UpdateSkillC2S;
import com.bleachmod.Reference;
import com.bleachmod.common.quest.Quest;
import com.bleachmod.common.quest.QuestAvailabilityChecker;
import com.bleachmod.common.quest.QuestProgress;
import com.bleachmod.common.quest.QuestRegistry;
import com.bleachmod.common.quest.QuestStatus;
import com.bleachmod.common.quest.objectives.QuestObjective;
import com.bleachmod.common.quest.rewards.QuestReward;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class JournalScreen extends Screen {
    private final List<Quest> quests = new ArrayList<>();
    private int selectedIndex;
    private Button startButton;
    private Button claimButton;
    private Button trackButton;

    public JournalScreen() {
        super(Component.translatable("screen.bleachmod.journal"));
    }

    @Override
    protected void init() {
        quests.clear();
        quests.addAll(QuestRegistry.allQuests());
        int listX = 20;
        int y = 40;
        int i = 0;
        for (Quest quest : quests) {
            int index = i;
            addRenderableWidget(Button.builder(buttonLabel(quest), b -> {
                selectedIndex = index;
                refreshActionButtons();
            }).bounds(listX, y, 160, 18).build());
            y += 20;
            i++;
            if (y > this.height - 40) {
                break;
            }
        }
        startButton = addRenderableWidget(Button.builder(Component.translatable("screen.bleachmod.journal.start"),
                b -> sendStart()).bounds(this.width - 180, this.height - 70, 70, 20).build());
        claimButton = addRenderableWidget(Button.builder(Component.translatable("screen.bleachmod.journal.claim"),
                b -> sendClaim()).bounds(this.width - 105, this.height - 70, 85, 20).build());
        trackButton = addRenderableWidget(Button.builder(Component.translatable("screen.bleachmod.journal.track"),
                b -> sendTrack()).bounds(this.width - 180, this.height - 46, 78, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.bleachmod.journal.upgrade"),
                b -> NetworkHandler.sendToServer(new UpdateSkillC2S(Reference.SKILL_ZANPAKUTO, UpdateSkillC2S.SkillAction.PURCHASE)))
                .bounds(this.width - 98, this.height - 46, 78, 20).build());
        refreshActionButtons();
    }

    private Component buttonLabel(Quest quest) {
        QuestStatus status = currentData() == null ? QuestStatus.NOT_STARTED : currentData().getPlayerQuestData().getStatus(quest.getQuestKey());
        return Component.literal("[" + status.name().charAt(0) + "] ").append(Component.translatable(quest.getTitle()));
    }

    private void refreshActionButtons() {
        PlayerData data = currentData();
        Quest quest = selectedQuest();
        boolean hasQuest = quest != null && data != null;
        boolean available = hasQuest && QuestAvailabilityChecker.isAvailable(data, quest.getQuestKey());
        boolean accepted = hasQuest && data.getPlayerQuestData().getStatus(quest.getQuestKey()) == QuestStatus.ACCEPTED;
        boolean success = hasQuest && data.getPlayerQuestData().isCompleted(quest.getQuestKey());
        boolean unclaimed = success && hasUnclaimed(data, quest);
        startButton.active = available;
        claimButton.active = unclaimed;
        trackButton.active = accepted;
    }

    private boolean hasUnclaimed(PlayerData data, Quest quest) {
        QuestProgress progress = data.getPlayerQuestData().getProgress(quest.getQuestKey());
        if (progress == null) {
            return true;
        }
        for (int i = 0; i < quest.getRewards().size(); i++) {
            if (!progress.isRewardClaimed(i)) {
                return true;
            }
        }
        return false;
    }

    private void sendStart() {
        Quest quest = selectedQuest();
        if (quest != null) {
            NetworkHandler.sendToServer(new QuestActionC2S(QuestActionC2S.Action.START, quest.getQuestKey()));
        }
    }

    private void sendClaim() {
        Quest quest = selectedQuest();
        PlayerData data = currentData();
        if (quest == null || data == null) {
            return;
        }
        QuestProgress progress = data.getPlayerQuestData().getProgress(quest.getQuestKey());
        for (int i = 0; i < quest.getRewards().size(); i++) {
            if (progress == null || !progress.isRewardClaimed(i)) {
                NetworkHandler.sendToServer(new ClaimQuestRewardC2S(quest.getQuestKey(), i));
            }
        }
    }

    private void sendTrack() {
        Quest quest = selectedQuest();
        if (quest != null) {
            NetworkHandler.sendToServer(new SetTrackedQuestC2S(quest.getQuestKey()));
        }
    }

    private Quest selectedQuest() {
        if (selectedIndex < 0 || selectedIndex >= quests.size()) {
            return null;
        }
        return quests.get(selectedIndex);
    }

    private PlayerData currentData() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return null;
        }
        return PlayerCapability.get(this.minecraft.player).orElse(null);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFE8C547);
        Quest quest = selectedQuest();
        PlayerData data = currentData();
        if (quest != null) {
            int x = 200;
            int y = 40;
            graphics.drawString(this.font, Component.translatable(quest.getTitle()), x, y, 0xFFFFFFFF, false);
            y += 14;
            graphics.drawWordWrap(this.font, Component.translatable(quest.getDescription()), x, y, this.width - x - 20, 0xFFCCCCCC);
            y += 36;
            graphics.drawString(this.font, Component.translatable("screen.bleachmod.journal.objectives"), x, y, 0xFFE8C547, false);
            y += 12;
            QuestProgress progress = data == null ? null : data.getPlayerQuestData().getProgress(quest.getQuestKey());
            for (int i = 0; i < quest.getObjectives().size(); i++) {
                QuestObjective objective = quest.getObjectives().get(i);
                int current = progress == null ? 0 : progress.getObjectiveProgress(i);
                int required = progress == null ? objective.getRequired() : progress.getRequired(i, objective.getRequired());
                graphics.drawString(this.font, Component.literal(current + "/" + required + " ").append(objective.describe()), x, y, 0xFFFFFFFF, false);
                y += 12;
            }
            y += 8;
            graphics.drawString(this.font, Component.translatable("screen.bleachmod.journal.rewards"), x, y, 0xFFE8C547, false);
            y += 12;
            int ri = 0;
            for (QuestReward reward : quest.getRewards()) {
                boolean claimed = progress != null && progress.isRewardClaimed(ri);
                graphics.drawString(this.font, reward.describe().copy().append(claimed ? " ✓" : ""), x, y, claimed ? 0xFF88FF88 : 0xFFFFFFFF, false);
                y += 12;
                ri++;
            }
            if (data != null) {
                graphics.drawString(this.font, Component.literal(data.getPlayerQuestData().getStatus(quest.getQuestKey()).name()), x, this.height - 90, 0xFFAAAAAA, false);
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
