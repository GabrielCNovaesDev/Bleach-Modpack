package com.bleachmod.client.model;

import com.bleachmod.Reference;
import net.minecraft.client.model.geom.ModelLayerLocation;

public final class ModModelLayers {

    public static final ModelLayerLocation HOLLOW_BOSS =
            new ModelLayerLocation(
                    Reference.id("hollow_boss"),
                    "main"
            );

    private ModModelLayers() {
    }
}