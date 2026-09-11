package com.bleachmod.client.gui;
import com.bleachmod.common.data.*;
import com.bleachmod.common.evolution.TransformationsHelper;
import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.common.network.c2s.SelectFormC2S;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;

public final class FormWheelScreen extends Screen {
    private final Map<String,Button> buttons=new LinkedHashMap<>();
    public FormWheelScreen() { super(Component.translatable("screen.bleachmod.wheel")); }
    @Override protected void init() {
        buttons.clear();
        var d=PlayerCapability.get(minecraft.player).orElse(null);
        if(d==null)return;
        List<String> names=TransformationsHelper.getUnlockedForms(d,"zanpakuto").stream().map(f->f.getName()).toList();
        int radius=Math.min(72,Math.max(36,height/2-60));
        for(int i=0;i<names.size();i++) {
            String form=names.get(i); double angle=-Math.PI/2+i*2*Math.PI/names.size();
            int x=width/2+(int)(Math.cos(angle)*radius)-48, y=height/2+(int)(Math.sin(angle)*radius)-10;
            buttons.put(form,addRenderableWidget(Button.builder(Component.translatable("form.bleachmod."+form),b->{
                NetworkHandler.sendToServer(new SelectFormC2S("zanpakuto",form)); onClose();
            }).bounds(x,y,96,20).build()));
        }
        tick();
    }
    @Override public void tick() {
        if(minecraft.player==null){onClose();return;}
        PlayerCapability.get(minecraft.player).ifPresent(d->buttons.forEach((form,b)->{
            b.active=TransformationsHelper.isSelectable(d,"zanpakuto",form);
            b.setTooltip(b.active?null:net.minecraft.client.gui.components.Tooltip.create(Component.translatable("screen.bleachmod.wheel_chain")));
        }));
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt) {
        renderBackground(g);
        g.drawCenteredString(font,title,width/2,16,0xE8C547);
        g.drawCenteredString(font,Component.translatable("screen.bleachmod.wheel_hint"),width/2,height-36,0xFFFFFF);
        g.drawCenteredString(font,Component.translatable("screen.bleachmod.no_active_skills"),width/2,height-22,0xCAB7FF);
        PlayerCapability.get(minecraft.player).ifPresent(d->{
            g.drawCenteredString(font,Component.translatable("form.bleachmod."+d.getCharacter().getSelectedForm()),width/2,height/2-4,0xE8C547);
        });
        super.render(g,mx,my,dt);
    }
    @Override public boolean isPauseScreen() { return false; }
}
