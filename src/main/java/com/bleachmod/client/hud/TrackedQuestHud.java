package com.bleachmod.client.hud;

import com.bleachmod.client.BleachTextures;
import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.quest.Quest;
import com.bleachmod.common.quest.QuestProgress;
import com.bleachmod.common.quest.QuestRegistry;
import com.bleachmod.common.quest.objectives.QuestObjective;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public final class TrackedQuestHud {
    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, width, height) -> render(graphics, width);

    private TrackedQuestHud() {
    }

    private static void render(GuiGraphics graphics, int width) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }
        PlayerCapability.get(mc.player).ifPresent(data -> {
            String tracked = data.getPlayerQuestData().getTrackedQuestId();
            if (tracked == null || tracked.isBlank()) {
                return;
            }
            Quest quest = QuestRegistry.getQuest(tracked);
            if (quest == null) {
                return;
            }
            QuestProgress progress = data.getPlayerQuestData().getProgress(tracked);
            int panelW = BleachTextures.QUEST_PANEL_W;
            int panelH = BleachTextures.QUEST_PANEL_H;
            int x = width - panelW - 6;
            int y = 6;
            BleachTextures.blit(graphics, BleachTextures.QUEST_PANEL, x, y, panelW, panelH,
                    BleachTextures.QUEST_PANEL_SRC_W, BleachTextures.QUEST_PANEL_SRC_H);

            int textX = x + BleachTextures.QUEST_TEXT_X;
            graphics.drawString(mc.font, Component.translatable(quest.getTitle()), textX, y + 10, 0xE8C547, false);
            int line = y + 24;
            for (int i = 0; i < quest.getObjectives().size() && i < 2; i++) {
                QuestObjective objective = quest.getObjectives().get(i);
                int current = progress == null ? 0 : progress.getObjectiveProgress(i);
                int required = progress == null ? objective.getRequired() : progress.getRequired(i, objective.getRequired());
                boolean kill = objective.getType() == QuestObjective.ObjectiveType.KILL;
                ResourceLocation icon = kill ? BleachTextures.ICON_OBJECTIVE_KILL : BleachTextures.ICON_OBJECTIVE_ITEM;
                int src = BleachTextures.objectiveSrc(kill);
                BleachTextures.blit(graphics, icon, textX, line, 10, 10, src, src);
                graphics.drawString(mc.font, Component.literal(current + "/" + required), textX + 12, line + 1, 0xF2E9C8, false);
                line += 12;
            }
        });
    }
}
