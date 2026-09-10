package com.bleachmod.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class ModKeybinds {
    public static final String CATEGORY = "key.categories.bleachmod";

    public static final KeyMapping JOURNAL = new KeyMapping(
            "key.bleachmod.journal",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            CATEGORY
    );

    public static final KeyMapping CHARGE = new KeyMapping(
            "key.bleachmod.charge",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );

    public static final KeyMapping CYCLE_FORM = new KeyMapping(
            "key.bleachmod.cycle_form",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );

    public static final KeyMapping DESCEND = new KeyMapping(
            "key.bleachmod.descend",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );

    public static final KeyMapping STATUS = new KeyMapping("key.bleachmod.status", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY);
    public static final KeyMapping WHEEL = new KeyMapping("key.bleachmod.wheel", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY);

    private ModKeybinds() {
    }
}
