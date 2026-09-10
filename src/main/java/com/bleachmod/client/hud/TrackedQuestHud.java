package com.bleachmod.client.hud;

import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.quest.Quest;
import com.bleachmod.common.quest.QuestProgress;
import com.bleachmod.common.quest.QuestRegistry;
import com.bleachmod.common.quest.objectives.QuestObjective;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
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
            int x = width - 180;
            int y = 8;
            graphics.fill(x - 4, y - 4, width - 4, y + 48, 0x66000000);
            graphics.drawString(mc.font, Component.translatable(quest.getTitle()), x, y, 0xFFE8C547, false);
            int line = y + 12;
            for (int i = 0; i < quest.getObjectives().size(); i++) {
                QuestObjective objective = quest.getObjectives().get(i);
                int current = progress == null ? 0 : progress.getObjectiveProgress(i);
                int required = progress == null ? objective.getRequired() : progress.getRequired(i, objective.getRequired());
                graphics.drawString(mc.font, Component.literal(current + "/" + required + " ").append(objective.describe()), x, line, 0xFFFFFFFF, false);
                line += 10;
            }
        });
    }
}
