package com.bleachmod.server.events;

import com.bleachmod.Reference;
import com.bleachmod.common.data.PlayerData;
import com.bleachmod.common.events.BleachEvents;
import com.bleachmod.common.evolution.FormData;
import com.bleachmod.common.evolution.TransformationsHelper;
import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.common.network.SyncHelper;
import com.bleachmod.common.network.s2c.ActionFeedbackS2C;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

public final class FormModeHandler {
    private FormModeHandler() {
    }

    public static int chargeRate(PlayerData data) {
        FormData target = TransformationsHelper.getSelectedFormData(data);
        if (target == null) {
            return 0;
        }
        int mastery = (int) data.getCharacter().getMastery(data.getCharacter().getSelectedFormGroup(), target.getName());
        return 2 + Math.min(3, (int) (mastery * 0.04D));
    }

    public static void attemptTransform(ServerPlayer player, PlayerData data) {
        transform(player, data, false);
        data.resetTransientState();
        SyncHelper.full(player);
    }

    public static void instantTransform(ServerPlayer player, PlayerData data) {
        FormData target = TransformationsHelper.getSelectedFormData(data);
        if (target == null) {
            return;
        }
        double mastery = data.getCharacter().getMastery(data.getCharacter().getSelectedFormGroup(), target.getName());
        if (mastery < target.getInstantTransformOnMastery()) {
            NetworkHandler.sendToPlayer(ActionFeedbackS2C.of(Component.translatable("message.bleachmod.form.no_instant")), player);
            return;
        }
        transform(player, data, true);
        data.resetTransientState();
        SyncHelper.full(player);
    }

    public static void descend(ServerPlayer player, PlayerData data) {
        String oldGroup = data.getCharacter().getActiveFormGroup();
        String oldForm = data.getCharacter().getActiveForm();
        String next = TransformationsHelper.previousForm(data);
        data.getCharacter().setActiveForm(oldGroup.isEmpty() ? Reference.GROUP_ZANPAKUTO : oldGroup, next);
        data.getResources().setActionCharge(0);
        data.getStatus().setActionCharging(false);
        MinecraftForge.EVENT_BUS.post(new BleachEvents.FormChangeEvent(player, oldGroup, oldForm, data.getCharacter().getActiveFormGroup(), next));
        player.displayClientMessage(Component.translatable("message.bleachmod.form.descend", Component.translatable("form.bleachmod." + next)), true);
        SyncHelper.appearance(player);
        SyncHelper.full(player);
    }

    public static void revertToSealed(ServerPlayer player, PlayerData data, Component reason) {
        String oldGroup = data.getCharacter().getActiveFormGroup();
        String oldForm = data.getCharacter().getActiveForm();
        if (Reference.FORM_SEALED.equals(oldForm)) {
            return;
        }
        data.getCharacter().setActiveForm(Reference.GROUP_ZANPAKUTO, Reference.FORM_SEALED);
        data.getResources().setActionCharge(0);
        data.getStatus().setActionCharging(false);
        MinecraftForge.EVENT_BUS.post(new BleachEvents.FormChangeEvent(player, oldGroup, oldForm, Reference.GROUP_ZANPAKUTO, Reference.FORM_SEALED));
        if (reason != null) {
            player.displayClientMessage(reason, true);
        }
        SyncHelper.appearance(player);
        SyncHelper.resources(player);
    }

    private static void transform(ServerPlayer player, PlayerData data, boolean instant) {
        FormData target = TransformationsHelper.getSelectedFormData(data);
        if (target == null) {
            return;
        }
        String group = data.getCharacter().getSelectedFormGroup();
        if (group.isEmpty()) {
            group = Reference.GROUP_ZANPAKUTO;
        }
        if (!data.getStatus().hasCreatedCharacter() || !player.isAlive() || !TransformationsHelper.isSelectable(data, group, target.getName())) {
            NetworkHandler.sendToPlayer(ActionFeedbackS2C.of(Component.translatable("message.bleachmod.form.locked")), player);
            return;
        }
        if (TransformationsHelper.needsFreeTransformMastery(data)) {
            player.displayClientMessage(Component.translatable("message.bleachmod.form.free_transform_mastery",
                    (int) Math.round(target.getAllowFreeTransformOnMastery())), true);
            return;
        }
        if (target.getName().equalsIgnoreCase(data.getCharacter().getActiveForm())
                && group.equalsIgnoreCase(data.getCharacter().getActiveFormGroup())) {
            return;
        }
        float cost = (float) (data.getResources().getMaxReiatsu() * 0.10D * target.getEnergyDrain());
        if (instant) {
            cost = (float) (target.getEnergyDrain() * 4.0D);
        }
        if (data.getResources().getCurrentReiatsu() < cost) {
            player.displayClientMessage(Component.translatable("message.bleachmod.form.no_reiatsu", (int) cost), true);
            return;
        }
        data.getResources().consumeReiatsu(cost);
        String oldGroup = data.getCharacter().getActiveFormGroup();
        String oldForm = data.getCharacter().getActiveForm();
        data.getCharacter().setActiveForm(group, target.getName());
        data.getResources().setActionCharge(0);
        data.getStatus().setActionCharging(false);
        MinecraftForge.EVENT_BUS.post(new BleachEvents.FormChangeEvent(player, oldGroup, oldForm, group, target.getName()));
        player.displayClientMessage(Component.translatable("message.bleachmod.transformation",
                Component.translatable("form.bleachmod." + target.getName())), true);
        SyncHelper.appearance(player);
        SyncHelper.resources(player);
    }
}
