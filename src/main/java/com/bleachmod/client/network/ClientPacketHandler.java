package com.bleachmod.client.network;

import com.bleachmod.client.gui.StoryToastManager;
import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.network.s2c.ActionFeedbackS2C;
import com.bleachmod.common.network.s2c.AppearanceSyncS2C;
import com.bleachmod.common.network.s2c.PlayerSyncS2C;
import com.bleachmod.common.network.s2c.ProgressionSyncS2C;
import com.bleachmod.common.network.s2c.ResourceSyncS2C;
import com.bleachmod.common.network.s2c.StoryToastS2C;
import com.bleachmod.common.network.s2c.SyncQuestRegistryS2C;
import com.bleachmod.common.network.s2c.OpenNpcQuestScreenS2C;
import com.bleachmod.client.gui.NpcQuestScreen;
import com.bleachmod.common.quest.QuestRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class ClientPacketHandler {
    private ClientPacketHandler() {
    }

    public static void handlePlayerSync(PlayerSyncS2C msg) {
        apply(msg.playerId(), msg.nbt());
    }

    public static void handleResourceSync(ResourceSyncS2C msg) {
        apply(msg.playerId(), msg.nbt());
    }

    public static void handleProgressionSync(ProgressionSyncS2C msg) {
        apply(msg.playerId(), msg.nbt());
    }

    public static void handleAppearanceSync(AppearanceSyncS2C msg) {
        apply(msg.playerId(), msg.nbt());
    }

    public static void handleQuestRegistry(SyncQuestRegistryS2C msg) {
        com.bleachmod.common.evolution.FormRegistry.replaceFromNetwork(msg.formsJson());
        QuestRegistry.replaceFromNetwork(msg.sagasJson(), msg.questsJson());
    }

    public static void handleToast(StoryToastS2C msg) {
        StoryToastManager.show(msg);
    }

    public static void handleFeedback(ActionFeedbackS2C msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.Serializer.fromJson(msg.json()), false);
        }
    }

    public static void handleOpenNpcQuestScreen(OpenNpcQuestScreenS2C msg) {
        Minecraft.getInstance().setScreen(new NpcQuestScreen(msg.npcId(), msg.entityId()));
    }

    private static void apply(int playerId, CompoundTag nbt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || nbt == null) {
            return;
        }
        Entity entity = mc.level.getEntity(playerId);
        if (entity instanceof Player player) {
            PlayerCapability.get(player).ifPresent(data -> data.load(nbt));
        }
    }
}
