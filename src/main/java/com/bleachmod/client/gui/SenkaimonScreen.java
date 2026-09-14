package com.bleachmod.client.gui;

import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.common.network.TravelPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SenkaimonScreen extends Screen {
    private static int sequence;
    private TravelPackets.Catalog catalog;
    private String message="loading";
    private Button travel;
    private int activeRequest, ticks;
    private boolean pending;
    public SenkaimonScreen() { super(Component.translatable("bleachmod.travel.title")); }
    @Override protected void init() {
        travel=addRenderableWidget(Button.builder(Component.translatable("bleachmod.travel.go"),button->submit())
            .bounds(width/2-100,height/2+15,200,20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.back"),button->onClose()).bounds(width/2-100,height/2+42,200,20).build());
        travel.active=false;
        NetworkHandler.sendToServer(new TravelPackets.Query());
    }
    private void submit() {
        if(catalog==null || pending) return;
        pending=true; ticks=0; message="preparing"; travel.active=false; activeRequest=++sequence;
        NetworkHandler.sendToServer(new TravelPackets.Request(activeRequest,catalog.target(),catalog.revision()));
    }
    public static void catalog(TravelPackets.Catalog packet) {
        if(Minecraft.getInstance().screen instanceof SenkaimonScreen screen) {
            screen.catalog=packet;
            if(!screen.pending) { screen.message=packet.reason(); screen.travel.active=packet.reason().isEmpty(); }
        }
    }
    public static void result(TravelPackets.Result packet) {
        if(Minecraft.getInstance().screen instanceof SenkaimonScreen screen && screen.pending && screen.activeRequest==packet.requestId()) {
            screen.message=packet.reason();
            if(!packet.reason().equals("preparing")) {
                screen.pending=false;
                if(packet.reason().equals("success")) screen.onClose();
                else { screen.catalog=null; screen.ticks=0; }
            }
        }
    }
    @Override public void tick() {
        ticks++;
        if(pending && ticks>240) { pending=false; message="timeout"; }
        if(!pending && ticks%40==0) NetworkHandler.sendToServer(new TravelPackets.Query());
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partialTick) {
        renderBackground(graphics);
        graphics.fill(width/2-150,height/2-90,width/2+150,height/2+76,0xED171C28);
        graphics.fill(width/2-150,height/2-90,width/2+150,height/2-88,0xFFD4B878);
        graphics.drawCenteredString(font,title,width/2,height/2-70,0xE7CC91);
        if(catalog!=null) {
            graphics.drawCenteredString(font,Component.translatable(catalog.target().equals("bleachmod:return")?"bleachmod.travel.return":"bleachmod.travel.entry"),width/2,height/2-47,0xFFFFFF);
            graphics.drawCenteredString(font,Component.translatable("bleachmod.travel.cost",catalog.cost(),(catalog.cooldown()+19)/20),width/2,height/2-31,0xBBC6DA);
        }
        if(!message.isEmpty()) graphics.drawCenteredString(font,Component.translatable("bleachmod.travel."+message),width/2,height/2-10,0xE7CC91);
        super.render(graphics,mouseX,mouseY,partialTick);
    }
}
