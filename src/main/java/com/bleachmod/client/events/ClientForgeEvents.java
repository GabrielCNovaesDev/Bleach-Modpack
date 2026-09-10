package com.bleachmod.client.events;

import com.bleachmod.Reference;
import com.bleachmod.client.gui.ConfirmRaceScreen;
import com.bleachmod.client.gui.JournalScreen;
import com.bleachmod.client.input.ModKeybinds;
import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.evolution.TransformationsHelper;
import com.bleachmod.common.network.NetworkHandler;
import com.bleachmod.common.network.c2s.ExecuteActionC2S;
import com.bleachmod.common.network.c2s.SelectFormC2S;
import com.bleachmod.common.network.c2s.UpdateStatC2S;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class ClientForgeEvents {
    private static boolean lastCharging;
    private static boolean instantHeld;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            lastCharging = false;
            instantHeld = false;
            return;
        }

        PlayerCapability.get(mc.player).ifPresent(data -> {
            if (data.isDataLoaded() && !data.getStatus().hasCreatedCharacter() && mc.screen == null) {
                mc.setScreen(new ConfirmRaceScreen());
            }
        });

        if (mc.screen != null) {
            if (lastCharging) {
                NetworkHandler.sendToServer(new UpdateStatC2S(UpdateStatC2S.StatAction.ACTION_CHARGE, false));
                lastCharging = false;
            }
            return;
        }

        while (ModKeybinds.STATUS.consumeClick()) mc.setScreen(new com.bleachmod.client.gui.StatusScreen());
        while (ModKeybinds.WHEEL.consumeClick()) mc.setScreen(new com.bleachmod.client.gui.FormWheelScreen());
        while (ModKeybinds.JOURNAL.consumeClick()) {
            mc.setScreen(new JournalScreen());
        }
        if (mc.screen != null) {
            if (lastCharging) NetworkHandler.sendToServer(new UpdateStatC2S(UpdateStatC2S.StatAction.ACTION_CHARGE, false));
            lastCharging = false;
            instantHeld = false;
            return;
        }
        while (ModKeybinds.CYCLE_FORM.consumeClick()) {
            cycleForm(mc);
        }
        while (ModKeybinds.DESCEND.consumeClick()) {
            NetworkHandler.sendToServer(new ExecuteActionC2S(ExecuteActionC2S.ActionType.FORCE_DESCEND));
        }

        boolean charging = ModKeybinds.CHARGE.isDown();
        if (charging && mc.player.isShiftKeyDown()) {
            if (lastCharging) NetworkHandler.sendToServer(new UpdateStatC2S(UpdateStatC2S.StatAction.ACTION_CHARGE, false));
            if (!instantHeld) {
                NetworkHandler.sendToServer(new ExecuteActionC2S(ExecuteActionC2S.ActionType.INSTANT_TRANSFORM));
            }
            instantHeld = true;
            lastCharging = false;
            return;
        }
        instantHeld = false;
        if (charging != lastCharging) {
            NetworkHandler.sendToServer(new UpdateStatC2S(UpdateStatC2S.StatAction.ACTION_CHARGE, charging));
            lastCharging = charging;
        }
    }

    @SubscribeEvent
    public static void logout(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        lastCharging = false;
        instantHeld = false;
        com.bleachmod.client.gui.StoryToastManager.clear();
        com.bleachmod.common.quest.QuestRegistry.clearClient();
        com.bleachmod.common.evolution.FormRegistry.clearClient();
    }

    private static void cycleForm(Minecraft mc) {
        if (mc.player == null) {
            return;
        }
        PlayerCapability.get(mc.player).ifPresent(data -> {
            String group = data.getCharacter().getSelectedFormGroup().isEmpty()
                    ? Reference.GROUP_ZANPAKUTO : data.getCharacter().getSelectedFormGroup();
            List<String> names = TransformationsHelper.getSelectableFormNames(data, group);
            if (names.isEmpty()) {
                return;
            }
            String current = data.getCharacter().getSelectedForm();
            int index = names.indexOf(current);
            String next = names.get((index + 1) % names.size());
            NetworkHandler.sendToServer(new SelectFormC2S(group, next));
        });
    }
}
