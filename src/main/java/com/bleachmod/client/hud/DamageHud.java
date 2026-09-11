package com.bleachmod.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.Locale;

public final class DamageHud {
    private static final long DISPLAY_MILLIS = 1200L;
    private static float damage;
    private static long visibleUntil;

    private DamageHud() {}

    public static void show(float inflictedDamage) {
        if (!Float.isFinite(inflictedDamage) || inflictedDamage <= 0) return;
        damage = inflictedDamage;
        visibleUntil = System.currentTimeMillis() + DISPLAY_MILLIS;
    }

    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, width, height) -> {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || System.currentTimeMillis() >= visibleUntil) return;
        String value = Math.abs(damage - Math.round(damage)) < 0.05F
                ? Integer.toString(Math.round(damage))
                : String.format(Locale.ROOT, "%.1f", damage);
        Component text = Component.literal("DMG : " + value);
        graphics.drawCenteredString(minecraft.font, text, width / 2, height / 2 + 18, 0xFFFFFF);
    };
}
