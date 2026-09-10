package com.bleachmod.client.hud;

import com.bleachmod.client.BleachTextures;
import com.bleachmod.common.data.PlayerCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public final class ReiatsuHud {
    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, width, height) -> render(graphics, height);

    private ReiatsuHud() {
    }

    private static void render(GuiGraphics graphics, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }
        PlayerCapability.get(mc.player).ifPresent(data -> {
            if (!data.isDataLoaded() || !data.getStatus().hasCreatedCharacter()) {
                return;
            }

            int x = 8;
            int y = height - 80;
            BleachTextures.blit(graphics, BleachTextures.ICON_REIATSU, x, y, BleachTextures.ICON, BleachTextures.ICON, 16, 16);
            int barX = x + 18;
            BleachTextures.blitNative(graphics, BleachTextures.REIATSU_FRAME, barX, y, BleachTextures.BAR_W, BleachTextures.BAR_H);
            float ratio = data.getResources().getMaxReiatsu() <= 0 ? 0
                    : data.getResources().getCurrentReiatsu() / data.getResources().getMaxReiatsu();
            BleachTextures.blitBarFill(graphics, BleachTextures.REIATSU_FILL, barX, y, (int) (BleachTextures.BAR_W * ratio));
            graphics.drawString(mc.font, Component.literal(
                            (int) data.getResources().getCurrentReiatsu() + "/" + (int) data.getResources().getMaxReiatsu()),
                    barX + 4, y + 4, 0xF2E9C8, false);

            String form = data.getCharacter().getActiveForm().isEmpty() ? "sealed" : data.getCharacter().getActiveForm();
            int stageY = y + 20;
            BleachTextures.blit(graphics, BleachTextures.formIcon(form), x, stageY, BleachTextures.ICON, BleachTextures.ICON,
                    BleachTextures.formSrc(form), BleachTextures.formSrc(form));
            graphics.drawString(mc.font, Component.translatable("form.bleachmod." + form), x + 18, stageY + 4, 0xF2E9C8, false);

            if (data.getStatus().isActionCharging() || data.getResources().getActionCharge() > 0) {
                int chargeY = y + 38;
                BleachTextures.blitNative(graphics, BleachTextures.CHARGE_FRAME, barX, chargeY, BleachTextures.BAR_W, BleachTextures.BAR_H);
                BleachTextures.blitBarFill(graphics, BleachTextures.CHARGE_FILL, barX, chargeY,
                        (int) (BleachTextures.BAR_W * (data.getResources().getActionCharge() / 100.0F)));
            }

            int tpY = y + 56;
            BleachTextures.blit(graphics, BleachTextures.ICON_TP, x, tpY, BleachTextures.ICON, BleachTextures.ICON, 16, 16);
            graphics.drawString(mc.font, Component.translatable("hud.bleachmod.tp",
                    (int) data.getResources().getTrainingPoints()), x + 18, tpY + 4, 0xA0E8A0, false);
        });
    }
}
