package com.bleachmod.client.gui;

import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.data.PlayerData;
import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.common.network.c2s.QuestActionC2S;
import com.bleachmod.common.quest.Quest;
import com.bleachmod.common.quest.QuestAvailabilityChecker;
import com.bleachmod.common.quest.QuestProgress;
import com.bleachmod.common.quest.QuestRegistry;
import com.bleachmod.common.quest.QuestStatus;
import com.bleachmod.entity.QuestNpcEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public final class NpcQuestScreen extends Screen {
    private final String npcId;
    private final int entityId;
    private List<Quest> quests = List.of();
    private final List<Button> slots = new ArrayList<>();
    private String selected;
    private int page;
    private int scroll;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private int split;
    private Button accept;
    private Button previous;
    private Button next;

    public NpcQuestScreen(String npcId, int entityId) {
        super(Component.translatable("screen.bleachmod.npc_quests"));
        this.npcId = npcId;
        this.entityId = entityId;
    }

    private PlayerData data() {
        return minecraft == null || minecraft.player == null
                ? null : PlayerCapability.get(minecraft.player).orElse(null);
    }

    private Quest selectedQuest() {
        return selected == null ? null : QuestRegistry.getQuest(selected);
    }

    @Override
    protected void init() {
        quests = QuestRegistry.questsForNpc(npcId);
        if (selectedQuest() == null && !quests.isEmpty()) {
            selected = quests.get(0).getQuestKey();
        }
        panelWidth = Math.min(480, width - 16);
        panelHeight = Math.min(280, height - 44);
        left = (width - panelWidth) / 2;
        top = 8;
        split = left + panelWidth * 2 / 5;
        slots.clear();
        for (int i = 0; i < 5; i++) {
            final int slot = i;
            slots.add(addRenderableWidget(Button.builder(Component.empty(), button -> {
                int index = page * 5 + slot;
                if (index < quests.size()) {
                    selected = quests.get(index).getQuestKey();
                    scroll = 0;
                    refresh();
                }
            }).bounds(left + 8, top + 42 + i * 24, split - left - 16, 20).build()));
        }
        previous = addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            page--;
            refresh();
        }).bounds(left + 8, top + 166, 30, 20).build());
        next = addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            page++;
            refresh();
        }).bounds(split - 38, top + 166, 30, 20).build());
        int buttonY = top + panelHeight + 4;
        accept = addRenderableWidget(Button.builder(Component.translatable("screen.bleachmod.npc_quests.accept"), button -> {
            Quest quest = selectedQuest();
            if (quest != null) {
                NetworkHandler.sendToServer(new QuestActionC2S(QuestActionC2S.Action.START, quest.getQuestKey(), entityId));
            }
        }).bounds(left, buttonY, (panelWidth - 4) / 2, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(left + (panelWidth + 4) / 2, buttonY, (panelWidth - 4) / 2, 20).build());
        refresh();
    }

    @Override
    public void tick() {
        if (minecraft == null || minecraft.level == null || minecraft.player == null) {
            onClose();
            return;
        }
        Entity entity = minecraft.level.getEntity(entityId);
        if (!(entity instanceof QuestNpcEntity) || minecraft.player.distanceToSqr(entity) > 64.0D) {
            onClose();
            return;
        }
        List<Quest> current = QuestRegistry.questsForNpc(npcId);
        if (!current.equals(quests)) {
            quests = current;
            page = 0;
            if (selectedQuest() == null) {
                selected = quests.isEmpty() ? null : quests.get(0).getQuestKey();
            }
        }
        refresh();
    }

    private void refresh() {
        page = Math.max(0, Math.min(page, Math.max(0, (quests.size() - 1) / 5)));
        PlayerData playerData = data();
        for (int i = 0; i < slots.size(); i++) {
            int index = page * 5 + i;
            Button button = slots.get(i);
            button.visible = index < quests.size();
            if (button.visible) {
                Quest quest = quests.get(index);
                QuestStatus status = playerData == null ? QuestStatus.NOT_STARTED
                        : playerData.getPlayerQuestData().getStatus(quest.getQuestKey());
                String marker = switch (status) {
                    case ACCEPTED -> "> ";
                    case SUCCESS -> "+ ";
                    case FAILED -> "! ";
                    default -> "  ";
                };
                String name = Component.translatable(quest.getTitle()).getString();
                button.setMessage(Component.literal(marker + font.plainSubstrByWidth(name, button.getWidth() - 20)));
            }
        }
        previous.active = page > 0;
        next.active = (page + 1) * 5 < quests.size();
        Quest quest = selectedQuest();
        QuestProgress progress = quest == null || playerData == null ? null
                : playerData.getPlayerQuestData().getProgress(quest.getQuestKey());
        boolean compatible = progress == null || progress.matchesDefinition(quest);
        accept.active = quest != null && playerData != null && compatible
                && QuestAvailabilityChecker.isAvailable(playerData, quest.getQuestKey());
    }

    private List<Component> details() {
        List<Component> result = new ArrayList<>();
        Quest quest = selectedQuest();
        PlayerData playerData = data();
        if (quest == null) {
            result.add(Component.translatable("screen.bleachmod.npc_quests.empty"));
            return result;
        }
        QuestProgress progress = playerData == null ? null
                : playerData.getPlayerQuestData().getProgress(quest.getQuestKey());
        result.add(Component.translatable(quest.getTitle()));
        result.add(Component.translatable(quest.getDescription()));
        result.add(Component.empty());
        if (progress != null && !progress.matchesDefinition(quest)) {
            result.add(Component.translatable("message.bleachmod.quest.changed"));
        } else if (playerData != null && !QuestAvailabilityChecker.isAvailable(playerData, quest.getQuestKey())
                && playerData.getPlayerQuestData().getStatus(quest.getQuestKey()) == QuestStatus.NOT_STARTED) {
            result.add(Component.translatable("screen.bleachmod.quest_locked"));
        }
        result.add(Component.translatable("screen.bleachmod.journal.objectives"));
        for (int i = 0; i < quest.getObjectives().size(); i++) {
            var objective = quest.getObjectives().get(i);
            int current = progress == null ? 0 : progress.getObjectiveProgress(i);
            int required = progress == null ? objective.getRequired() : progress.getRequired(i, objective.getRequired());
            result.add(objective.describe().copy().append(" [" + current + "/" + required + "]"));
        }
        result.add(Component.empty());
        result.add(Component.translatable("screen.bleachmod.journal.rewards"));
        for (int i = 0; i < quest.getRewards().size(); i++) {
            result.add(quest.getRewards().get(i).describe().copy()
                    .append(progress != null && progress.isRewardClaimed(i) ? " ✓" : ""));
        }
        return result;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll = Math.max(0, scroll - (int) (delta * 20));
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xF0171321);
        graphics.fill(split, top + 38, left + panelWidth - 6, top + panelHeight - 6, 0xFFDFD2AB);
        graphics.drawCenteredString(font, Component.translatable("entity.bleachmod.npc_" + npcId),
                width / 2, top + 10, 0xE8C547);
        graphics.drawString(font, title, left + 8, top + 26, 0xE8C547, false);
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (Component text : details()) {
            lines.addAll(font.split(text, left + panelWidth - split - 20));
        }
        int available = panelHeight - 50;
        scroll = Math.min(scroll, Math.max(0, lines.size() * 12 - available));
        graphics.enableScissor(split + 4, top + 42, left + panelWidth - 8, top + panelHeight - 8);
        int y = top + 44 - scroll;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, split + 8, y, 0x302318, false);
            y += 12;
        }
        graphics.disableScissor();
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
