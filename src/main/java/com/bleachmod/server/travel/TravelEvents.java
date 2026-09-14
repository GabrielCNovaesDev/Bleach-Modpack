package com.bleachmod.server.travel;

import com.bleachmod.BleachMod;
import com.bleachmod.Reference;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import java.nio.file.Files;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID)
public final class TravelEvents {
    @SubscribeEvent
    public static void beforeLevels(ServerAboutToStartEvent event) {
        var server=event.getServer();
        var source=FMLPaths.GAMEDIR.get().resolve("bleachmod-maps/soul_society");
        var target=server.getWorldPath(LevelResource.ROOT).resolve("dimensions/bleachmod/soul_society");
        try {
            Files.createDirectories(source);
            var map=SoulSocietyMapInstaller.install(source,target);
            DimensionTravelService.get(server).setMap(map.orElse(null));
            BleachMod.LOGGER.info("Soul Society map: {}. Import folder: {}",map.isPresent()?"installed":"waiting for save",source.toAbsolutePath());
        } catch(Exception e) {
            // Do not load a partially imported or incompatible dimension after installation fails.
            throw new IllegalStateException("Soul Society map import failed. Source: "+source+". Existing saves were not overwritten.",e);
        }
    }
    @SubscribeEvent
    public static void starting(ServerStartingEvent event) {
        try { DimensionTravelService.get(event.getServer()).reload(); }
        catch(Exception e) { throw new IllegalStateException("Invalid travel configuration",e); }
    }
    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if(event.phase==TickEvent.Phase.END) DimensionTravelService.get(event.getServer()).tick();
    }
    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) { DimensionTravelService.stop(event.getServer()); }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if(event.getEntity() instanceof ServerPlayer player) DimensionTravelService.get(player.server).logout(player);
    }
    @SubscribeEvent
    public static void died(LivingDeathEvent event) {
        if(event.getEntity() instanceof ServerPlayer player) DimensionTravelService.get(player.server).cancel(player);
    }
    @SubscribeEvent
    public static void spawn(net.minecraftforge.event.entity.player.PlayerSetSpawnEvent event) {
        if(event.getSpawnLevel().equals(SoulSociety.LEVEL)) event.setCanceled(true);
    }
    @SubscribeEvent
    public static void changed(PlayerEvent.PlayerChangedDimensionEvent event) {
        if(event.getEntity() instanceof ServerPlayer player) {
            var service=DimensionTravelService.get(player.server);
            if(!service.isCommitting(player)) service.cancel(player);
        }
    }
    @SubscribeEvent
    public static void portal(EntityTravelToDimensionEvent event) {
        if(event.getEntity().level().dimension().equals(SoulSociety.LEVEL) || event.getDimension().equals(SoulSociety.LEVEL)) {
            if(!(event.getEntity() instanceof ServerPlayer player) || !DimensionTravelService.get(player.server).isCommitting(player)) event.setCanceled(true);
        }
    }
}
