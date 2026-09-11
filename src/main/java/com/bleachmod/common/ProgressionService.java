package com.bleachmod.common;
import com.bleachmod.common.data.*;
import com.bleachmod.common.evolution.TransformationsHelper;
import com.bleachmod.common.network.SyncHelper;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class ProgressionService {
    private static final UUID VITALITY_MODIFIER_ID = UUID.fromString("3a62a536-8846-4b19-b3ce-31f76d1ddd18");
    private ProgressionService() {}
    public static boolean purchaseSkill(PlayerData data, String id) {
        if (!data.getStatus().hasCreatedCharacter() || !"zanpakuto".equals(id)) return false;
        int next=data.getSkills().getLevel(id)+1;
        if (!data.getCharacter().isFormDiscovered(next==1?"shikai":"bankai")) return false;
        return data.getSkills().tryPurchase(id,data.getResources());
    }
    public static void normalize(PlayerData data) {
        data.refreshDerivedResources();
        if (!TransformationsHelper.isUnlocked(data, "zanpakuto", data.getCharacter().getActiveForm()))
            data.getCharacter().setActiveForm("zanpakuto","sealed");
        if (!TransformationsHelper.isSelectable(data,"zanpakuto",data.getCharacter().getSelectedForm()))
            data.getCharacter().setSelectedForm("zanpakuto","sealed");
        data.resetTransientState();
    }
    public static void normalize(ServerPlayer player, PlayerData data) {
        normalize(data);
        applyVitality(player, data);
    }
    public static void applyVitality(ServerPlayer player, PlayerData data) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;
        maxHealth.removeModifier(VITALITY_MODIFIER_ID);
        double amount = Math.min(1004.0D, CombatBalance.HEALTH_PER_VITALITY_RANK
                * data.getAttributes().level(AttributeData.VITALITY));
        if (amount > 0) {
            maxHealth.addTransientModifier(new AttributeModifier(VITALITY_MODIFIER_ID,
                    "bleachmod.vitality", amount, AttributeModifier.Operation.ADDITION));
        }
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }
    public static void sync(ServerPlayer player,PlayerData data) {
        normalize(player, data); SyncHelper.full(player); SyncHelper.appearance(player);
    }
}
