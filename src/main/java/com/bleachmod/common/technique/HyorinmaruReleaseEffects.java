package com.bleachmod.common.technique;

import com.bleachmod.Reference;
import com.bleachmod.common.data.*;
import com.bleachmod.common.events.BleachEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID)
public final class HyorinmaruReleaseEffects {
    @SubscribeEvent public static void release(BleachEvents.FormChangeEvent event) {
        ServerPlayer p=event.getPlayer();
        if (!Reference.GROUP_ZANPAKUTO.equals(event.getNewGroup()) || !RyujinReleaseEffects.shouldRelease(event.getOldForm(),event.getNewForm())) return;
        PlayerCapability.get(p).ifPresent(d->{
            if (!CharacterData.HYORINMARU.equals(d.getCharacter().getZanpakutoIdentity())) return;
            boolean bankai=Reference.FORM_BANKAI.equals(event.getNewForm());
            int samples=bankai?40:24;
            for(int i=0;i<samples;i++) {
                double a=i*Math.PI*2/samples,r=bankai?3:1.5;
                p.serverLevel().sendParticles(ParticleTypes.SNOWFLAKE,p.getX()+Math.cos(a)*r,p.getY()+0.8,p.getZ()+Math.sin(a)*r,4,0.2,0.8,0.2,0.08);
            }
            p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.GLASS_BREAK,p.getSoundSource(),1,bankai?0.6F:1);
            if(bankai) p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.ENDER_DRAGON_GROWL,p.getSoundSource(),0.3F,1.3F);
        });
    }
    @SubscribeEvent public static void aura(TickEvent.PlayerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END || !(event.player instanceof ServerPlayer p) || p.tickCount%8!=0 || !p.isAlive() || p.isSpectator()) return;
        PlayerCapability.get(p).ifPresent(d->{
            if(!d.getStatus().hasCreatedCharacter() || !CharacterData.HYORINMARU.equals(d.getCharacter().getZanpakutoIdentity())) return;
            String form=d.getCharacter().getActiveForm();
            boolean bankai=Reference.FORM_BANKAI.equals(form);
            if(!bankai && !Reference.FORM_SHIKAI.equals(form) && !IceArmorService.active(p)) return;
            p.serverLevel().sendParticles(ParticleTypes.SNOWFLAKE,p.getX(),p.getY()+1.2,p.getZ(),bankai?10:4,bankai?0.9:0.4,0.6,0.4,0.015);
        });
    }
}
