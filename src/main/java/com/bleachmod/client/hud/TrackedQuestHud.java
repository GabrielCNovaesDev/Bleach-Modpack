package com.bleachmod.client.hud;
import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.quest.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
public final class TrackedQuestHud {
    private TrackedQuestHud(){}
    public static final IGuiOverlay OVERLAY=(gui,g,dt,w,h)->{
        Minecraft mc=Minecraft.getInstance();if(mc.player==null||mc.options.hideGui)return;
        PlayerCapability.get(mc.player).ifPresent(d->{
            String key=d.getPlayerQuestData().getTrackedQuestId();
            if(key==null||!d.getPlayerQuestData().isAccepted(key))return;
            Quest q=QuestRegistry.getQuest(key);if(q==null)return;
            QuestProgress p=d.getPlayerQuestData().getProgress(key);
            int width=Math.min(200,w/2-8),x=w-width-6,y=6;
            java.util.List<net.minecraft.util.FormattedCharSequence> lines=new java.util.ArrayList<>();
            lines.addAll(mc.font.split(Component.translatable(q.getTitle()),width-12));
            for(int i=0;i<q.getObjectives().size();i++){
                var o=q.getObjectives().get(i);
                lines.addAll(mc.font.split(o.describe().copy().append(" ["+p.getObjectiveProgress(i)+"/"+p.getRequired(i,o.getRequired())+"]"),width-12));
            }
            int visible=Math.min(lines.size(),8);
            g.fill(x,y,x+width,y+visible*11+12,0xCC171321);
            for(int i=0;i<visible;i++)g.drawString(mc.font,lines.get(i),x+6,y+6+i*11,i==0?0xE8C547:0xFFFFFF,false);
        });
    };
}
