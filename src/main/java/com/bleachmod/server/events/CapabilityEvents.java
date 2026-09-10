package com.bleachmod.server.events;

import com.bleachmod.Reference;
import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.data.PlayerData;
import com.bleachmod.common.data.PlayerProvider;
import com.bleachmod.common.network.SyncHelper;
import com.bleachmod.common.quest.QuestRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public final class CapabilityEvents {
    private CapabilityEvents() {
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBus {
        @SubscribeEvent
        public static void registerCaps(RegisterCapabilitiesEvent event) {
            event.register(PlayerData.class);
        }
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    public static class ForgeBus {
        @SubscribeEvent
        public static void attach(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player) {
                event.addCapability(PlayerProvider.ID, new PlayerProvider());
            }
        }

        @SubscribeEvent
        public static void clone(PlayerEvent.Clone event) {
            event.getOriginal().reviveCaps();
            event.getOriginal().getCapability(PlayerCapability.INSTANCE).ifPresent(oldData ->
                    event.getEntity().getCapability(PlayerCapability.INSTANCE).ifPresent(newData -> newData.copyFrom(oldData)));
            event.getOriginal().invalidateCaps();
        }

        @SubscribeEvent
        public static void login(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                SyncHelper.questRegistry(player);
                SyncHelper.full(player);
            }
        }

        @SubscribeEvent
        public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                PlayerCapability.get(player).ifPresent(data -> data.getResources().setCurrentReiatsu(data.getResources().getMaxReiatsu()));
                SyncHelper.resources(player);
            }
        }

        @SubscribeEvent
        public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                SyncHelper.full(player);
            }
        }
    }
}
