package com.bleachmod.client.hud;

import com.bleachmod.client.BleachTextures;
import com.bleachmod.common.CombatBalance;
import com.bleachmod.common.data.AttributeData;
import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.data.PlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/**
 * Renders the Bleach concept HUD: a single panel showing VIDA / REIATSU / TRANSFORMACAO,
 * anchored top-left, with Spirit Points and the active Zanpakutō stage inside the
 * Transformacao row. Vanilla hearts are hidden by VanillaHealthHider once a character
 * exists, so this HUD owns the player's HP visualization.
 */
public final class ReiatsuHud {
    private static float shownCharge;
    private static long previousRender;

    private ReiatsuHud() {
    }

    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }
        PlayerCapability.get(mc.player).ifPresent(data -> {
            if (!data.isDataLoaded() || !data.getStatus().hasCreatedCharacter()) {
                return;
            }
            renderPanel(mc, graphics, screenWidth, screenHeight, data);
        });
    };

    private static void renderPanel(Minecraft mc, net.minecraft.client.gui.GuiGraphics g,
                                    int screenWidth, int screenHeight, PlayerData data) {
        float scale = BleachTextures.HUD_SCALE;
        int panelSrcW = BleachTextures.HUD_SRC_W;
        int panelSrcH = BleachTextures.HUD_SRC_H;
        int panelW = Math.round(panelSrcW * scale);
        int panelH = Math.round(panelSrcH * scale);
        int x = 6;
        int y = 6;

        // Panel base (hud_panel_full.png) drawn at scale.
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1.0F);
        BleachTextures.blit(g, BleachTextures.HUD_PANEL, 0, 0, panelSrcW, panelSrcH, panelSrcW, panelSrcH);
        g.pose().popPose();

        int barsX = BleachTextures.HUD_BARS_X;
        int barH = BleachTextures.HUD_BAR_H;
        int stride = BleachTextures.HUD_BAR_STRIDE;
        int firstBarY = BleachTextures.HUD_FIRST_BAR_Y;

        // Three bar rows: VIDA, REIATSU, TRANSFORMACAO.
        // Each row draws its fill on top of the panel base using its own slice of the panel
        // so that labels and base-frame stay aligned with the concept art.
        float healthRatio = computeHealthRatio(mc, data);
        float reiatsuRatio = Math.max(0f, Math.min(1f,
                data.getResources().getCurrentReiatsu() / Math.max(1f, data.getResources().getMaxReiatsu())));
        float transformRatio = computeTransformRatio(mc, data);

        drawConceptBar(g, mc, x, y, scale, barsX, firstBarY, barH, BleachTextures.HUD_HEALTH_FILL, healthRatio);
        drawConceptBar(g, mc, x, y, scale, barsX, firstBarY + stride, barH, BleachTextures.HUD_REIATSU_FILL, reiatsuRatio);
        drawConceptBar(g, mc, x, y, scale, barsX, firstBarY + stride * 2, barH, BleachTextures.HUD_TRANSFORM_FILL, transformRatio);

        // Percentage labels (drawn in code so they track the live value).
        drawPercentLabel(g, mc.font, x, y, scale, barsX, firstBarY, barH, healthRatio * 100f);
        drawPercentLabel(g, mc.font, x, y, scale, barsX, firstBarY + stride, barH, reiatsuRatio * 100f);
        drawPercentLabel(g, mc.font, x, y, scale, barsX, firstBarY + stride * 2, barH, transformRatio * 100f);

        // SP + Zanpakutō stage live inside the Transformacao row, below the bar fill.
        int infoY = y + Math.round((firstBarY + stride * 2 + barH + 12) * scale);
        int infoX = x + Math.round(barsX * scale);
        g.drawString(mc.font,
                Component.translatable("hud.bleachmod.reiatsu",
                        (int) data.getResources().getCurrentReiatsu(),
                        (int) data.getResources().getMaxReiatsu()),
                infoX, infoY, 0xE8C547, false);
        g.drawString(mc.font,
                Component.translatable("hud.bleachmod.target",
                        Component.translatable("form.bleachmod." + data.getCharacter().getActiveForm())),
                infoX, infoY + 11, 0xCAB7FF, false);
        g.drawString(mc.font,
                Component.translatable("hud.bleachmod.tp", (int) data.getResources().getTrainingPoints()),
                infoX, infoY + 22, 0xA0E8A0, false);
    }

    private static float computeHealthRatio(Minecraft mc, PlayerData data) {
        float max = computeMaxHealth(mc, data);
        if (max <= 0f) return 0f;
        return Math.max(0f, Math.min(1f, mc.player.getHealth() / max));
    }

    private static float computeMaxHealth(Minecraft mc, PlayerData data) {
        // Client-side: vanilla max HP plus the vitality bonus our server transient modifier adds.
        // The transient modifier is applied server-side only; mirror it locally so the bar matches
        // the real server HP.
        float base = (float) mc.player.getAttributeBaseValue(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        int vitalityRank = Math.max(0, data.getAttributes().level(AttributeData.VITALITY));
        return base + vitalityRank * CombatBalance.HEALTH_PER_VITALITY_RANK;
    }

    private static float computeTransformRatio(Minecraft mc, PlayerData data) {
        int target = data.getResources().getActionCharge();
        if (data.getStatus().isActionCharging()) {
            long now = System.nanoTime();
            float seconds = previousRender == 0L ? 0f : Math.min(.1f, (now - previousRender) / 1_000_000_000f);
            previousRender = now;
            shownCharge += (target - shownCharge) * Math.min(1f, seconds * 18f);
        } else {
            shownCharge = target;
            previousRender = 0L;
        }
        return Math.max(0f, Math.min(1f, shownCharge / 100f));
    }

    private static void drawConceptBar(net.minecraft.client.gui.GuiGraphics g, Minecraft mc, int panelX, int panelY,
                                       float scale, int srcX, int srcY, int srcH,
                                       ResourceLocation fillTexture, float ratio) {
        if (ratio <= 0f) {
            return;
        }
        // Posicao do slot no GUI: origem (x=srcX, y=srcY) dentro do PNG original,
        // escalada e ancorada em (panelX, panelY). O fill eh desenhado com altura
        // propria (HUD_FILL_SRC_H) e largura esticada (HUD_FILL_DRAW_W) para nao
        // distorcer as listras diagonais. Centralizamos verticalmente dentro do slot.
        int destX = panelX + Math.round((srcX + BleachTextures.HUD_FILL_OFFSET) * scale);
        int fillDestH = Math.round(BleachTextures.HUD_FILL_SRC_H * scale);
        int slotDestH = Math.round(srcH * scale);
        int destY = panelY + Math.round(srcY * scale) + (slotDestH - fillDestH) / 2;
        int destFullW = Math.round(BleachTextures.HUD_FILL_DRAW_W * scale);
        int destH = fillDestH;

        int visiblePixelW = Math.max(1, (int) Math.round(destFullW * ratio));

        // Scissor: limita o draw do fill ao retangulo GUI do slot preenchido.
        // RenderSystem usa coordenadas de framebuffer com Y invertido (origem inferior-esquerdo).
        // O blit do GuiGraphics opera em pixel GUI (origem superior-esquerdo); convertemos.
        double guiScale = mc.getWindow().getGuiScale();
        int fbH = mc.getWindow().getHeight();
        int scX = (int) Math.floor(destX * guiScale);
        int scW = Math.max(1, (int) Math.ceil(visiblePixelW * guiScale));
        int scH = Math.max(1, (int) Math.ceil(destH * guiScale));
        int scY = fbH - (int) Math.ceil(destY * guiScale) - scH;
        com.mojang.blaze3d.systems.RenderSystem.enableScissor(scX, scY, scW, scH);
        try {
            BleachTextures.blitSlice(g, fillTexture, destX, destY, destFullW, destH,
                    0, 0, BleachTextures.HUD_FILL_SRC_W, BleachTextures.HUD_FILL_SRC_H,
                    BleachTextures.HUD_FILL_SRC_W, BleachTextures.HUD_FILL_SRC_H);
        } finally {
            com.mojang.blaze3d.systems.RenderSystem.disableScissor();
        }
    }

    private static void drawPercentLabel(net.minecraft.client.gui.GuiGraphics g, net.minecraft.client.gui.Font font,
                                         int panelX, int panelY, float scale,
                                         int srcX, int srcY, int srcH, float percent) {
        int pct = Math.round(percent);
        String text = pct + "%";
        int rightX = panelX + Math.round((srcX + BleachTextures.HUD_FILL_OFFSET + BleachTextures.HUD_FILL_DRAW_W) * scale) - font.width(text) - 4;
        int centerY = panelY + Math.round((srcY + srcH / 2f) * scale) - 4;
        g.drawString(font, text, rightX + 1, centerY + 1, 0x80000000, false);
        g.drawString(font, text, rightX, centerY, 0xFFFFFFFF, false);
    }
}
