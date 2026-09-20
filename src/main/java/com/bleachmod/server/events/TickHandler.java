package com.bleachmod.server.events;

import com.bleachmod.Reference;
import com.bleachmod.common.CombatBalance;
import com.bleachmod.common.data.AttributeData;
import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.evolution.FormData;
import com.bleachmod.common.evolution.TransformationsHelper;
import com.bleachmod.common.network.SyncHelper;
import com.bleachmod.common.technique.TechniqueService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class TickHandler {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        PlayerCapability.get(player).ifPresent(data -> {
            if (!data.getStatus().hasCreatedCharacter()) {
                return;
            }
            if (!player.isAlive() || player.isSpectator()) {
                data.resetTransientState();
                return;
            }

            data.getStatus().tickTransientState();

            float previousEnergy = data.getResources().getCurrentReiatsu();
            int previousCharge = data.getResources().getActionCharge();
            TechniqueService.tickIgnition(player, data);
            TechniqueService.tickFlameFanGround(player, data);
            TechniqueService.tickFlameBarrageGround(player, data);
            TechniqueService.tickFlameDash(player, data);
            FormData active = TransformationsHelper.getActiveFormData(data);
            float drain = active == null ? 0.0F : CombatBalance.formDrain((float) active.getEnergyDrain(),
                    data.getAttributes().level(AttributeData.CONTROL));
            if (drain > 0.0F) {
                data.getResources().addReiatsu(-drain);
                if (data.getResources().getCurrentReiatsu() <= data.getResources().getMaxReiatsu() * Reference.REVERT_REIATSU_RATIO) {
                    FormModeHandler.revertToSealed(player, data, Component.translatable("message.bleachmod.form.drained_reiatsu"));
                }
            } else {
                data.getResources().addReiatsu(Reference.REIATSU_REGEN_PER_TICK);
            }

            if (data.getStatus().isActionCharging()) {
                data.getResources().addActionCharge(FormModeHandler.chargeRate(data));
                if (data.getResources().getActionCharge() >= 100) {
                    FormModeHandler.attemptTransform(player, data);
                }
            }

            if (player.tickCount % 4 == 0 || (previousEnergy != data.getResources().getCurrentReiatsu()
                    && (data.getResources().getCurrentReiatsu() == data.getResources().getMaxReiatsu()
                    || data.getResources().getCurrentReiatsu() == 0))) {
                if (previousEnergy != data.getResources().getCurrentReiatsu() || previousCharge != data.getResources().getActionCharge())
                    SyncHelper.resources(player);
            }
            active = TransformationsHelper.getActiveFormData(data);
            if (player.tickCount % 100 == 0 && active != null && !Reference.FORM_SEALED.equals(active.getName())) {
                data.getCharacter().addMastery(
                        data.getCharacter().getActiveFormGroup(),
                        active.getName(),
                        active.getPassiveMasteryEveryFiveSeconds(),
                        active.getMaxMastery()
                );
                SyncHelper.full(player);
            }
        });
    }

    @SubscribeEvent
    public static void onLoginResetCharge(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PlayerCapability.get(player).ifPresent(data -> data.resetTransientState());
    }
}
