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

import java.util.List;

/**
 * Renders the compact Bleach HUD: VIDA / REIATSU / TRANSFORMACAO stay in a
 * responsive panel at the upper-left, while the detailed resource information
 * sits beside the hotbar. Vanilla hearts are hidden by VanillaHealthHider once a
 * character exists, so this HUD owns the player's HP visualization.
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
        HudLayout.Panel panel = HudLayout.panel(screenWidth, screenHeight);
        float scale = panel.scale();
        int panelSrcW = BleachTextures.HUD_SRC_W;
        int panelSrcH = BleachTextures.HUD_SRC_H;
        int x = panel.x();
        int y = panel.y();

        // Panel base (hud_panel_full.png) drawn at scale.
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1.0F);
        BleachTextures.blit(g, BleachTextures.HUD_PANEL, 0, 0, panelSrcW, panelSrcH, panelSrcW, panelSrcH);
        g.pose().popPose();

        // Three bar rows: VIDA, REIATSU, TRANSFORMACAO.
        // Each fill uses the measured bounds of its own well. The three rows have
        // different left edges and widths because their labels and right caps differ.
        float healthRatio = computeHealthRatio(mc, data);
        float reiatsuRatio = Math.max(0f, Math.min(1f,
                data.getResources().getCurrentReiatsu() / Math.max(1f, data.getResources().getMaxReiatsu())));
        float transformRatio = computeTransformRatio(mc, data);

        drawConceptBar(g, x, y, scale,
                BleachTextures.HUD_HEALTH_BOUNDS,
                BleachTextures.HUD_HEALTH_FILL, healthRatio);
        drawConceptBar(g, x, y, scale,
                BleachTextures.HUD_REIATSU_BOUNDS,
                BleachTextures.HUD_REIATSU_FILL, reiatsuRatio);
        drawConceptBar(g, x, y, scale,
                BleachTextures.HUD_TRANSFORM_BOUNDS,
                BleachTextures.HUD_TRANSFORM_FILL, transformRatio);

        // Percentage labels (drawn in code so they track the live value).
        drawPercentLabel(g, mc.font, x, y, scale,
                BleachTextures.HUD_HEALTH_BOUNDS, healthRatio * 100f);
        drawPercentLabel(g, mc.font, x, y, scale,
                BleachTextures.HUD_REIATSU_BOUNDS, reiatsuRatio * 100f);
        drawPercentLabel(g, mc.font, x, y, scale,
                BleachTextures.HUD_TRANSFORM_BOUNDS, transformRatio * 100f);

        drawInformation(g, mc, screenWidth, screenHeight, data);
    }

    private static void drawInformation(net.minecraft.client.gui.GuiGraphics g, Minecraft mc,
                                        int screenWidth, int screenHeight, PlayerData data) {
        List<InfoLine> lines = List.of(
                new InfoLine(Component.translatable("hud.bleachmod.reiatsu",
                        (int) data.getResources().getCurrentReiatsu(),
                        (int) data.getResources().getMaxReiatsu()), 0xE8C547),
                new InfoLine(Component.translatable("hud.bleachmod.target",
                        Component.translatable("form.bleachmod." + data.getCharacter().getActiveForm())), 0xCAB7FF),
                new InfoLine(Component.translatable("hud.bleachmod.tp",
                        (int) data.getResources().getTrainingPoints()), 0xA0E8A0)
        );
        int lineStep = mc.font.lineHeight + 2;
        int blockWidth = lines.stream().mapToInt(line -> mc.font.width(line.text())).max().orElse(0);
        int blockHeight = mc.font.lineHeight + lineStep * (lines.size() - 1);
        HudLayout.Point position = HudLayout.information(screenWidth, screenHeight, blockWidth, blockHeight);

        for (int index = 0; index < lines.size(); index++) {
            InfoLine line = lines.get(index);
            g.drawString(mc.font, line.text(), position.x(), position.y() + index * lineStep,
                    line.color(), true);
        }
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

    private static void drawConceptBar(net.minecraft.client.gui.GuiGraphics g, int panelX, int panelY,
                                       float scale, BleachTextures.HudBarBounds bounds,
                                       ResourceLocation fillTexture, float ratio) {
        if (ratio <= 0f) {
            return;
        }
        int destY = panelY + Math.round(bounds.y() * scale);
        int destH = Math.max(1, Math.round(bounds.height() * scale));

        // Draw one GUI-pixel slice at a time. Both sides interpolate between their
        // measured top and bottom positions, producing the same slanted well as the
        // reference instead of a rectangular fill that crosses the frame.
        for (int row = 0; row < destH; row++) {
            float t = destH == 1 ? 0.5F : (row + 0.5F) / destH;
            float sourceRow = t * bounds.height();
            int left = panelX + Math.round(lerp(bounds.topX(), bounds.bottomX(), t) * scale);
            int right = panelX + Math.round(lerp(bounds.topRight(), bounds.bottomRight(), t) * scale);
            int fullWidth = Math.max(1, right - left);
            int visibleWidth = Math.max(1, Math.round(fullWidth * ratio));
            int srcY = Math.min(BleachTextures.HUD_FILL_SRC_H - 1,
                    (int) (sourceRow * BleachTextures.HUD_FILL_SRC_H / bounds.height()));

            BleachTextures.blitSlice(g, fillTexture, left, destY + row, visibleWidth, 1,
                    0, srcY,
                    Math.max(1, Math.round(BleachTextures.HUD_FILL_SRC_W * ratio)), 1,
                    BleachTextures.HUD_FILL_SRC_W, BleachTextures.HUD_FILL_SRC_H);
        }
    }

    private static float lerp(int start, int end, float amount) {
        return start + (end - start) * amount;
    }

    private static void drawPercentLabel(net.minecraft.client.gui.GuiGraphics g, net.minecraft.client.gui.Font font,
                                         int panelX, int panelY, float scale,
                                         BleachTextures.HudBarBounds bounds, float percent) {
        int pct = Math.round(percent);
        String text = pct + "%";
        float textScale = Math.max(0.72F, Math.min(1.0F, scale / 0.14F));
        int rightX = panelX + Math.round(bounds.percentRight() * scale);
        int centerY = panelY + Math.round((bounds.y() + bounds.height() / 2f) * scale);
        g.pose().pushPose();
        g.pose().translate(rightX, centerY, 0);
        g.pose().scale(textScale, textScale, 1.0F);
        g.drawString(font, text, -font.width(text), -font.lineHeight / 2, 0xFFFFFFFF, true);
        g.pose().popPose();
    }

    private record InfoLine(Component text, int color) {
    }
}
