package com.bleachmod.client.gui;

import com.bleachmod.Reference;
import com.bleachmod.client.BleachTextures;
import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.common.network.c2s.ConfirmCharacterC2S;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfirmRaceScreen extends Screen {
    private boolean pending;
    public ConfirmRaceScreen() {
        super(Component.translatable("screen.bleachmod.confirm_race"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        addRenderableWidget(Button.builder(Component.translatable("screen.bleachmod.confirm_shinigami"), button -> {
            NetworkHandler.sendToServer(new ConfirmCharacterC2S(Reference.RACE_SHINIGAMI));
            pending = true;
            button.active = false;
        }).bounds(cx - 100, this.height - 48, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        double scale = Math.max((double) width / BleachTextures.CONFIRM_BG_W, (double) height / BleachTextures.CONFIRM_BG_H);
        int bgW = (int) Math.ceil(BleachTextures.CONFIRM_BG_W * scale);
        int bgH = (int) Math.ceil(BleachTextures.CONFIRM_BG_H * scale);
        int bgX = (this.width - bgW) / 2;
        BleachTextures.blit(graphics, BleachTextures.CONFIRM_BG, bgX, (height - bgH) / 2, bgW, bgH,
                BleachTextures.CONFIRM_BG_W, BleachTextures.CONFIRM_BG_H);
        graphics.fill(0, 0, this.width, this.height, 0x44000000);

        int portrait = BleachTextures.PORTRAIT_DEST;
        int px = this.width / 2 - portrait / 2;
        int py = this.height / 2 - 96;
        BleachTextures.blit(graphics, BleachTextures.RACE_SHINIGAMI, px, py, portrait, portrait,
                BleachTextures.PORTRAIT_SRC, BleachTextures.PORTRAIT_SRC);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, py + portrait + 8, 0xFFE8C547);
        graphics.drawCenteredString(this.font, Component.translatable("screen.bleachmod.confirm_race.desc"),
                this.width / 2, py + portrait + 22, 0xFFF2E9C8);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void tick() {
        if (minecraft.player != null && pending)
            com.bleachmod.common.data.PlayerCapability.get(minecraft.player).ifPresent(data -> {
                if (data.getStatus().hasCreatedCharacter()) onClose();
            });
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
