package com.bleachmod.client.gui;

import com.bleachmod.Reference;
import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.common.network.c2s.ConfirmCharacterC2S;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfirmRaceScreen extends Screen {
    public ConfirmRaceScreen() {
        super(Component.translatable("screen.bleachmod.confirm_race"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        addRenderableWidget(Button.builder(Component.translatable("screen.bleachmod.confirm_shinigami"), button -> {
            NetworkHandler.sendToServer(new ConfirmCharacterC2S(Reference.RACE_SHINIGAMI));
            onClose();
        }).bounds(cx - 100, cy + 20, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFFE8C547);
        graphics.drawCenteredString(this.font, Component.translatable("screen.bleachmod.confirm_race.desc"), this.width / 2, this.height / 2 - 20, 0xFFFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
