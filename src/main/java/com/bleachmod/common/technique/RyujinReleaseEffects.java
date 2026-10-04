package com.bleachmod.common.technique;

import com.bleachmod.Reference;
import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.events.BleachEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Release visuals and bounded Bankai world-fire emission. */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class RyujinReleaseEffects {
    private RyujinReleaseEffects() { }
    public static boolean shouldRelease(String oldForm, String newForm) {
        return !oldForm.equals(newForm) && (Reference.FORM_BANKAI.equals(newForm)
                || (Reference.FORM_SHIKAI.equals(newForm) && Reference.FORM_SEALED.equals(oldForm)));
    }
    @SubscribeEvent public static void release(BleachEvents.FormChangeEvent event) {
        ServerPlayer player = event.getPlayer();
        if (!Reference.GROUP_ZANPAKUTO.equals(event.getNewGroup()) || !TechniqueService.isRyujinJakkaEquipped(player)
                || !shouldRelease(event.getOldForm(), event.getNewForm())) return;
        boolean bankai = Reference.FORM_BANKAI.equals(event.getNewForm());
        if (bankai) SpiritFlameService.emit(player, 8);
        int samples = bankai ? 32 : 20;
        double radius = bankai ? 3 : 1.5;
        for (int i = 0; i < samples; i++) {
            double angle = i * Math.PI * 2 / samples;
            player.serverLevel().sendParticles(ParticleTypes.FLAME, player.getX() + Math.cos(angle) * radius,
                    player.getY() + 0.2, player.getZ() + Math.sin(angle) * radius,
                    0, Math.cos(angle), 0.15, Math.sin(angle), bankai ? 0.18 : 0.09);
        }
        player.serverLevel().sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1,
                player.getZ(), bankai ? 24 : 10, 0.4, 0.6, 0.4, 0.025);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT,
                player.getSoundSource(), 0.8F, bankai ? 0.6F : 1.1F);
    }
    @SubscribeEvent public static void aura(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || !player.isAlive() || player.isSpectator() || player.tickCount % 8 != 0
                || !TechniqueService.isRyujinJakkaEquipped(player)) return;
        PlayerCapability.get(player).ifPresent(data -> {
            if (!data.getStatus().hasCreatedCharacter() || !Reference.RACE_SHINIGAMI.equals(data.getCharacter().getRace())
                    || !Reference.GROUP_ZANPAKUTO.equals(data.getCharacter().getActiveFormGroup())) return;
            String form = data.getCharacter().getActiveForm();
            if (!Reference.FORM_SHIKAI.equals(form) && !Reference.FORM_BANKAI.equals(form)) return;
            if (Reference.FORM_BANKAI.equals(form) && player.tickCount % 40 == 0) SpiritFlameService.emit(player, 3);
            // Follow the blade side, based on horizontal aim; deliberately sparse during combat.
            Vec3 direction = Vec3.directionFromRotation(0, player.getYRot());
            Vec3 point = player.position().add(direction.scale(0.7)).add(-direction.z * 0.4, 1, direction.x * 0.4);
            player.serverLevel().sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z,
                    Reference.FORM_BANKAI.equals(form) ? 1 : 3, 0.1, 0.25, 0.1, 0.01);
        });
    }
}
