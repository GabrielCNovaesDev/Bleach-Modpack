package com.bleachmod.client.hud;

import com.bleachmod.Reference;
import com.bleachmod.common.data.PlayerCapability;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Cancels the vanilla hearts overlay once the player has created their character.
 * Our ReiatsuHud renders a custom VIDA bar in the same slot, so the vanilla hearts
 * would otherwise overlap with it.
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class VanillaHealthHider {
    private VanillaHealthHider() {
    }

    @SubscribeEvent
    public static void onGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != VanillaGuiOverlay.PLAYER_HEALTH.type()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        boolean shouldHide = PlayerCapability.get(mc.player)
                .filter(d -> d.isDataLoaded() && d.getStatus().hasCreatedCharacter())
                .isPresent();
        if (shouldHide) {
            event.setCanceled(true);
        }
    }
}
