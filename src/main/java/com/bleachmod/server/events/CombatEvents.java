package com.bleachmod.server.events;
import com.bleachmod.Reference;
import com.bleachmod.common.CombatBalance;
import com.bleachmod.common.data.AttributeData;
import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.init.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID)
public final class CombatEvents {
    @SubscribeEvent
    public static void hurt(LivingHurtEvent event) {
        if(event.getSource().getEntity() instanceof ServerPlayer player && event.getSource().getDirectEntity()==player) {
            PlayerCapability.get(player).ifPresent(data->{
                if(!data.getStatus().hasCreatedCharacter())return;
                if (player.getMainHandItem().is(ModItems.ASAUCHI.get())) {
                    String form=data.getCharacter().getActiveForm();
                    float formBonus="bankai".equals(form)?.50F:"shikai".equals(form)?.20F:0;
                    event.setAmount(CombatBalance.outgoingDamage(event.getAmount(),
                        data.getAttributes().level(AttributeData.ZANJUTSU), formBonus));
                } else if (player.getMainHandItem().isEmpty()) {
                    event.setAmount(CombatBalance.outgoingDamage(event.getAmount(),
                        data.getAttributes().level(AttributeData.HAKUDA), 0));
                }
            });
        }

        // Resistance is for direct physical blows. Environmental, magic and projectile damage retain their rules.
        if(event.getEntity() instanceof ServerPlayer target
            && event.getSource().getEntity() instanceof LivingEntity attacker
            && attacker != target && event.getSource().getDirectEntity() == attacker) {
            PlayerCapability.get(target).ifPresent(data->{
                if(data.getStatus().hasCreatedCharacter())
                    event.setAmount(CombatBalance.incomingPhysicalDamage(event.getAmount(),
                        data.getAttributes().level(AttributeData.RESISTANCE)));
            });
        }
    }
}
