package com.bleachmod;

import net.minecraft.resources.ResourceLocation;

public final class Reference {
    public static final String MOD_ID = "bleachmod";
    public static final String NETWORK_PROTOCOL = "2.2";

    public static final String RACE_SHINIGAMI = "shinigami";
    public static final String GROUP_ZANPAKUTO = "zanpakuto";
    public static final String FORM_SEALED = "sealed";
    public static final String FORM_SHIKAI = "shikai";
    public static final String FORM_BANKAI = "bankai";
    public static final String SKILL_ZANPAKUTO = "zanpakuto";

    public static final String TAG_QUEST_KEY = "blc_quest_key";
    public static final String TAG_QUEST_OBJECTIVE_INDEX = "blc_quest_objective_index";
    public static final String TAG_QUEST_OWNER = "blc_quest_owner";

    public static final int ZANPAKUTO_MAX_LEVEL = 2;
    public static final float BASE_REIATSU = 100.0F;
    public static final float REIATSU_REGEN_PER_TICK = 0.25F;
    public static final float REVERT_REIATSU_RATIO = 0.05F;

    private Reference() {
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}

