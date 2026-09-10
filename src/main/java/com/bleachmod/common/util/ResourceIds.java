package com.bleachmod.common.util;

import net.minecraft.resources.ResourceLocation;

public final class ResourceIds {
    private ResourceIds() {
    }

    public static ResourceLocation parse(String id) {
        if (id == null || id.isBlank() || !ResourceLocation.isValidResourceLocation(id)) {
            return null;
        }
        return new ResourceLocation(id);
    }
}
