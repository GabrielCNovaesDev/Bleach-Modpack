package com.bleachmod.common;
import com.bleachmod.common.data.*;
import com.bleachmod.common.evolution.TransformationsHelper;
import com.bleachmod.common.network.SyncHelper;
import net.minecraft.server.level.ServerPlayer;

public final class ProgressionService {
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
    public static void sync(ServerPlayer player,PlayerData data) {
        normalize(data); SyncHelper.full(player); SyncHelper.appearance(player);
    }
}
