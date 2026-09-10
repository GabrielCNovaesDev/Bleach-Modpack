package com.bleachmod.client.hud;
import com.bleachmod.common.data.PlayerCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public final class ReiatsuHud {
    private ReiatsuHud() {}
    private static float shownCharge;
    private static long previousRender;
    public static final IGuiOverlay OVERLAY=(gui,g,dt,w,h)->{
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.options.hideGui)return;
        PlayerCapability.get(mc.player).ifPresent(d->{
            if(!d.isDataLoaded()||!d.getStatus().hasCreatedCharacter())return;
            int x=6,y=h-116,bw=138;
            g.fill(x,y,x+bw+12,y+78,0xCA171321);
            float ratio=d.getResources().getCurrentReiatsu()/Math.max(1,d.getResources().getMaxReiatsu());
            g.fill(x+6,y+6,x+bw+6,y+20,0xFF32283F);
            g.fill(x+6,y+6,x+6+(int)(bw*Math.max(0,Math.min(1,ratio))),y+20,0xFF7958BF);
            g.drawString(mc.font,Component.translatable("hud.bleachmod.reiatsu",(int)d.getResources().getCurrentReiatsu(),(int)d.getResources().getMaxReiatsu()),x+9,y+9,0xFFFFFF,false);
            g.drawString(mc.font,Component.translatable("form.bleachmod."+d.getCharacter().getActiveForm()),x+6,y+26,0xE8C547,false);
            g.drawString(mc.font,Component.translatable("hud.bleachmod.target",Component.translatable("form.bleachmod."+d.getCharacter().getSelectedForm())),x+6,y+39,0xCAB7FF,false);
            g.drawString(mc.font,Component.translatable("hud.bleachmod.tp",(int)d.getResources().getTrainingPoints()),x+6,y+52,0xA0E8A0,false);
            if(d.getStatus().isActionCharging()){
                long now = System.nanoTime();
                float seconds = previousRender == 0 ? 0 : Math.min(.1F, (now - previousRender) / 1_000_000_000F);
                previousRender = now;
                float target = d.getResources().getActionCharge();
                shownCharge += (target - shownCharge) * Math.min(1, seconds * 18);
                g.fill(x+6,y+66,x+bw+6,y+72,0xFF32283F);
                g.fill(x+6,y+66,x+6+(int)(bw*shownCharge/100),y+72,0xFFE8C547);
            } else { shownCharge = 0; previousRender = 0; }
        });
    };
}
