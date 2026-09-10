package com.bleachmod.common.data;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

public final class PlayerCapability {
    public static final Capability<PlayerData> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });

    private PlayerCapability() {
    }

    public static LazyOptional<PlayerData> get(Player player) {
        return player.getCapability(INSTANCE);
    }
}
