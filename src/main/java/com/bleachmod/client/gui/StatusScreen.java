package com.bleachmod.client.gui;
import com.bleachmod.common.data.*;
import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.common.network.c2s.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;

public final class StatusScreen extends Screen {
    private static final int PANEL_WIDTH=304;
    private static final int PANEL_HEIGHT=228;
    private final Map<String,Button> upgrades=new LinkedHashMap<>();
    private Button skill;
    private int left,top;
    public StatusScreen() { super(Component.translatable("screen.bleachmod.status")); }
    private PlayerData data() { return minecraft==null||minecraft.player==null?null:PlayerCapability.get(minecraft.player).orElse(null); }
    @Override protected void init() {
        left=(width-PANEL_WIDTH)/2; top=Math.max(6,(height-PANEL_HEIGHT)/2); upgrades.clear();
        int index=0;
        for(String id:AttributeData.IDS) {
            int column=index/4, row=index%4;
            int x=left+78+column*148, y=top+90+row*26;
            Button button=addRenderableWidget(Button.builder(Component.empty(),b->NetworkHandler.sendToServer(new PurchaseAttributeC2S(id)))
                .bounds(x,y,68,20).build());
            upgrades.put(id,button); index++;
        }
        skill=addRenderableWidget(Button.builder(Component.empty(),b->NetworkHandler.sendToServer(new UpdateSkillC2S("zanpakuto",UpdateSkillC2S.SkillAction.PURCHASE)))
            .bounds(left+8,top+202,PANEL_WIDTH-16,20).build());
        refresh();
    }
    @Override public void tick() { refresh(); }
    private void refresh() {
        PlayerData d=data(); if(d==null)return;
        upgrades.forEach((id,b)->{
            int cost=d.getAttributes().cost(id);
            b.setMessage(cost<0?Component.translatable("screen.bleachmod.max"):Component.translatable("screen.bleachmod.buy",cost));
            b.active=cost>=0&&d.getResources().getTrainingPoints()>=cost;
            b.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("attribute.bleachmod."+id+".desc")
                .append("\n").append(Component.translatable(cost<0?"screen.bleachmod.max":b.active?"screen.bleachmod.purchase_remaining":"screen.bleachmod.insufficient_points",
                    Math.max(0,(int)d.getResources().getTrainingPoints()-cost)))));
        });
        int level=d.getSkills().getLevel("zanpakuto");
        int cost=d.getSkills().skillCostForNextLevel("zanpakuto");
        boolean discovered=d.getCharacter().isFormDiscovered(level==0?"shikai":"bankai");
        skill.setMessage(level>=2?Component.translatable("screen.bleachmod.skill_max"):
            !discovered?Component.translatable("screen.bleachmod.quest_required"):
            Component.translatable("screen.bleachmod.skill_buy",level+1,cost));
        skill.active=level<2&&discovered&&d.getResources().getTrainingPoints()>=cost;
        skill.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable(level>=2?"screen.bleachmod.skill_max":!discovered?"screen.bleachmod.quest_required":skill.active?"screen.bleachmod.purchase_remaining":"screen.bleachmod.insufficient_points",
            Math.max(0,(int)d.getResources().getTrainingPoints()-cost))));
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt) {
        renderBackground(g);
        g.fill(left,top,left+PANEL_WIDTH,top+PANEL_HEIGHT,0xEE171321);
        g.drawCenteredString(font,title,width/2,top+8,0xE8C547);
        PlayerData d=data();
        if(d!=null) {
            g.drawString(font,Component.translatable("hud.bleachmod.tp",(int)d.getResources().getTrainingPoints()),left+8,top+26,0xA0E8A0);
            g.drawString(font,Component.translatable("screen.bleachmod.bp",Math.round(d.getBattlePower())),left+190,top+26,0xF4D35E);
            g.drawString(font,Component.translatable("screen.bleachmod.forms",Component.translatable("form.bleachmod."+d.getCharacter().getActiveForm()),
                Component.translatable("form.bleachmod."+d.getCharacter().getSelectedForm())),left+8,top+40,0xFFFFFF);
            int y=top+56;
            for(String form:List.of("shikai","bankai")) {
                g.drawString(font,Component.translatable("screen.bleachmod.mastery",Component.translatable("form.bleachmod."+form),
                    (int)d.getCharacter().getMastery("zanpakuto",form)),left+8,y,0xCAB7FF); y+=13;
            }
            int index=0;
            for(String id:AttributeData.IDS) {
                int column=index/4, row=index%4;
                int x=left+8+column*148, attrY=top+96+row*26;
                String label=Component.translatable("attribute.bleachmod."+id).getString()+" "+d.getAttributes().level(id);
                g.drawString(font,font.plainSubstrByWidth(label,66),x,attrY,0xFFFFFF);
                index++;
            }
        }
        super.render(g,mx,my,dt);
    }
    @Override public boolean isPauseScreen() { return false; }
}
