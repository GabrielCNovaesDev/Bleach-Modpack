package com.bleachmod.client.gui;
import com.bleachmod.common.data.*;
import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.common.network.c2s.*;
import com.bleachmod.common.quest.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import java.util.*;

public final class JournalScreen extends Screen {
    private List<Quest> quests=List.of();
    private final List<Button> slots=new ArrayList<>();
    private String selected;
    private int page,scroll,left,top,panelW,panelH,split;
    private Button start,claim,track,previous,next;
    public JournalScreen(){super(Component.translatable("screen.bleachmod.journal"));}
    private PlayerData data(){return minecraft==null||minecraft.player==null?null:PlayerCapability.get(minecraft.player).orElse(null);}
    private Quest quest(){return selected==null?null:QuestRegistry.getQuest(selected);}
    @Override protected void init(){
        quests=List.copyOf(QuestRegistry.allQuests());
        if(quest()==null&&!quests.isEmpty())selected=quests.get(0).getQuestKey();
        panelW=Math.min(480,width-16); panelH=Math.min(280,height-44);
        left=(width-panelW)/2;top=8;split=left+panelW*2/5;
        slots.clear();
        for(int i=0;i<5;i++){
            final int slot=i;
            slots.add(addRenderableWidget(Button.builder(Component.empty(),b->{
                int index=page*5+slot;
                if(index<quests.size()){selected=quests.get(index).getQuestKey();scroll=0;refresh();}
            }).bounds(left+8,top+30+i*24,split-left-16,20).build()));
        }
        previous=addRenderableWidget(Button.builder(Component.literal("<"),b->{page--;refresh();}).bounds(left+8,top+154,30,20).build());
        next=addRenderableWidget(Button.builder(Component.literal(">"),b->{page++;refresh();}).bounds(split-38,top+154,30,20).build());
        int y=top+panelH+4,bw=(panelW-12)/4;
        start=addRenderableWidget(Button.builder(Component.translatable("screen.bleachmod.journal.start"),b->{
            if(quest()!=null)NetworkHandler.sendToServer(new QuestActionC2S(QuestActionC2S.Action.START,selected));
        }).bounds(left,y,bw,20).build());
        claim=addRenderableWidget(Button.builder(Component.translatable("screen.bleachmod.journal.claim"),b->claim())
            .bounds(left+bw+4,y,bw,20).build());
        track=addRenderableWidget(Button.builder(Component.translatable("screen.bleachmod.journal.track"),b->{
            PlayerData d=data(); if(d!=null&&quest()!=null)NetworkHandler.sendToServer(new SetTrackedQuestC2S(selected.equals(d.getPlayerQuestData().getTrackedQuestId())?"":selected));
        }).bounds(left+2*(bw+4),y,bw,20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.bleachmod.status"),b->minecraft.setScreen(new StatusScreen()))
            .bounds(left+3*(bw+4),y,bw,20).build());
        refresh();
    }
    private boolean pending(PlayerData d,Quest q){
        QuestProgress p=d.getPlayerQuestData().getProgress(q.getQuestKey());
        if(p==null)return false;
        for(int i=0;i<q.getRewards().size();i++)if(!p.isRewardClaimed(i))return true;
        return false;
    }
    private void claim(){
        PlayerData d=data();Quest q=quest();if(d==null||q==null)return;
        QuestProgress p=d.getPlayerQuestData().getProgress(selected);
        for(int i=0;i<q.getRewards().size();i++)if(p!=null&&!p.isRewardClaimed(i))NetworkHandler.sendToServer(new ClaimQuestRewardC2S(selected,i));
    }
    @Override public void tick(){
        List<Quest> current=List.copyOf(QuestRegistry.allQuests());
        if(!current.equals(quests)){quests=current;page=0;if(quest()==null)selected=quests.isEmpty()?null:quests.get(0).getQuestKey();}
        refresh();
    }
    private void refresh(){
        page=Math.max(0,Math.min(page,Math.max(0,(quests.size()-1)/5)));
        PlayerData d=data();Quest q=quest();
        for(int i=0;i<slots.size();i++){
            int index=page*5+i;Button b=slots.get(i);b.visible=index<quests.size();
            if(b.visible){
                Quest item=quests.get(index);QuestStatus status=d==null?QuestStatus.NOT_STARTED:d.getPlayerQuestData().getStatus(item.getQuestKey());
                String marker=switch(status){case ACCEPTED->"> ";case SUCCESS->"+ ";case FAILED->"! ";default->"  ";};
                String name=Component.translatable(item.getTitle()).getString();
                b.setMessage(Component.literal(marker+font.plainSubstrByWidth(name,b.getWidth()-20)));
            }
        }
        previous.active=page>0;next.active=(page+1)*5<quests.size();
        boolean valid=q!=null&&d!=null;
        QuestProgress p=valid?d.getPlayerQuestData().getProgress(selected):null;
        boolean compatible=p==null||p.matchesDefinition(q);
        start.active=valid&&compatible&&QuestAvailabilityChecker.isAvailable(d,selected);
        claim.active=valid&&compatible&&d.getPlayerQuestData().isCompleted(selected)&&pending(d,q);
        track.active=valid&&d.getPlayerQuestData().isAccepted(selected);
        track.setMessage(Component.translatable(valid&&selected.equals(d.getPlayerQuestData().getTrackedQuestId())?"screen.bleachmod.untrack":"screen.bleachmod.journal.track"));
    }
    private List<Component> details(){
        List<Component> text=new ArrayList<>(); Quest q=quest();PlayerData d=data();if(q==null)return text;
        QuestProgress p=d==null?null:d.getPlayerQuestData().getProgress(selected);
        text.add(Component.translatable(q.getTitle()));text.add(Component.translatable(q.getDescription()));text.add(Component.empty());
        if(p!=null&&!p.matchesDefinition(q))text.add(Component.translatable("message.bleachmod.quest.changed"));
        else if(d!=null&&!QuestAvailabilityChecker.isAvailable(d,selected)&&d.getPlayerQuestData().getStatus(selected)==QuestStatus.NOT_STARTED)
            text.add(Component.translatable("screen.bleachmod.quest_locked"));
        text.add(Component.translatable("screen.bleachmod.journal.objectives"));
        for(int i=0;i<q.getObjectives().size();i++){
            var o=q.getObjectives().get(i);
            text.add(o.describe().copy().append(" ["+(p==null?0:p.getObjectiveProgress(i))+"/"+(p==null?o.getRequired():p.getRequired(i,o.getRequired()))+"]"));
        }
        text.add(Component.empty());text.add(Component.translatable("screen.bleachmod.journal.rewards"));
        for(int i=0;i<q.getRewards().size();i++)text.add(q.getRewards().get(i).describe().copy().append(p!=null&&p.isRewardClaimed(i)?" ✓":""));
        return text;
    }
    @Override public boolean mouseScrolled(double x,double y,double delta){scroll=Math.max(0,scroll-(int)(delta*20));return true;}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){
        renderBackground(g);g.fill(left,top,left+panelW,top+panelH,0xF0171321);
        g.fill(split,top+26,left+panelW-6,top+panelH-6,0xFFDFD2AB);
        g.drawCenteredString(font,title,width/2,top+10,0xE8C547);
        List<FormattedCharSequence> lines=new ArrayList<>();
        for(Component text:details())lines.addAll(font.split(text,left+panelW-split-20));
        int available=panelH-38;scroll=Math.min(scroll,Math.max(0,lines.size()*12-available));
        g.enableScissor(split+4,top+30,left+panelW-8,top+panelH-8);
        int y=top+32-scroll;for(var line:lines){g.drawString(font,line,split+8,y,0x302318,false);y+=12;}
        g.disableScissor();super.render(g,mx,my,dt);
    }
    @Override public boolean isPauseScreen(){return false;}
}
