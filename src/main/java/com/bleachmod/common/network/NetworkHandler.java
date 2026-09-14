package com.bleachmod.common.network;

import com.bleachmod.Reference;
import com.bleachmod.common.network.c2s.ClaimQuestRewardC2S;
import com.bleachmod.common.network.c2s.ConfirmCharacterC2S;
import com.bleachmod.common.network.c2s.ExecuteActionC2S;
import com.bleachmod.common.network.c2s.QuestActionC2S;
import com.bleachmod.common.network.c2s.SelectFormC2S;
import com.bleachmod.common.network.c2s.SetTrackedQuestC2S;
import com.bleachmod.common.network.c2s.UpdateSkillC2S;
import com.bleachmod.common.network.c2s.UpdateStatC2S;
import com.bleachmod.common.network.s2c.ActionFeedbackS2C;
import com.bleachmod.common.network.s2c.AppearanceSyncS2C;
import com.bleachmod.common.network.s2c.PlayerSyncS2C;
import com.bleachmod.common.network.s2c.ProgressionSyncS2C;
import com.bleachmod.common.network.s2c.ResourceSyncS2C;
import com.bleachmod.common.network.s2c.StoryToastS2C;
import com.bleachmod.common.network.s2c.SyncQuestRegistryS2C;
import com.bleachmod.common.network.s2c.DamageIndicatorS2C;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkDirection;
import java.util.Optional;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Reference.id("network"),
            () -> Reference.NETWORK_PROTOCOL,
            Reference.NETWORK_PROTOCOL::equals,
            Reference.NETWORK_PROTOCOL::equals
    );

    private static int id;

    private NetworkHandler() {
    }

    public static void register() {
        CHANNEL.registerMessage(id++, com.bleachmod.common.network.c2s.PurchaseAttributeC2S.class,
            com.bleachmod.common.network.c2s.PurchaseAttributeC2S::encode, com.bleachmod.common.network.c2s.PurchaseAttributeC2S::decode,
            com.bleachmod.common.network.c2s.PurchaseAttributeC2S::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, QuestActionC2S.class, QuestActionC2S::encode, QuestActionC2S::decode, QuestActionC2S::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, ClaimQuestRewardC2S.class, ClaimQuestRewardC2S::encode, ClaimQuestRewardC2S::decode, ClaimQuestRewardC2S::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, SetTrackedQuestC2S.class, SetTrackedQuestC2S::encode, SetTrackedQuestC2S::decode, SetTrackedQuestC2S::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, SelectFormC2S.class, SelectFormC2S::encode, SelectFormC2S::decode, SelectFormC2S::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, UpdateStatC2S.class, UpdateStatC2S::encode, UpdateStatC2S::decode, UpdateStatC2S::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, ExecuteActionC2S.class, ExecuteActionC2S::encode, ExecuteActionC2S::decode, ExecuteActionC2S::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, UpdateSkillC2S.class, UpdateSkillC2S::encode, UpdateSkillC2S::decode, UpdateSkillC2S::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, ConfirmCharacterC2S.class, ConfirmCharacterC2S::encode, ConfirmCharacterC2S::decode, ConfirmCharacterC2S::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, PlayerSyncS2C.class, PlayerSyncS2C::encode, PlayerSyncS2C::decode, PlayerSyncS2C::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, ResourceSyncS2C.class, ResourceSyncS2C::encode, ResourceSyncS2C::decode, ResourceSyncS2C::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, ProgressionSyncS2C.class, ProgressionSyncS2C::encode, ProgressionSyncS2C::decode, ProgressionSyncS2C::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, AppearanceSyncS2C.class, AppearanceSyncS2C::encode, AppearanceSyncS2C::decode, AppearanceSyncS2C::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, SyncQuestRegistryS2C.class, SyncQuestRegistryS2C::encode, SyncQuestRegistryS2C::decode, SyncQuestRegistryS2C::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, StoryToastS2C.class, StoryToastS2C::encode, StoryToastS2C::decode, StoryToastS2C::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, ActionFeedbackS2C.class, ActionFeedbackS2C::encode, ActionFeedbackS2C::decode, ActionFeedbackS2C::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, DamageIndicatorS2C.class, DamageIndicatorS2C::encode, DamageIndicatorS2C::decode, DamageIndicatorS2C::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, TravelPackets.Query.class, TravelPackets.Query::encode, TravelPackets.Query::decode, TravelPackets.Query::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, TravelPackets.Request.class, TravelPackets.Request::encode, TravelPackets.Request::decode, TravelPackets.Request::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, TravelPackets.Catalog.class, TravelPackets.Catalog::encode, TravelPackets.Catalog::decode, TravelPackets.Catalog::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, TravelPackets.Result.class, TravelPackets.Result::encode, TravelPackets.Result::decode, TravelPackets.Result::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
