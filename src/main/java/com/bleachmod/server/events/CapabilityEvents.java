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
                PlayerProvider provider = new PlayerProvider();
                event.addCapability(PlayerProvider.ID, provider);
                event.addListener(provider::invalidate);
            }
        }

        @SubscribeEvent
        public static void clone(PlayerEvent.Clone event) {
            // Serialization reads the provider data even after LazyOptional invalidation.
            var saved = event.getOriginal().serializeNBT().getCompound("ForgeCaps").getCompound(PlayerProvider.ID.toString());
            event.getEntity().getCapability(PlayerCapability.INSTANCE).ifPresent(data -> {
                if (!saved.isEmpty()) data.load(saved);
                data.resetTransientState();
                data.getCharacter().setActiveForm("zanpakuto", "sealed");
            });
        }

        @SubscribeEvent
        public static void login(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                PlayerCapability.get(player).ifPresent(data -> com.bleachmod.common.ProgressionService.normalize(player, data));
                PlayerCapability.get(player).ifPresent(data -> data.getPlayerQuestData().bindDefinitions());
                SyncHelper.questRegistry(player);
                SyncHelper.full(player);
            }
        }

        @SubscribeEvent
        public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                PlayerCapability.get(player).ifPresent(data -> {
                    com.bleachmod.common.ProgressionService.normalize(player, data);
                    data.getResources().setCurrentReiatsu(data.getResources().getMaxReiatsu());
                });
                SyncHelper.full(player);
                SyncHelper.appearance(player);
            }
        }

        @SubscribeEvent
        public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                PlayerCapability.get(player).ifPresent(data -> data.resetTransientState());
                SyncHelper.full(player);
            }
        }
        @SubscribeEvent
        public static void startTracking(PlayerEvent.StartTracking event) {
            if (event.getEntity() instanceof ServerPlayer observer && event.getTarget() instanceof ServerPlayer subject)
                SyncHelper.appearanceTo(subject, observer);
        }

        @SubscribeEvent
        public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
            PlayerCapability.get(event.getEntity()).ifPresent(data -> data.resetTransientState());
        }
    }
}
