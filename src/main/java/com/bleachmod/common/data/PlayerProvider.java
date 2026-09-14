package com.bleachmod.common.data;

import com.bleachmod.Reference;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final ResourceLocation ID = Reference.id("player_data");

    private final PlayerData data = new PlayerData();
    private LazyOptional<PlayerData> optional = LazyOptional.of(() -> data);
    private boolean invalidated;

    public PlayerData getData() {
        return data;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap != PlayerCapability.INSTANCE) return LazyOptional.empty();
        // Entity's CapabilityProvider gates access while invalid. After reviveCaps (dimension travel),
        // the dispatcher is reused; renew its invalidated handle while preserving the PlayerData instance.
        if (invalidated) {
            optional = LazyOptional.of(() -> data);
            invalidated = false;
        }
        return optional.cast();
    }

    public void invalidate() {
        invalidated = true;
        optional.invalidate();
    }

    @Override
    public CompoundTag serializeNBT() {
        return data.save();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        data.load(nbt);
    }
}
