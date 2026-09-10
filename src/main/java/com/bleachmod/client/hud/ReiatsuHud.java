package com.bleachmod.client.hud;

import com.bleachmod.common.data.PlayerCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public final class ReiatsuHud {
    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, width, height) -> render(graphics, width, height);

    private ReiatsuHud() {
    }

    private static void render(GuiGraphics graphics, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }
        PlayerCapability.get(mc.player).ifPresent(data -> {
            if (!data.isDataLoaded() || !data.getStatus().hasCreatedCharacter()) {
                return;
            }
            int x = 8;
            int y = height - 72;
            int barWidth = 120;
            int barHeight = 8;
            float ratio = data.getResources().getMaxReiatsu() <= 0 ? 0
                    : data.getResources().getCurrentReiatsu() / data.getResources().getMaxReiatsu();
            graphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xFF101010);
            graphics.fill(x, y, x + barWidth, y + barHeight, 0xFF2A1B4A);
            graphics.fill(x, y, x + (int) (barWidth * ratio), y + barHeight, 0xFF7B5CFF);
            graphics.drawString(mc.font, Component.translatable("hud.bleachmod.reiatsu",
                    (int) data.getResources().getCurrentReiatsu(),
                    (int) data.getResources().getMaxReiatsu()), x, y - 10, 0xE0D4FF, false);

            String form = data.getCharacter().getActiveForm().isEmpty() ? "sealed" : data.getCharacter().getActiveForm();
            graphics.drawString(mc.font, Component.translatable("hud.bleachmod.stage",
                    Component.translatable("form.bleachmod." + form)), x, y + 12, 0xF2E9C8, false);

            if (data.getStatus().isActionCharging() || data.getResources().getActionCharge() > 0) {
                int chargeY = y + 24;
                graphics.fill(x - 1, chargeY - 1, x + barWidth + 1, chargeY + 5, 0xFF101010);
                graphics.fill(x, chargeY, x + (int) (barWidth * (data.getResources().getActionCharge() / 100.0F)), chargeY + 4, 0xFFE8C547);
            }

            graphics.drawString(mc.font, Component.translatable("hud.bleachmod.tp",
                    (int) data.getResources().getTrainingPoints()), x, y + 32, 0xA0E8A0, false);
            com.bleachmod.client.gui.StoryToastManager.render(graphics, width);
        });
    }
}
