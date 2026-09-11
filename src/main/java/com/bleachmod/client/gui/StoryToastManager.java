package com.bleachmod.client.gui;


import com.bleachmod.common.network.s2c.StoryToastS2C;
import com.bleachmod.common.quest.Quest;
import com.bleachmod.common.quest.QuestRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public final class StoryToastManager {
    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, width, height) -> render(graphics, width);

    private static Component title = Component.empty();
    private static Component subtitle = Component.empty();
    private static StoryToastS2C.ToastType type = StoryToastS2C.ToastType.START;
    private static long untilMs;
    private static final java.util.ArrayDeque<StoryToastS2C> pending = new java.util.ArrayDeque<>();
    public static void clear() { pending.clear(); untilMs = 0; }

    private StoryToastManager() {
    }

    public static void show(StoryToastS2C msg) {
        if (System.currentTimeMillis() < untilMs) {
            if (pending.size() < 8) pending.addLast(msg);
            return;
        }
        Quest quest = QuestRegistry.getQuest(msg.questId());
        Component questName = quest == null ? Component.literal(msg.questId()) : Component.translatable(quest.getTitle());
        type = msg.type();
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
            if (pending.isEmpty()) return;
            show(pending.removeFirst());
        }
        Minecraft mc = Minecraft.getInstance();
        int toastW = Math.min(280, width - 16);
        int x = (width - toastW) / 2, y = 8;
        var lines = mc.font.split(subtitle, toastW - 40);
        int toastH = 30 + Math.min(2, lines.size()) * 11;
        int accent = type == StoryToastS2C.ToastType.FAIL ? 0xFFE07B7B : 0xFFE8C547;
        graphics.fill(x, y, x + toastW, y + toastH, 0xF0171321);
        graphics.fill(x, y, x + 3, y + toastH, accent);
        String mark = switch (type) {
            case START -> ">";
            case OBJECTIVE, COMPLETE -> "+";
            case FAIL -> "!";
            case CLAIM -> "*";
        };
        graphics.drawString(mc.font, mark, x + 12, y + 12, accent, false);
        graphics.drawString(mc.font, mc.font.plainSubstrByWidth(title.getString(), toastW - 40), x + 30, y + 9, accent, false);
        for (int i = 0; i < Math.min(2, lines.size()); i++)
            graphics.drawString(mc.font, lines.get(i), x + 30, y + 23 + i * 11, 0xFFF2E9C8, false);
    }
}