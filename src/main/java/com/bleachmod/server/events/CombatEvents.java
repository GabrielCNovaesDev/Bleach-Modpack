package com.bleachmod.server.events;
import com.bleachmod.Reference;
import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.init.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID)
public final class CombatEvents {
    @SubscribeEvent
    public static void hurt(LivingHurtEvent event) {
        if(event.getSource().getEntity() instanceof ServerPlayer player
            && event.getSource().getDirectEntity()==player && player.getMainHandItem().is(ModItems.ASAUCHI.get())) {
            PlayerCapability.get(player).ifPresent(data->{
                if(!data.getStatus().hasCreatedCharacter())return;
                String form=data.getCharacter().getActiveForm();
                int bonus=data.getAttributes().level("power") + ("bankai".equals(form)?5:"shikai".equals(form)?2:0);
                // Scale the original hit; attack cooldown, criticals and enchantments keep their relative effect.
                event.setAmount(event.getAmount()*(1+.1F*bonus));
            });
        }
    }
}
