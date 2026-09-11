package com.bleachmod.server.events;
import com.bleachmod.Reference;
import com.bleachmod.common.CombatBalance;
import com.bleachmod.common.data.AttributeData;
import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.common.network.s2c.DamageIndicatorS2C;
import com.bleachmod.init.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID)
public final class CombatEvents {
    @SubscribeEvent
    public static void hurt(LivingHurtEvent event) {
        if(isPlayerMeleeAttack(event.getSource()) && event.getSource().getEntity() instanceof ServerPlayer player) {
            PlayerCapability.get(player).ifPresent(data->{
                if(!data.getStatus().hasCreatedCharacter())return;
                float formBonus=CombatBalance.formDamageBonus(data.getCharacter().getActiveForm());
                if (player.getMainHandItem().is(ModItems.ASAUCHI.get())) {
                    event.setAmount(CombatBalance.outgoingDamage(event.getAmount(),
                        data.getAttributes().level(AttributeData.ZANJUTSU), formBonus));
                } else if (player.getMainHandItem().isEmpty()) {
                    event.setAmount(CombatBalance.outgoingDamage(event.getAmount(),
                        data.getAttributes().level(AttributeData.HAKUDA), formBonus));
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

    @SubscribeEvent
    public static void damageApplied(LivingDamageEvent event) {
        if (!isPlayerMeleeAttack(event.getSource()) || !(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!isSupportedMeleeWeapon(player)) return;
        PlayerCapability.get(player).ifPresent(data -> {
            if (data.getStatus().hasCreatedCharacter() && event.getAmount() > 0)
                NetworkHandler.sendToPlayer(new DamageIndicatorS2C(event.getAmount()), player);
        });
    }

    private static boolean isPlayerMeleeAttack(DamageSource source) {
        return source.is(DamageTypes.PLAYER_ATTACK) && source.getDirectEntity() == source.getEntity();
    }

    private static boolean isSupportedMeleeWeapon(ServerPlayer player) {
        return player.getMainHandItem().isEmpty() || player.getMainHandItem().is(ModItems.ASAUCHI.get());
    }
}
