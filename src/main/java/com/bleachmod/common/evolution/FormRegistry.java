package com.bleachmod.common.evolution;

import com.bleachmod.BleachMod;
import com.bleachmod.Reference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class FormRegistry {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, Map<String, FormGroup>> BY_RACE = new LinkedHashMap<>();

    private FormRegistry() {
    }

    public static void loadAll(MinecraftServer server) {
        Path root = server.getWorldPath(LevelResource.ROOT).resolve(Reference.MOD_ID).resolve("forms");
        try {
            Files.createDirectories(root);
            FormDefaults.writeIfMissing(root);
            BY_RACE.clear();
            try (var stream = Files.list(root)) {
                stream.filter(p -> p.toString().endsWith(".json")).forEach(FormRegistry::loadRaceFile);
            }
            BleachMod.LOGGER.info("Loaded forms for {} races", BY_RACE.size());
        } catch (IOException e) {
            BleachMod.LOGGER.error("Failed to load forms", e);
            FormDefaults.registerInMemory();
        }
        if (BY_RACE.isEmpty()) {
            FormDefaults.registerInMemory();
        }
    }

    public static void register(String race, FormGroup group) {
        BY_RACE.computeIfAbsent(race.toLowerCase(Locale.ROOT), k -> new LinkedHashMap<>())
                .put(group.getGroupName().toLowerCase(Locale.ROOT), group);
    }

    public static FormGroup getGroup(String race, String group) {
        Map<String, FormGroup> groups = BY_RACE.get(race == null ? "" : race.toLowerCase(Locale.ROOT));
        if (groups == null) {
            return null;
        }
        return groups.get(group == null ? "" : group.toLowerCase(Locale.ROOT));
    }

    public static FormData getForm(String race, String group, String form) {
        FormGroup formGroup = getGroup(race, group);
        if (formGroup == null) {
            return null;
        }
        return formGroup.getForms().get(form == null ? "" : form.toLowerCase(Locale.ROOT));
    }

    public static Map<String, FormGroup> groupsForRace(String race) {
        return BY_RACE.getOrDefault(race == null ? "" : race.toLowerCase(Locale.ROOT), Map.of());
    }

    private static void loadRaceFile(Path path) {
        try {
            JsonObject json = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            String race = json.get("race").getAsString();
            JsonObject groups = json.getAsJsonObject("groups");
            for (String groupName : groups.keySet()) {
                JsonObject groupJson = groups.getAsJsonObject(groupName);
                FormGroup group = new FormGroup();
                group.setGroupName(groupName);
                group.setFormType(groupJson.has("formType") ? groupJson.get("formType").getAsString() : groupName);
                JsonObject forms = groupJson.getAsJsonObject("forms");
                for (String formName : forms.keySet()) {
                    FormData data = FormData.fromJson(formName, forms.getAsJsonObject(formName));
                    group.getForms().put(formName.toLowerCase(Locale.ROOT), data);
                }
                register(race, group);
            }
        } catch (Exception e) {
            BleachMod.LOGGER.error("Failed to parse form file {}", path, e);
        }
    }

    public static String serialize() {
        JsonObject root = new JsonObject();
        BY_RACE.forEach((race, groups) -> {
            JsonObject raceJson = new JsonObject();
            groups.forEach((name, group) -> raceJson.add(name, group.toJson()));
            root.add(race, raceJson);
        });
        return GSON.toJson(root);
    }
}
