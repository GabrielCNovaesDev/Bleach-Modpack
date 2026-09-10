package com.bleachmod.common.evolution;

import com.bleachmod.Reference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FormDefaults {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private FormDefaults() {
    }

    public static void writeIfMissing(Path folder) throws IOException {
        Path file = folder.resolve("shinigami.json");
        if (!Files.exists(file)) {
            Files.writeString(file, GSON.toJson(shinigamiJson()), StandardCharsets.UTF_8);
        }
    }

    public static void registerInMemory() {
        FormGroup group = shinigamiGroup();
        FormRegistry.register(Reference.RACE_SHINIGAMI, group);
    }

    public static FormGroup shinigamiGroup() {
        FormGroup group = new FormGroup();
        group.setGroupName(Reference.GROUP_ZANPAKUTO);
        group.setFormType(Reference.SKILL_ZANPAKUTO);

        FormData sealed = new FormData();
        sealed.setName(Reference.FORM_SEALED);
        sealed.setUnlockOnSkillLevel(0);
        sealed.setEnergyDrain(0.0D);
        sealed.setAllowFreeTransformOnMastery(0.0D);
        group.getForms().put(Reference.FORM_SEALED, sealed);

        FormData shikai = new FormData();
        shikai.setName(Reference.FORM_SHIKAI);
        shikai.setUnlockOnSkillLevel(1);
        shikai.setFormRequisite("zanpakuto.sealed");
        shikai.setUnlockOnMastery(0.0D);
        shikai.setEnergyDrain(0.4D);
        shikai.setInstantTransformOnMastery(40.0D);
        shikai.setAllowFreeTransformOnMastery(50.0D);
        group.getForms().put(Reference.FORM_SHIKAI, shikai);

        FormData bankai = new FormData();
        bankai.setName(Reference.FORM_BANKAI);
        bankai.setUnlockOnSkillLevel(2);
        bankai.setFormRequisite("zanpakuto.shikai");
        bankai.setUnlockOnMastery(25.0D);
        bankai.setEnergyDrain(0.8D);
        bankai.setInstantTransformOnMastery(40.0D);
        bankai.setAllowFreeTransformOnMastery(50.0D);
        group.getForms().put(Reference.FORM_BANKAI, bankai);
        return group;
    }

    private static JsonObject shinigamiJson() {
        JsonObject root = new JsonObject();
        root.addProperty("race", Reference.RACE_SHINIGAMI);
        JsonObject groups = new JsonObject();
        groups.add(Reference.GROUP_ZANPAKUTO, shinigamiGroup().toJson());
        root.add("groups", groups);
        return root;
    }
}
