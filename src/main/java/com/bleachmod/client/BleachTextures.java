package com.bleachmod.client;

import com.bleachmod.Reference;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class BleachTextures {
    public static final ResourceLocation REIATSU_FRAME = gui("hud/reiatsu_frame.png");
    public static final ResourceLocation REIATSU_FILL = gui("hud/reiatsu_fill.png");
    public static final ResourceLocation CHARGE_FRAME = gui("hud/charge_frame.png");
    public static final ResourceLocation CHARGE_FILL = gui("hud/charge_fill.png");
    public static final ResourceLocation ICON_REIATSU = gui("hud/icon_reiatsu.png");
    public static final ResourceLocation ICON_TP = gui("hud/icon_tp.png");
    public static final ResourceLocation QUEST_PANEL = gui("hud/quest_panel.png");
    public static final ResourceLocation ICON_OBJECTIVE_KILL = gui("hud/icon_objective_kill.png");
    public static final ResourceLocation ICON_OBJECTIVE_ITEM = gui("hud/icon_objective_item.png");
    public static final ResourceLocation ICON_QUEST_TRACK = gui("hud/icon_quest_track.png");

    public static final ResourceLocation FORM_SEALED = gui("forms/sealed.png");
    public static final ResourceLocation FORM_SHIKAI = gui("forms/shikai.png");
    public static final ResourceLocation FORM_BANKAI = gui("forms/bankai.png");

    public static final ResourceLocation TOAST_BG = gui("toast/toast_bg.png");
    public static final ResourceLocation TOAST_START = gui("toast/start.png");
    public static final ResourceLocation TOAST_OBJECTIVE = gui("toast/objective.png");
    public static final ResourceLocation TOAST_COMPLETE = gui("toast/complete.png");
    public static final ResourceLocation TOAST_FAIL = gui("toast/fail.png");
    public static final ResourceLocation TOAST_CLAIM = gui("toast/claim.png");

    public static final ResourceLocation CONFIRM_BG = gui("character/confirm_bg.png");
    public static final ResourceLocation RACE_SHINIGAMI = gui("character/race_shinigami.png");

    public static final ResourceLocation JOURNAL_BG = gui("journal/journal_bg.png");
    public static final ResourceLocation JOURNAL_HEADER = gui("journal/journal_header.png");
    public static final ResourceLocation STATUS_NOT_STARTED = gui("journal/status_not_started.png");
    public static final ResourceLocation STATUS_ACCEPTED = gui("journal/status_accepted.png");
    public static final ResourceLocation STATUS_SUCCESS = gui("journal/status_success.png");
    public static final ResourceLocation STATUS_FAILED = gui("journal/status_failed.png");

    public static final int BAR_W = 128;
    public static final int BAR_H = 16;
    public static final int ICON = 16;
    public static final int JOURNAL_W = 320;
    public static final int JOURNAL_H = 200;
    public static final int HEADER_W = 256;
    public static final int HEADER_H = 32;
    public static final int JOURNAL_SLOT_COUNT = 5;
    public static final int JOURNAL_SLOT_X = 18;
    public static final int JOURNAL_SLOT_Y = 34;
    public static final int JOURNAL_SLOT_W = 126;
    public static final int JOURNAL_SLOT_H = 19;
    public static final int JOURNAL_SLOT_STRIDE = 29;
    public static final int JOURNAL_RIGHT_X = 176;
    public static final int JOURNAL_RIGHT_WRAP = 114;
    public static final int QUEST_PANEL_W = 186;
    public static final int QUEST_PANEL_H = 56;
    public static final int QUEST_TEXT_X = 34;
    public static final int TOAST_W = 256;
    public static final int TOAST_H = 46;
    public static final int TOAST_ICON_X = 16;
    public static final int TOAST_ICON_Y = 14;
    public static final int TOAST_TEXT_X = 50;
    public static final int CONFIRM_BG_W = 1448;
    public static final int CONFIRM_BG_H = 1086;
    public static final int PORTRAIT_SRC = 1190;
    public static final int PORTRAIT_DEST = 128;
    public static final int QUEST_PANEL_SRC_W = 2156;
    public static final int QUEST_PANEL_SRC_H = 655;
    public static final int TOAST_SRC_W = 2137;
    public static final int TOAST_SRC_H = 381;
    public static final int ICON_OBJECTIVE_KILL_SRC = 1022;
    public static final int ICON_OBJECTIVE_ITEM_SRC = 994;
    public static final int FORM_SEALED_SRC = 751;
    public static final int FORM_SHIKAI_SRC = 1100;
    public static final int FORM_BANKAI_SRC = 671;
    public static final int TOAST_START_SRC = 812;
    public static final int TOAST_OBJECTIVE_SRC = 764;
    public static final int TOAST_COMPLETE_SRC = 896;
    public static final int TOAST_FAIL_SRC = 987;
    public static final int TOAST_CLAIM_SRC = 663;

    private BleachTextures() {
    }

    public static ResourceLocation gui(String path) {
        return Reference.id("textures/gui/" + path);
    }

    public static ResourceLocation formIcon(String form) {
        return switch (form) {
            case Reference.FORM_SHIKAI -> FORM_SHIKAI;
            case Reference.FORM_BANKAI -> FORM_BANKAI;
            default -> FORM_SEALED;
        };
    }

    public static int formSrc(String form) {
        return switch (form) {
            case Reference.FORM_SHIKAI -> FORM_SHIKAI_SRC;
            case Reference.FORM_BANKAI -> FORM_BANKAI_SRC;
            default -> FORM_SEALED_SRC;
        };
    }

    public static int objectiveSrc(boolean kill) {
        return kill ? ICON_OBJECTIVE_KILL_SRC : ICON_OBJECTIVE_ITEM_SRC;
    }

    public static ResourceLocation statusIcon(String status) {
        return switch (status) {
            case "ACCEPTED" -> STATUS_ACCEPTED;
            case "SUCCESS" -> STATUS_SUCCESS;
            case "FAILED" -> STATUS_FAILED;
            default -> STATUS_NOT_STARTED;
        };
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int destW, int destH, int srcW, int srcH) {
        graphics.blit(texture, x, y, destW, destH, 0.0F, 0.0F, srcW, srcH, srcW, srcH);
    }

    public static void blitNative(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height) {
        graphics.blit(texture, x, y, 0, 0, width, height, width, height);
    }

    public static void blitBarFill(GuiGraphics graphics, ResourceLocation texture, int x, int y, int fillWidth) {
        if (fillWidth <= 0) {
            return;
        }
        graphics.blit(texture, x, y, 0, 0, Math.min(fillWidth, BAR_W), BAR_H, BAR_W, BAR_H);
    }
}
