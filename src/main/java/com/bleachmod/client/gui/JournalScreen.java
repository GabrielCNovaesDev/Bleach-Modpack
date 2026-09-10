package com.bleachmod.client.gui;

import com.bleachmod.Reference;
import com.bleachmod.client.BleachTextures;
import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.data.PlayerData;
import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.common.network.c2s.ClaimQuestRewardC2S;
import com.bleachmod.common.network.c2s.QuestActionC2S;
import com.bleachmod.common.network.c2s.SetTrackedQuestC2S;
import com.bleachmod.common.network.c2s.UpdateSkillC2S;
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
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class JournalScreen extends Screen {
    private final List<Quest> quests = new ArrayList<>();
    private int selectedIndex;
    private Button startButton;
    private Button claimButton;
    private Button trackButton;
    private int bookX;
    private int bookY;

    public JournalScreen() {
        super(Component.translatable("screen.bleachmod.journal"));
    }

    @Override
    protected void init() {
        quests.clear();
        quests.addAll(QuestRegistry.allQuests());
        bookX = (this.width - BleachTextures.JOURNAL_W) / 2;
        bookY = (this.height - BleachTextures.JOURNAL_H) / 2 + 6;

        int max = Math.min(quests.size(), BleachTextures.JOURNAL_SLOT_COUNT);
        for (int i = 0; i < max; i++) {
            int index = i;
            addRenderableWidget(new SlotButton(
                    bookX + BleachTextures.JOURNAL_SLOT_X,
                    bookY + BleachTextures.JOURNAL_SLOT_Y + i * BleachTextures.JOURNAL_SLOT_STRIDE,
                    BleachTextures.JOURNAL_SLOT_W,
                    BleachTextures.JOURNAL_SLOT_H,
                    () -> {
                        selectedIndex = index;
                        refreshActionButtons();
                    }, () -> selectedIndex == index));
        }

        int by = bookY + BleachTextures.JOURNAL_H + 8;
        startButton = addRenderableWidget(Button.builder(Component.translatable("screen.bleachmod.journal.start"),
                b -> sendStart()).bounds(bookX, by, 74, 20).build());
        claimButton = addRenderableWidget(Button.builder(Component.translatable("screen.bleachmod.journal.claim"),
                b -> sendClaim()).bounds(bookX + 82, by, 74, 20).build());
        trackButton = addRenderableWidget(Button.builder(Component.translatable("screen.bleachmod.journal.track"),
                b -> sendTrack()).bounds(bookX + 164, by, 74, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.bleachmod.journal.upgrade"),
                b -> NetworkHandler.sendToServer(new UpdateSkillC2S(Reference.SKILL_ZANPAKUTO, UpdateSkillC2S.SkillAction.PURCHASE)))
                .bounds(bookX + 246, by, 74, 20).build());
        refreshActionButtons();
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
        BleachTextures.blitNative(graphics, BleachTextures.JOURNAL_BG, bookX, bookY,
                BleachTextures.JOURNAL_W, BleachTextures.JOURNAL_H);
        int headerX = bookX + (BleachTextures.JOURNAL_W - BleachTextures.HEADER_W) / 2;
        int headerY = bookY - BleachTextures.HEADER_H - 2;
        BleachTextures.blitNative(graphics, BleachTextures.JOURNAL_HEADER, headerX, headerY,
                BleachTextures.HEADER_W, BleachTextures.HEADER_H);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, headerY + 10, 0xF2E9C8);

        PlayerData data = currentData();
        int visible = Math.min(quests.size(), BleachTextures.JOURNAL_SLOT_COUNT);
        for (int i = 0; i < visible; i++) {
            Quest quest = quests.get(i);
            QuestStatus status = data == null ? QuestStatus.NOT_STARTED : data.getPlayerQuestData().getStatus(quest.getQuestKey());
            ResourceLocation icon = BleachTextures.statusIcon(status.name());
            int slotY = bookY + BleachTextures.JOURNAL_SLOT_Y + i * BleachTextures.JOURNAL_SLOT_STRIDE;
            BleachTextures.blit(graphics, icon, bookX + 22, slotY + 4, 12, 12, 16, 16);
            int color = i == selectedIndex ? 0xE8C547 : 0xF2E9C8;
            graphics.drawString(this.font, Component.translatable(quest.getTitle()), bookX + 40, slotY + 5, color, false);
        }

        Quest quest = selectedQuest();
        if (quest != null) {
            int x = bookX + BleachTextures.JOURNAL_RIGHT_X;
            int y = bookY + 24;
            int wrap = BleachTextures.JOURNAL_RIGHT_WRAP;
            graphics.drawString(this.font, Component.translatable(quest.getTitle()), x, y, 0x3A2A1C, false);
            y += 12;
            graphics.drawWordWrap(this.font, Component.translatable(quest.getDescription()), x, y, wrap, 0x5A4A3A);
            y += 36;
            graphics.drawString(this.font, Component.translatable("screen.bleachmod.journal.objectives"), x, y, 0x7B5CFF, false);
            y += 12;
            QuestProgress progress = data == null ? null : data.getPlayerQuestData().getProgress(quest.getQuestKey());
            for (int i = 0; i < quest.getObjectives().size(); i++) {
                QuestObjective objective = quest.getObjectives().get(i);
                int current = progress == null ? 0 : progress.getObjectiveProgress(i);
                int required = progress == null ? objective.getRequired() : progress.getRequired(i, objective.getRequired());
                boolean kill = objective.getType() == QuestObjective.ObjectiveType.KILL;
                ResourceLocation objIcon = kill ? BleachTextures.ICON_OBJECTIVE_KILL : BleachTextures.ICON_OBJECTIVE_ITEM;
                int src = BleachTextures.objectiveSrc(kill);
                BleachTextures.blit(graphics, objIcon, x, y, 10, 10, src, src);
                graphics.drawString(this.font, Component.literal(current + "/" + required), x + 12, y + 1, 0x3A2A1C, false);
                y += 12;
            }
            y += 6;
            graphics.drawString(this.font, Component.translatable("screen.bleachmod.journal.rewards"), x, y, 0x7B5CFF, false);
            y += 12;
            int ri = 0;
            for (QuestReward reward : quest.getRewards()) {
                boolean claimed = progress != null && progress.isRewardClaimed(ri);
                graphics.drawString(this.font, reward.describe().copy().append(claimed ? " *" : ""), x, y,
                        claimed ? 0x5FA86A : 0x3A2A1C, false);
                y += 10;
                ri++;
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class SlotButton extends Button {
        private final java.util.function.BooleanSupplier selected;

        private SlotButton(int x, int y, int width, int height, Runnable onPress, java.util.function.BooleanSupplier selected) {
            super(x, y, width, height, Component.empty(), button -> onPress.run(), DEFAULT_NARRATION);
            this.selected = selected;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (isHoveredOrFocused() || selected.getAsBoolean()) {
                graphics.fill(getX(), getY(), getX() + width, getY() + height, 0x44E8C547);
            }
        }
    }
}
