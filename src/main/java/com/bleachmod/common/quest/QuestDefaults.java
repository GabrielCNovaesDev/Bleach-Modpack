package com.bleachmod.common.quest;

import com.bleachmod.Reference;
import com.bleachmod.common.quest.objectives.ItemObjective;
import com.bleachmod.common.quest.objectives.KillObjective;
import com.bleachmod.common.quest.rewards.SkillReward;
import com.bleachmod.common.quest.rewards.TpsReward;
import com.bleachmod.common.quest.rewards.TransformationReward;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class QuestDefaults {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private QuestDefaults() {
    }

    public static void writeIfMissing(Path root) throws IOException {
        Path sagaFile = root.resolve("sagas").resolve("soul_society.json");
        if (!Files.exists(sagaFile)) {
            Saga saga = new Saga();
            saga.setId("soul_society");
            saga.setName("bleachmod.saga.soul_society");
            saga.setPreviousSaga("");
            saga.setQuestFolder("saga_soul_society");
            write(sagaFile, GSON.toJson(saga.toJson()));
        }

        Path questFolder = root.resolve("quests").resolve("saga_soul_society");
        Files.createDirectories(questFolder);
        writeQuest(questFolder.resolve("1.json"), soulSociety1());
        writeQuest(questFolder.resolve("2.json"), soulSociety2());
        writeQuest(questFolder.resolve("3.json"), soulSociety3());

        Path sideFolder = root.resolve("sidequests").resolve("training");
        Files.createDirectories(sideFolder);
        writeQuest(sideFolder.resolve("rukia_basic_training.json"), rukiaTraining());

        Path npcFolder = root.resolve("sidequests").resolve("npc_test");
        Files.createDirectories(npcFolder);
        writeQuest(npcFolder.resolve("sado_yasutora_test.json"), npcKillQuest("sado_yasutora", "minecraft:zombie", 3));
        writeQuest(npcFolder.resolve("ichigo_test.json"), npcKillQuest("ichigo", "bleachmod:hollow", 2));
        writeQuest(npcFolder.resolve("orihime_test.json"), npcItemQuest("orihime", "minecraft:apple", 3));
        writeQuest(npcFolder.resolve("uryuu_test.json"), npcKillQuest("uryuu", "minecraft:skeleton", 3));
        writeQuest(npcFolder.resolve("rukia_test.json"), npcBossQuest("rukia", "bleachmod:hollow_boss"));
        writeQuest(npcFolder.resolve("byakuya_test.json"), npcKillQuest("byakuya", "minecraft:spider", 4));
        writeQuest(npcFolder.resolve("urahara_test.json"), npcItemQuest("urahara", "minecraft:paper", 4));
        writeQuest(npcFolder.resolve("ulquiorra_test.json"), npcKillQuest("ulquiorra", "minecraft:enderman", 3));
    }

    private static void writeQuest(Path path, Quest quest) throws IOException {
        if (!Files.exists(path)) {
            write(path, GSON.toJson(quest.toJson()));
        }
    }

    private static void write(Path path, String json) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, json, StandardCharsets.UTF_8);
    }

    private static Quest soulSociety1() {
        Quest quest = new Quest();
        quest.setNumericId(1);
        quest.setType(QuestType.SAGA);
        quest.setSagaId("soul_society");
        quest.setTitle("bleachmod.quest.soul_society.1.name");
        quest.setDescription("bleachmod.quest.soul_society.1.desc");
        quest.setCategory("saga_soul_society");
        quest.getObjectives().add(new KillObjective("bleachmod:hollow", 6, KillObjective.SpawnMode.NATURAL, KillObjective.CountMode.ANY_MATCHING));
        quest.getRewards().add(new TpsReward(200));
        return quest;
    }

    private static Quest soulSociety2() {
        Quest quest = new Quest();
        quest.setNumericId(2);
        quest.setType(QuestType.SAGA);
        quest.setSagaId("soul_society");
        quest.setTitle("bleachmod.quest.soul_society.2.name");
        quest.setDescription("bleachmod.quest.soul_society.2.desc");
        quest.setCategory("saga_soul_society");
        quest.setParallelObjectives(true);
        quest.getPrerequisites().getConditions().add(new QuestPrerequisites.Condition(
                QuestPrerequisites.ConditionType.SAGA_QUEST, "soul_society", 1, null, null, 0, null));
        quest.getObjectives().add(new ItemObjective("minecraft:rotten_flesh", 8));
        quest.getObjectives().add(new KillObjective("minecraft:skeleton", 3, KillObjective.SpawnMode.NATURAL, KillObjective.CountMode.ANY_MATCHING));
        quest.getRewards().add(new TpsReward(400));
        quest.getRewards().add(new TransformationReward(Reference.GROUP_ZANPAKUTO, Reference.FORM_SHIKAI, 0));
        return quest;
    }

    private static Quest soulSociety3() {
        Quest quest = new Quest();
        quest.setNumericId(3);
        quest.setType(QuestType.SAGA);
        quest.setSagaId("soul_society");
        quest.setTitle("bleachmod.quest.soul_society.3.name");
        quest.setDescription("bleachmod.quest.soul_society.3.desc");
        quest.setCategory("saga_soul_society");
        quest.getPrerequisites().getConditions().add(new QuestPrerequisites.Condition(
                QuestPrerequisites.ConditionType.SAGA_QUEST, "soul_society", 2, null, null, 0, null));
        quest.getObjectives().add(new KillObjective("minecraft:zombie", 8, KillObjective.SpawnMode.NATURAL, KillObjective.CountMode.ANY_MATCHING));
        quest.getRewards().add(new TpsReward(600));
        quest.getRewards().add(new TransformationReward(Reference.GROUP_ZANPAKUTO, Reference.FORM_BANKAI, 0));
        return quest;
    }

    private static Quest rukiaTraining() {
        Quest quest = new Quest();
        quest.setStringId("rukia_basic_training");
        quest.setRepeatable(true);
        quest.setType(QuestType.SIDEQUEST);
        quest.setTitle("bleachmod.quest.rukia_basic_training.name");
        quest.setDescription("bleachmod.quest.rukia_basic_training.desc");
        quest.setCategory("training");
        quest.getObjectives().add(new KillObjective("minecraft:zombie", 10, KillObjective.SpawnMode.NATURAL, KillObjective.CountMode.ANY_MATCHING));
        quest.getRewards().add(new TpsReward(150));
        return quest;
    }

    private static Quest npcKillQuest(String npcId, String entityId, int count) {
        Quest quest = npcQuest(npcId);
        quest.getObjectives().add(new KillObjective(entityId, count,
                KillObjective.SpawnMode.NATURAL, KillObjective.CountMode.ANY_MATCHING));
        return quest;
    }

    private static Quest npcItemQuest(String npcId, String itemId, int count) {
        Quest quest = npcQuest(npcId);
        quest.getObjectives().add(new ItemObjective(itemId, count));
        return quest;
    }

    private static Quest npcQuest(String npcId) {
        Quest quest = new Quest();
        quest.setStringId(npcId + "_npc_test");
        quest.setType(QuestType.SIDEQUEST);
        quest.setTitle("bleachmod.quest." + npcId + "_npc_test.name");
        quest.setDescription("bleachmod.quest." + npcId + "_npc_test.desc");
        quest.setCategory("npc_test");
        quest.setQuestGiver(npcId);
        quest.getRewards().add(new TpsReward(100));
        return quest;
    }

    private static Quest npcBossQuest(String npcId, String entityId) {
        Quest quest = npcQuest(npcId);

        quest.setTitle("bleachmod.quest.substitute_in_action.name");
        quest.setDescription("bleachmod.quest.substitute_in_action.desc");

        quest.getObjectives().add(new KillObjective(
                entityId,
                1,
                KillObjective.SpawnMode.QUEST,
                KillObjective.CountMode.QUEST_SPAWNED_ONLY
        ));

        return quest;
    }
}
