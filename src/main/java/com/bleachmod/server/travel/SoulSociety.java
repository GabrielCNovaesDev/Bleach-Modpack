package com.bleachmod.server.travel;

import com.bleachmod.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class SoulSociety {
    public static final String ID = "bleachmod:soul_society";
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, Reference.id("soul_society"));
    private SoulSociety() {}
}
