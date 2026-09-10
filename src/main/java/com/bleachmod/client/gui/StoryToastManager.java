package com.bleachmod.client.gui;

import com.bleachmod.client.BleachTextures;
import com.bleachmod.common.network.s2c.StoryToastS2C;
import com.bleachmod.common.quest.Quest;
import com.bleachmod.common.quest.QuestRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
        int toastW = BleachTextures.TOAST_W;
        int toastH = BleachTextures.TOAST_H;
        int x = width / 2 - toastW / 2;
        int y = 8;
        BleachTextures.blit(graphics, BleachTextures.TOAST_BG, x, y, toastW, toastH,
                BleachTextures.TOAST_SRC_W, BleachTextures.TOAST_SRC_H);
        int iconSrc = iconSrc(type);
        BleachTextures.blit(graphics, iconFor(type), x + BleachTextures.TOAST_ICON_X, y + BleachTextures.TOAST_ICON_Y,
                16, 16, iconSrc, iconSrc);
        graphics.drawString(mc.font, title, x + BleachTextures.TOAST_TEXT_X, y + 12, 0x3A2A1C, false);
        graphics.drawString(mc.font, subtitle, x + BleachTextures.TOAST_TEXT_X, y + 24, 0x1A1028, false);
    }

    private static ResourceLocation iconFor(StoryToastS2C.ToastType toastType) {
        return switch (toastType) {
            case START -> BleachTextures.TOAST_START;
            case OBJECTIVE -> BleachTextures.TOAST_OBJECTIVE;
            case COMPLETE -> BleachTextures.TOAST_COMPLETE;
            case FAIL -> BleachTextures.TOAST_FAIL;
            case CLAIM -> BleachTextures.TOAST_CLAIM;
        };
    }

    private static int iconSrc(StoryToastS2C.ToastType toastType) {
        return switch (toastType) {
            case START -> BleachTextures.TOAST_START_SRC;
            case OBJECTIVE -> BleachTextures.TOAST_OBJECTIVE_SRC;
            case COMPLETE -> BleachTextures.TOAST_COMPLETE_SRC;
            case FAIL -> BleachTextures.TOAST_FAIL_SRC;
            case CLAIM -> BleachTextures.TOAST_CLAIM_SRC;
        };
    }
}
