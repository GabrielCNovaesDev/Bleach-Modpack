package com.bleachmod.common.network;

import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.data.PlayerData;
import com.bleachmod.common.network.s2c.AppearanceSyncS2C;
import com.bleachmod.common.network.s2c.PlayerSyncS2C;
import com.bleachmod.common.network.s2c.ProgressionSyncS2C;
import com.bleachmod.common.network.s2c.ResourceSyncS2C;
import com.bleachmod.common.network.s2c.SyncQuestRegistryS2C;
import com.bleachmod.common.quest.QuestRegistry;
import com.bleachmod.common.evolution.FormRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public final class SyncHelper {
    private SyncHelper() {
    }

    public static void full(ServerPlayer player) {
        PlayerCapability.get(player).ifPresent(data ->
                NetworkHandler.sendToPlayer(new PlayerSyncS2C(player.getId(), data.save()), player));
    }

    public static void resources(ServerPlayer player) {
        PlayerCapability.get(player).ifPresent(data -> {
            CompoundTag tag = new CompoundTag();
            tag.put("resources", data.getResources().save());
            tag.put("status", data.getStatus().save());
            NetworkHandler.sendToPlayer(new ResourceSyncS2C(player.getId(), tag), player);
        });
    }

    public static void progression(ServerPlayer player) {
        PlayerCapability.get(player).ifPresent(data -> {
            CompoundTag tag = new CompoundTag();
            tag.put("skills", data.getSkills().save());
            tag.put("playerQuestData", data.getPlayerQuestData().save());
            tag.put("status", data.getStatus().save());
            NetworkHandler.sendToPlayer(new ProgressionSyncS2C(player.getId(), tag), player);
        });
    }

    public static void appearance(ServerPlayer player) {
        PlayerCapability.get(player).ifPresent(data -> {
            CompoundTag tag = new CompoundTag();
            tag.put("character", data.getCharacter().saveAppearance());
            NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new AppearanceSyncS2C(player.getId(), tag));
        });
    }

    public static void questRegistry(ServerPlayer player) {
        NetworkHandler.sendToPlayer(new SyncQuestRegistryS2C(QuestRegistry.serializeSagas(), QuestRegistry.serializeQuests(), FormRegistry.serialize()), player);
    }

    public static void appearanceTo(ServerPlayer subject, ServerPlayer observer) {
        PlayerCapability.get(subject).ifPresent(data -> {
            CompoundTag tag = new CompoundTag();
            tag.put("character", data.getCharacter().saveAppearance());
            NetworkHandler.sendToPlayer(new AppearanceSyncS2C(subject.getId(), tag), observer);
        });
    }

    public static PlayerData require(ServerPlayer player) {
        return PlayerCapability.get(player).orElseThrow(() -> new IllegalStateException("Missing PlayerData for " + player.getGameProfile().getName()));
    }
}
