package com.bleachmod.client.hud;

/**
 * Keeps the custom HUD readable without letting Minecraft's larger GUI scales
 * turn it into a full-width overlay.
 */
public final class HudLayout {
    private static final int PANEL_SOURCE_WIDTH = 2169;
    private static final int PANEL_SOURCE_HEIGHT = 725;
    private static final float TARGET_SCREEN_WIDTH = 0.58F;
    private static final float MIN_PANEL_SCALE = 0.09F;
    private static final float MAX_PANEL_SCALE = 0.14F;
    private static final int HOTBAR_WIDTH = 182;
    private static final int HOTBAR_TOP_OFFSET = 22;
    private static final int HOTBAR_GAP = 6;

    private HudLayout() {
    }

    public static Panel panel(int screenWidth, int screenHeight) {
        int x = clamp(Math.round(screenWidth * 0.02F), 6, 12);
        int y = clamp(Math.round(screenHeight * 0.025F), 4, 8);
        float desiredScale = screenWidth * TARGET_SCREEN_WIDTH / PANEL_SOURCE_WIDTH;
        float fitScale = Math.max(0.01F,
                (screenWidth - x * 2F) / PANEL_SOURCE_WIDTH);
        float scale = Math.min(clamp(desiredScale, MIN_PANEL_SCALE, MAX_PANEL_SCALE), fitScale);
        return new Panel(x, y, scale,
                Math.round(PANEL_SOURCE_WIDTH * scale),
                Math.round(PANEL_SOURCE_HEIGHT * scale));
    }

    public static Point information(int screenWidth, int screenHeight, int width, int height) {
        int margin = clamp(Math.round(screenWidth * 0.02F), 6, 12);
        int hotbarLeft = screenWidth / 2 - HOTBAR_WIDTH / 2;
        int x = hotbarLeft - HOTBAR_GAP - width;
        int y = screenHeight - margin - height;

        // Very narrow GUI layouts have no room beside the hotbar. Keep the block
        // in the lower-left, but lift it above the hotbar instead of overlapping it.
        if (x < margin) {
            x = margin;
            y = screenHeight - HOTBAR_TOP_OFFSET - HOTBAR_GAP - height;
        }
        return new Point(x, Math.max(margin, y));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Panel(int x, int y, float scale, int width, int height) {
    }

    public record Point(int x, int y) {
    }
}
