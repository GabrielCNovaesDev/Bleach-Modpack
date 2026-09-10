package com.bleachmod.client.gui;

import com.bleachmod.common.network.s2c.StoryToastS2C;
import com.bleachmod.common.quest.Quest;
import com.bleachmod.common.quest.QuestRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class StoryToastManager {
    private static Component title = Component.empty();
    private static Component subtitle = Component.empty();
    private static long untilMs;

    private StoryToastManager() {
    }

    public static void show(StoryToastS2C msg) {
        Quest quest = QuestRegistry.getQuest(msg.questId());
        Component questName = quest == null ? Component.literal(msg.questId()) : Component.translatable(quest.getTitle());
        title = switch (msg.type()) {
            case START -> Component.translatable("toast.bleachmod.quest.start");
            case OBJECTIVE -> Component.translatable("toast.bleachmod.quest.objective");
            case COMPLETE -> Component.translatable("toast.bleachmod.quest.complete");
            case FAIL -> Component.translatable("toast.bleachmod.quest.fail");
            case CLAIM -> Component.translatable("toast.bleachmod.quest.claim");
        };
        subtitle = questName;
        untilMs = System.currentTimeMillis() + 3500L;
    }

    public static void render(GuiGraphics graphics, int width) {
        if (System.currentTimeMillis() > untilMs) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        int x = width / 2 - 90;
        int y = 18;
        graphics.fill(x, y, x + 180, y + 28, 0xAA1A1028);
        graphics.drawCenteredString(mc.font, title, width / 2, y + 4, 0xFFE8C547);
        graphics.drawCenteredString(mc.font, subtitle, width / 2, y + 14, 0xFFFFFFFF);
    }
}
