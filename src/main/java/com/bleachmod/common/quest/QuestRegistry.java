package com.bleachmod.common.quest;

import com.bleachmod.BleachMod;
import com.bleachmod.Reference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class QuestRegistry {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, Saga> SAGAS = new LinkedHashMap<>();
    private static final Map<String, Quest> QUESTS = new LinkedHashMap<>();
    private static final Map<String, List<String>> SAGA_QUEST_ORDER = new LinkedHashMap<>();

    private QuestRegistry() {
    }

    public static void loadAll(MinecraftServer server) {
        Path root = server.getWorldPath(LevelResource.ROOT).resolve(Reference.MOD_ID);
        try {
            Files.createDirectories(root.resolve("sagas"));
            Files.createDirectories(root.resolve("quests"));
            Files.createDirectories(root.resolve("sidequests"));
            QuestDefaults.writeIfMissing(root);
            clear();
            loadSagas(root.resolve("sagas"));
            for (Saga saga : SAGAS.values()) {
                Path folder = root.resolve("quests").resolve(saga.getQuestFolder());
                loadSagaQuests(folder, saga);
            }
            loadSidequests(root.resolve("sidequests"));
            BleachMod.LOGGER.info("Loaded {} sagas and {} quests", SAGAS.size(), QUESTS.size());
        } catch (IOException e) {
            BleachMod.LOGGER.error("Failed to load quests", e);
        }
    }

    public static void replaceFromNetwork(String sagasJson, String questsJson) {
        clear();
        JsonArray sagaArray = JsonParser.parseString(sagasJson).getAsJsonArray();
        for (JsonElement el : sagaArray) {
            Saga saga = Saga.fromJson(el.getAsJsonObject());
            SAGAS.put(saga.getId(), saga);
        }
        JsonArray questArray = JsonParser.parseString(questsJson).getAsJsonArray();
        for (JsonElement el : questArray) {
            JsonObject obj = el.getAsJsonObject();
            String sagaId = obj.has("sagaId") ? obj.get("sagaId").getAsString() : null;
            Quest quest = QuestParser.parseQuest(obj, sagaId);
            QUESTS.put(quest.getQuestKey(), quest);
            if (quest.getType() == QuestType.SAGA && quest.getSagaId() != null) {
                SAGA_QUEST_ORDER.computeIfAbsent(quest.getSagaId(), k -> new ArrayList<>()).add(quest.getQuestKey());
            }
        }
    }

    public static String serializeSagas() {
        JsonArray array = new JsonArray();
        SAGAS.values().forEach(s -> array.add(s.toJson()));
        return GSON.toJson(array);
    }

    public static String serializeQuests() {
        JsonArray array = new JsonArray();
        QUESTS.values().forEach(q -> {
            JsonObject json = q.toJson();
            if (q.getSagaId() != null) {
                json.addProperty("sagaId", q.getSagaId());
            }
            array.add(json);
        });
        return GSON.toJson(array);
    }

    public static Quest getQuest(String key) {
        return QUESTS.get(key);
    }

    public static Saga getSaga(String id) {
        return SAGAS.get(id);
    }

    public static Collection<Quest> allQuests() {
        return QUESTS.values();
    }

    public static Collection<Saga> allSagas() {
        return SAGAS.values();
    }

    public static List<String> sagaQuestKeys(String sagaId) {
        return SAGA_QUEST_ORDER.getOrDefault(sagaId, List.of());
    }

    public static void clear() {
        SAGAS.clear();
        QUESTS.clear();
        SAGA_QUEST_ORDER.clear();
    }

    private static void loadSagas(Path folder) throws IOException {
        if (!Files.isDirectory(folder)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(folder)) {
            stream.filter(p -> p.toString().endsWith(".json")).forEach(path -> {
                try {
                    JsonObject json = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
                    Saga saga = Saga.fromJson(json);
                    SAGAS.put(saga.getId(), saga);
                } catch (Exception e) {
                    BleachMod.LOGGER.error("Failed to parse saga {}", path, e);
                }
            });
        }
    }

    private static void loadSagaQuests(Path folder, Saga saga) throws IOException {
        Files.createDirectories(folder);
        List<String> order = new ArrayList<>();
        try (Stream<Path> stream = Files.list(folder)) {
            stream.filter(p -> p.toString().endsWith(".json")).sorted().forEach(path -> {
                try {
                    JsonObject json = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
                    Quest quest = QuestParser.parseQuest(json, saga.getId());
                    QUESTS.put(quest.getQuestKey(), quest);
                    order.add(quest.getQuestKey());
                } catch (Exception e) {
                    BleachMod.LOGGER.error("Failed to parse quest {}", path, e);
                }
            });
        }
        order.sort((a, b) -> {
            Quest qa = QUESTS.get(a);
            Quest qb = QUESTS.get(b);
            return Integer.compare(qa.getNumericId(), qb.getNumericId());
        });
        SAGA_QUEST_ORDER.put(saga.getId(), order);
    }

    private static void loadSidequests(Path folder) throws IOException {
        if (!Files.isDirectory(folder)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(folder)) {
            stream.filter(p -> p.toString().endsWith(".json")).forEach(path -> {
                try {
                    JsonObject json = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
                    Quest quest = QuestParser.parseQuest(json, null);
                    QUESTS.put(quest.getQuestKey(), quest);
                } catch (Exception e) {
                    BleachMod.LOGGER.error("Failed to parse sidequest {}", path, e);
                }
            });
        }
    }
}
