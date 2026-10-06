package com.bleachmod;

import com.bleachmod.common.CombatBalance;
import com.bleachmod.common.data.*;
import com.bleachmod.common.quest.*;
import com.bleachmod.common.quest.objectives.*;
import com.bleachmod.common.quest.rewards.*;
import com.bleachmod.common.evolution.*;
import com.bleachmod.common.ProgressionService;
import com.bleachmod.client.hud.HudLayout;
import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import java.util.*;

/** Dependency-free regression runner, executed by Gradle check even offline. */
public final class MvpRegressionTest {
    private static int count;
    public static void main(String[] args) {
        test("Zanpakuto identity archives mastery and unlocks across switches and reload", () -> {
            CharacterData d = new CharacterData(); d.initializeShinigami();
            d.unlockForm("bankai"); d.setMastery("zanpakuto", "shikai", 75);
            d.bindZanpakuto("hyorinmaru");
            yes(!d.isFormDiscovered("bankai")); eq(0D, d.getMastery("zanpakuto", "shikai"));
            d.unlockForm("shikai"); d.setMastery("zanpakuto", "shikai", 15);
            CharacterData loaded = new CharacterData(); loaded.load(d.save());
            eq("hyorinmaru", loaded.getZanpakutoIdentity()); eq(15D, loaded.getMastery("zanpakuto", "shikai"));
            loaded.bindZanpakuto("ryujin_jakka"); yes(loaded.isFormDiscovered("bankai")); eq(75D, loaded.getMastery("zanpakuto", "shikai"));
            loaded.bindZanpakuto("hyorinmaru"); yes(loaded.isFormDiscovered("shikai")); yes(!loaded.isFormDiscovered("bankai"));
            CompoundTag legacy = d.save(); legacy.remove("zanpakutoIdentity"); loaded.load(legacy);
            eq("ryujin_jakka", loaded.getZanpakutoIdentity()); eq(75D, loaded.getMastery("zanpakuto", "shikai"));
        });
        test("Original story reward cannot unlock Hyorinmaru", () -> {
            PlayerData d = new PlayerData(); d.initializeShinigami(); d.getCharacter().bindZanpakuto("hyorinmaru");
            new TransformationReward("zanpakuto", "bankai", 40).give(null, d);
            yes(!d.getCharacter().isFormDiscovered("bankai")); eq(0D, d.getCharacter().getMastery("zanpakuto", "bankai"));
            d.getCharacter().bindZanpakuto("ryujin_jakka"); yes(d.getCharacter().isFormDiscovered("bankai"));
            eq(40D, d.getCharacter().getMastery("zanpakuto", "bankai"));
        });
        test("Hyorinmaru models load with a single edge and distinct guard", () -> {
            try {
                for (String name : List.of("hyorinmaru", "hyorinmaru_shikai", "hyorinmaru_bankai")) {
                    String source = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/resources/assets/bleachmod/models/item", name + ".json"));
                    var parsed = net.minecraft.client.renderer.block.model.BlockModel.fromString(source);
                    yes(parsed.getElements().size() >= 23);
                    var model = JsonParser.parseString(source).getAsJsonObject(); int edges = 0;
                    for (var element : model.getAsJsonArray("elements")) {
                        if (element.getAsJsonObject().get("name").getAsString().equals("single_cutting_edge")) edges++;
                    }
                    eq(4, edges); yes(source.contains("four_point_tsuba"));
                }
            } catch (java.io.IOException e) { throw new IllegalStateException(e); }
        });
        test("Four-block flame front collapses over six ticks in both ranges", () -> {
            for (int range : List.of(8, 14)) {
                eq(4D, com.bleachmod.common.technique.FlameWaveService.frontHeight(1, range));
                eq(4D, com.bleachmod.common.technique.FlameWaveService.frontHeight(range, range));
                eq(2D, com.bleachmod.common.technique.FlameWaveService.frontHeight(range + 3, range));
                eq(0D, com.bleachmod.common.technique.FlameWaveService.frontHeight(range + 6, range));
            }
        });
        test("Ryujin models parse as explicit geometry with a shared corrected grip", () -> {
            try {
                var directory = java.nio.file.Path.of("src/main/resources/assets/bleachmod/models/item");
                var parent = JsonParser.parseString(java.nio.file.Files.readString(
                        directory.resolve("ryujin_katana_handheld.json"))).getAsJsonObject();
                yes(!parent.has("parent")); // builtin/generated would replace our geometry with the short sprite.
                var display = parent.getAsJsonObject("display");
                eq(55, display.getAsJsonObject("thirdperson_righthand").getAsJsonArray("rotation").get(2).getAsInt());
                eq(-55, display.getAsJsonObject("thirdperson_lefthand").getAsJsonArray("rotation").get(2).getAsInt());
                for (String name : List.of("ryujin_jakka", "ryujin_jakka_shikai", "ryujin_jakka_bankai")) {
                    String source = java.nio.file.Files.readString(directory.resolve(name + ".json"));
                    var parsed = net.minecraft.client.renderer.block.model.BlockModel.fromString(source);
                    eq(22, parsed.getElements().size());
                    var model = JsonParser.parseString(source).getAsJsonObject();
                    int edges = 0;
                    int spines = 0;
                    eq("bleachmod:item/ryujin_katana_handheld", model.get("parent").getAsString());
                    for (var element : model.getAsJsonArray("elements")) {
                        var part = element.getAsJsonObject();
                        if (part.has("name") && part.get("name").getAsString().equals("single_cutting_edge")) edges++;
                        if (part.has("name") && part.get("name").getAsString().equals("blunt_spine")) spines++;
                        eq(6, part.getAsJsonObject("faces").size());
                        for (int axis = 0; axis < 3; axis++) {
                            double from = part.getAsJsonArray("from").get(axis).getAsDouble();
                            double to = part.getAsJsonArray("to").get(axis).getAsDouble();
                            yes(from >= -16 && to <= 32 && from < to);
                        }
                    }
                    eq(4, edges);
                    eq(4, spines);
                    if (!name.equals("ryujin_jakka")) {
                        var uv = model.getAsJsonArray("elements").get(8).getAsJsonObject()
                                .getAsJsonObject("faces").getAsJsonObject("north").getAsJsonArray("uv");
                        yes(uv.get(2).getAsDouble() - uv.get(0).getAsDouble() > 10);
                    }
                }
            } catch (java.io.IOException exception) { throw new IllegalStateException(exception); }
        });
        test("HUD cooldown snapshot is isolated from persistence and authoritative state", () -> {
            StatusData server = new StatusData();
            server.setTechniqueSlot3CooldownTicks(400);
            server.setTechniqueSlot4CooldownTicks(1200);
            StatusData client = new StatusData();
            client.load(server.save());
            client.readTechniqueHud(server.techniqueHud());
            eq(400, client.getHudCooldowns()[2]);
            eq(1200, client.getHudCooldowns()[3]);
            eq(0, client.getTechniqueSlot4CooldownTicks());
            yes(!server.save().contains("cooldowns"));
            int[] detached = client.getHudCooldowns();
            detached[3] = 0;
            eq(1200, client.getHudCooldowns()[3]);
            server.setCooldownsDisabled(true);
            client.readTechniqueHud(server.techniqueHud());
            yes(client.areHudCooldownsDisabled());
        });
        test("release effects trigger only when entering or ascending released forms", () -> {
            yes(com.bleachmod.common.technique.RyujinReleaseEffects.shouldRelease("sealed", "shikai"));
            yes(com.bleachmod.common.technique.RyujinReleaseEffects.shouldRelease("shikai", "bankai"));
            yes(!com.bleachmod.common.technique.RyujinReleaseEffects.shouldRelease("bankai", "shikai"));
            yes(!com.bleachmod.common.technique.RyujinReleaseEffects.shouldRelease("bankai", "bankai"));
            yes(!com.bleachmod.common.technique.RyujinReleaseEffects.shouldRelease("shikai", "sealed"));
        });
        test("free reiatsu accepts zero balance without debit and restore enforces costs", () -> {
            ResourcesData resources = new ResourcesData();
            resources.setCurrentReiatsu(0);
            resources.setCostsDisabled(true);
            yes(resources.canAffordReiatsu(45));
            yes(resources.consumeReiatsu(45));
            eq(0F, resources.getCurrentReiatsu());
            yes(!resources.consumeReiatsu(Float.NaN));
            yes(!resources.consumeReiatsu(-1));
            resources.setCurrentReiatsu(50);
            resources.addReiatsu(-5);
            eq(50F, resources.getCurrentReiatsu());
            resources.setCostsDisabled(false);
            yes(resources.consumeReiatsu(45));
            eq(5F, resources.getCurrentReiatsu());
            yes(!resources.consumeReiatsu(6));
        });
        test("cooldown test mode covers every slot and survives form cleanup", () -> {
            StatusData status = new StatusData();
            status.setTechniqueSlot4CooldownTicks(1200);
            status.setCooldownsDisabled(true);
            status.setTechniqueSlot1CooldownTicks(100);
            status.setTechniqueSlot2CooldownTicks(80);
            status.setTechniqueSlot3CooldownTicks(400);
            status.setTechniqueSlot4CooldownTicks(1200);
            status.clearTransformationState();
            yes(status.areCooldownsDisabled());
            eq(0, status.getTechniqueSlot1CooldownTicks());
            eq(0, status.getTechniqueSlot2CooldownTicks());
            eq(0, status.getTechniqueSlot3CooldownTicks());
            eq(0, status.getTechniqueSlot4CooldownTicks());
            status.setCooldownsDisabled(false);
            status.setTechniqueSlot4CooldownTicks(1200);
            eq(1200, status.getTechniqueSlot4CooldownTicks());
        });
        test("operator overrides are never restored from NBT", () -> {
            PlayerData original = new PlayerData();
            original.getStatus().setCooldownsDisabled(true);
            original.getResources().setCostsDisabled(true);
            PlayerData loaded = new PlayerData();
            loaded.getStatus().setCooldownsDisabled(true);
            loaded.getResources().setCostsDisabled(true);
            loaded.load(original.save());
            yes(!loaded.getStatus().areCooldownsDisabled());
            yes(!loaded.getResources().areCostsDisabled());
        });
        test("oriented wall rejects behind width height and diagonal query corners", () -> {
            var origin = net.minecraft.world.phys.Vec3.ZERO;
            var forward = new net.minecraft.world.phys.Vec3(0, 0, 1);
            yes(com.bleachmod.common.technique.TechniqueGeometry.intersectsWall(origin, forward, 16, 2, 15,
                    new net.minecraft.world.phys.AABB(-0.3, 0, 15.9, 0.3, 2, 16.5)));
            for (var box : List.of(new net.minecraft.world.phys.AABB(2, 0, 2, 3, 2, 3),
                    new net.minecraft.world.phys.AABB(0, 16, 2, 0.5, 17, 3),
                    new net.minecraft.world.phys.AABB(0, 0, -2, 0.5, 2, -1))) {
                yes(!com.bleachmod.common.technique.TechniqueGeometry.intersectsWall(origin, forward, 16, 2, 15, box));
            }
            var diagonal = new net.minecraft.world.phys.Vec3(1, 0, 1).normalize();
            yes(!com.bleachmod.common.technique.TechniqueGeometry.intersectsWall(origin, diagonal, 16, 2, 15,
                    new net.minecraft.world.phys.AABB(8, 0, 0, 8.5, 2, 0.5)));
        });
        test("tornado phase advances clockwise in Minecraft horizontal coordinates", () -> {
            double before = com.bleachmod.common.technique.TechniqueGeometry.tornadoAngle(0, 0, 0);
            double after = com.bleachmod.common.technique.TechniqueGeometry.tornadoAngle(1, 0, 0);
            yes(after > before);
            yes(Math.sin(after) > Math.sin(before));
        });
        test("technique cooldowns survive transformation and count down", () -> {
            StatusData status = new StatusData();
            status.setTechniqueSlot1CooldownTicks(100);
            status.setTechniqueSlot2CooldownTicks(80);
            status.setTechniqueSlot3CooldownTicks(300);
            status.setTechniqueSlot4CooldownTicks(1200);
            status.setIgnitionActive(true);
            status.setFlameDashTicks(16);
            status.clearTransformationState();
            yes(!status.isIgnitionActive());
            eq(0, status.getFlameDashTicks());
            status.tickTransientState();
            eq(99, status.getTechniqueSlot1CooldownTicks());
            eq(79, status.getTechniqueSlot2CooldownTicks());
            eq(299, status.getTechniqueSlot3CooldownTicks());
            eq(1199, status.getTechniqueSlot4CooldownTicks());
        });
        test("technique cooldowns are transient and administrative reset clears slot three", () -> {
            StatusData status = new StatusData();
            status.setTechniqueSlot3CooldownTicks(300);
            status.setTechniqueSlot4CooldownTicks(1200);
            StatusData loaded = new StatusData();
            loaded.load(status.save());
            eq(0, loaded.getTechniqueSlot3CooldownTicks());
            eq(0, loaded.getTechniqueSlot4CooldownTicks());
            status.clearTechniqueTestState();
            eq(0, status.getTechniqueSlot3CooldownTicks());
            eq(0, status.getTechniqueSlot4CooldownTicks());
        });
        test("cylinder rejects enclosing box corners and targets above and below", () -> {
            var center = net.minecraft.world.phys.Vec3.ZERO;
            yes(!com.bleachmod.common.technique.TechniqueGeometry.intersectsCylinder(center, 5, 0, 8,
                    new net.minecraft.world.phys.AABB(4, 1, 4, 4.5, 2, 4.5)));
            yes(!com.bleachmod.common.technique.TechniqueGeometry.intersectsCylinder(center, 5, 0, 8,
                    new net.minecraft.world.phys.AABB(0, 8.1, 0, 1, 9, 1)));
            yes(!com.bleachmod.common.technique.TechniqueGeometry.intersectsCylinder(center, 5, 0, 8,
                    new net.minecraft.world.phys.AABB(0, -2, 0, 1, -0.1, 1)));
        });
        test("cylinder includes contact and intersecting edge hitboxes", () -> {
            var center = net.minecraft.world.phys.Vec3.ZERO;
            yes(com.bleachmod.common.technique.TechniqueGeometry.intersectsCylinder(center, 6, 1, 2,
                    new net.minecraft.world.phys.AABB(-0.3, 0, -0.3, 0.3, 1.8, 0.3)));
            yes(com.bleachmod.common.technique.TechniqueGeometry.intersectsCylinder(center, 6, 1, 2,
                    new net.minecraft.world.phys.AABB(5.9, 0, 0, 6.5, 1.8, 0.5)));
        });
        test("cleared tracking replaces old client value",()->{
            PlayerQuestData server=new PlayerQuestData(), client=new PlayerQuestData();
            client.setTrackedQuestId("old"); client.load(server.save());
            eq(null,client.getTrackedQuestId());
        });
        test("exact balance purchases one attribute",()->{
            ResourcesData r=new ResourcesData();r.addTrainingPoints(100);
            AttributeData a=new AttributeData();
            yes(a.purchase("zanjutsu",r));eq(1,a.level("zanjutsu"));eq(0F,r.getTrainingPoints());
        });
        test("insufficient balance does not change rank or points",()->{
            ResourcesData r=new ResourcesData();r.addTrainingPoints(99);
            AttributeData a=new AttributeData();
            yes(!a.purchase("reserve",r));eq(0,a.level("reserve"));eq(99F,r.getTrainingPoints());
        });
        test("unknown attribute cannot consume points",()->{
            ResourcesData r=new ResourcesData();r.addTrainingPoints(1000);
            yes(!new AttributeData().purchase("unknown",r));eq(1000F,r.getTrainingPoints());
        });
        test("attributes continue beyond the old level five cap",()->{
            ResourcesData r=new ResourcesData();r.addTrainingPoints(10000);AttributeData a=new AttributeData();
            for(int i=0;i<6;i++)yes(a.purchase("control",r));
            eq(6,a.level("control"));eq(700,a.cost("control"));
        });
        test("negative/NaN debit cannot create money or energy",()->{
            ResourcesData r=new ResourcesData();yes(!r.consumeTrainingPoints(-1));yes(!r.consumeReiatsu(Float.NaN));eq(100F,r.getCurrentReiatsu());
        });
        test("attribute NBT rejects negative ranks and preserves high ranks",()->{
            CompoundTag tag=new CompoundTag();tag.putInt("zanjutsu",-50);tag.putInt("reserve",900);
            AttributeData a=new AttributeData();a.load(tag);eq(0,a.level("zanjutsu"));eq(900,a.level("reserve"));
        });
        test("legacy power migrates to zanjutsu",()->{
            CompoundTag tag=new CompoundTag();tag.putInt("power",4);
            AttributeData a=new AttributeData();a.load(tag);eq(4,a.level("zanjutsu"));eq(0,a.level("hakuda"));
        });
        test("save roundtrip preserves progression",()->{
            PlayerData d=player();d.getResources().addTrainingPoints(750);d.getAttributes().purchase("reserve",d.getResources());d.refreshDerivedResources();
            d.getCharacter().unlockForm("shikai");d.getCharacter().setMastery("zanpakuto","shikai",42);
            PlayerData copy=new PlayerData();copy.load(d.save());eq(650F,copy.getResources().getTrainingPoints());eq(120F,copy.getResources().getMaxReiatsu());
            eq(42D,copy.getCharacter().getMastery("zanpakuto","shikai"));yes(copy.getCharacter().isFormDiscovered("shikai"));
        });
        test("raising reserve while full also fills the new maximum",()->{
            PlayerData d=player();d.getResources().addTrainingPoints(100);
            yes(d.getResources().isReiatsuFull());yes(d.getAttributes().purchase("reserve",d.getResources()));
            d.refreshDerivedResources();eq(120F,d.getResources().getCurrentReiatsu());eq(120F,d.getResources().getMaxReiatsu());
        });
        test("raising reserve while depleted preserves current reiatsu",()->{
            PlayerData d=player();d.getResources().setCurrentReiatsu(40);d.getResources().addTrainingPoints(100);
            yes(d.getAttributes().purchase("reserve",d.getResources()));d.refreshDerivedResources();
            eq(40F,d.getResources().getCurrentReiatsu());eq(120F,d.getResources().getMaxReiatsu());
        });
        test("legacy skills migrate discovered forms without losing mastery",()->{
            PlayerData d=player();d.getSkills().setSkillLevel("zanpakuto",2);d.getCharacter().setMastery("zanpakuto","bankai",100);
            CompoundTag old=d.save();old.remove("attributes");old.remove("schemaVersion");old.getCompound("character").remove("unlockedForms");
            PlayerData copy=new PlayerData();copy.load(old);yes(copy.getCharacter().isFormDiscovered("bankai"));eq(100D,copy.getCharacter().getMastery("zanpakuto","bankai"));eq(0,copy.getAttributes().level("zanjutsu"));
        });
        test("combat attributes use their separate damage paths",()->{
            eq(12F,CombatBalance.outgoingDamage(10F,2,0));
            eq(17F,CombatBalance.outgoingDamage(10F,2,.5F));
            eq(8F,CombatBalance.incomingPhysicalDamage(10F,5));
            eq(12F,CombatBalance.kidouDamage(10F,2));
            eq(.2F,CombatBalance.formDamageBonus("shikai"));
            eq(.5F,CombatBalance.formDamageBonus("bankai"));
        });
        test("control drain has diminishing returns and never becomes regeneration",()->{
            eq(.08F,CombatBalance.formDrain(.08F,0));
            yes(CombatBalance.formDrain(.08F,100)>0);
            yes(CombatBalance.formDrain(.08F,100)<.08F);
        });
        test("battle power is the weighted sum of all categories",()->{
            PlayerData d=player();d.getResources().addTrainingPoints(1000);
            yes(d.getAttributes().purchase("zanjutsu",d.getResources()));
            yes(d.getAttributes().purchase("hakuda",d.getResources()));
            eq(20D,d.getBattlePower());
        });
        test("undiscovered skill purchase cannot consume points",()->{
            PlayerData d=player();d.getResources().addTrainingPoints(500);
            yes(!ProgressionService.purchaseSkill(d,"zanpakuto"));eq(500F,d.getResources().getTrainingPoints());
            d.getCharacter().unlockForm("shikai");yes(ProgressionService.purchaseSkill(d,"zanpakuto"));eq(300F,d.getResources().getTrainingPoints());
        });
        test("zero mastery reward remains zero",()->{
            var reward=new TransformationReward("zanpakuto","shikai",0);
            eq(0D,reward.toJson().get("mastery").getAsDouble());
        });
        test("quest reward cannot be reclaimed after NBT roundtrip",()->{
            QuestProgress p=new QuestProgress("q");p.claimReward(0);p.setStatus(QuestStatus.SUCCESS);
            QuestProgress copy=QuestProgress.load(p.save());yes(copy.isRewardClaimed(0));yes(!copy.isRewardClaimed(1));
        });
        test("quest structural changes rejected but text edits accepted",()->{
            Quest q=quest();QuestProgress p=new QuestProgress("q");yes(p.bindDefinition(q));q.setDescription("new description");yes(p.bindDefinition(q));
            q.getRewards().add(new TpsReward(200));yes(!p.bindDefinition(q));yes(!QuestProgress.load(p.save()).matchesDefinition(q));
        });
        test("quest version changes require migration",()->{
            Quest q=quest();QuestProgress p=new QuestProgress("q");yes(p.bindDefinition(q));q.setVersion(2);yes(!p.bindDefinition(q));
        });
        test("forms ordered consistently regardless of JSON order",()->{
            JsonObject json=forms();JsonObject map=json.getAsJsonObject("shinigami").getAsJsonObject("zanpakuto").getAsJsonObject("forms");
            JsonObject reordered=new JsonObject();for(String key:List.of("bankai","sealed","shikai"))reordered.add(key,map.get(key));
            json.getAsJsonObject("shinigami").getAsJsonObject("zanpakuto").add("forms",reordered);
            eq(List.of("sealed","shikai","bankai"),new ArrayList<>(FormRegistry.parse(json.toString()).get("shinigami").get("zanpakuto").getForms().keySet()));
        });
        test("negative drain rejected before install",()->{
            JsonObject json=forms();json.getAsJsonObject("shinigami").getAsJsonObject("zanpakuto").getAsJsonObject("forms").getAsJsonObject("shikai").addProperty("energyDrain",-1);
            rejects(()->FormRegistry.parse(json.toString()));
        });
        test("quest-owned spawn modes roundtrip for the existing boss quests",()->{
            JsonObject q=quest().toJson();
            JsonObject objective=q.getAsJsonArray("objectives").get(0).getAsJsonObject();
            objective.addProperty("spawn","QUEST");
            objective.addProperty("count_mode","QUEST_SPAWNED_ONLY");
            KillObjective parsed=(KillObjective)QuestParser.parseQuest(q,null).getObjectives().get(0);
            eq(KillObjective.SpawnMode.QUEST,parsed.getSpawnMode());
            eq(KillObjective.CountMode.QUEST_SPAWNED_ONLY,parsed.getCountMode());
        });
        test("unknown quest spawn modes are rejected",()->{
            JsonObject q=quest().toJson();q.getAsJsonArray("objectives").get(0).getAsJsonObject().addProperty("spawn","INVALID");
            rejects(()->QuestParser.parseQuest(q,null));
        });
        test("empty objectives rejected",()->{
            JsonObject q=quest().toJson();q.add("objectives",new JsonArray());rejects(()->QuestParser.parseQuest(q,null));
        });
        test("transient reset preserves permanent progress",()->{
            PlayerData d=player();d.getResources().addTrainingPoints(88);d.getStatus().setActionCharging(true);d.getResources().setActionCharge(75);
            d.resetTransientState();yes(!d.getStatus().isActionCharging());eq(0,d.getResources().getActionCharge());eq(88F,d.getResources().getTrainingPoints());
        });
        test("training cannot restart before every reward is claimed",()->{
            Quest q=quest();q.setRepeatable(true);QuestProgress p=new QuestProgress("q");p.bindDefinition(q);
            p.setStatus(QuestStatus.SUCCESS);yes(!p.canRepeat(q));p.claimReward(0);yes(p.canRepeat(q));
            p.initializeRequirements(q);p.setStatus(QuestStatus.ACCEPTED);yes(!p.isRewardClaimed(0));yes(!p.canRepeat(q));
        });
        test("repeatability survives JSON roundtrip",()->{
            Quest q=quest();q.setRepeatable(true);
            yes(QuestParser.parseQuest(q.toJson(),null).isRepeatable());
        });
        test("quest giver survives JSON roundtrip",()->{
            Quest q=quest();q.setQuestGiver("rukia");
            eq("rukia",QuestParser.parseQuest(q.toJson(),null).getQuestGiver());
        });
        test("invalid quest giver is rejected",()->{
            JsonObject q=quest().toJson();q.addProperty("quest_giver","Rukia Kuchiki");
            rejects(()->QuestParser.parseQuest(q,null));
        });
        test("selection is revalidated after descending",()->{
            FormRegistry.replaceFromNetwork(forms().toString());
            PlayerData d=player();d.getSkills().setSkillLevel("zanpakuto",2);
            d.getCharacter().unlockForm("shikai");d.getCharacter().unlockForm("bankai");
            d.getCharacter().setMastery("zanpakuto","shikai",25);
            d.getCharacter().setActiveForm("zanpakuto","shikai");
            yes(TransformationsHelper.isSelectable(d,"zanpakuto","bankai"));
            d.getCharacter().setSelectedForm("zanpakuto","bankai");
            d.getCharacter().setActiveForm("zanpakuto","sealed");
            yes(!TransformationsHelper.isSelectable(d,"zanpakuto","bankai"));
            d.getCharacter().setMastery("zanpakuto","bankai",50);
            yes(TransformationsHelper.isSelectable(d,"zanpakuto","bankai"));
            FormRegistry.clearClient();
        });
        test("mastery boundary 24/25 controls bankai",()->{
            FormRegistry.replaceFromNetwork(forms().toString());
            PlayerData d=player();d.getSkills().setSkillLevel("zanpakuto",2);d.getCharacter().unlockForm("bankai");
            d.getCharacter().setMastery("zanpakuto","shikai",24);yes(!TransformationsHelper.isUnlocked(d,"zanpakuto","bankai"));
            d.getCharacter().setMastery("zanpakuto","shikai",25);yes(TransformationsHelper.isUnlocked(d,"zanpakuto","bankai"));
            FormRegistry.clearClient();
        });
        test("invalid client snapshot leaves previous definitions intact",()->{
            FormRegistry.replaceFromNetwork(forms().toString());
            rejects(()->FormRegistry.replaceFromNetwork("{}"));
            yes(FormRegistry.getForm("shinigami","zanpakuto","shikai")!=null);
            FormRegistry.clearClient();
        });
        test("provider data remains serializable after optional invalidation",()->{
            PlayerProvider provider=new PlayerProvider();provider.getData().initializeShinigami();
            provider.getData().getResources().addTrainingPoints(321);provider.invalidate();
            PlayerProvider replacement=new PlayerProvider();replacement.deserializeNBT(provider.serializeNBT());
            eq(321F,replacement.getData().getResources().getTrainingPoints());
        });
        test("HUD panel remains compact across GUI widths",()->{
            HudLayout.Panel narrow=HudLayout.panel(339,189), normal=HudLayout.panel(509,189), wide=HudLayout.panel(1017,189);
            yes(narrow.width()<normal.width());yes(normal.width()<wide.width());
            yes(narrow.width()<=339*.60F);yes(normal.width()<=509*.60F);yes(wide.width()<1017*.35F);
            yes(normal.x()>0);yes(normal.y()>0);
        });
        test("HUD information stays beside or above the hotbar",()->{
            HudLayout.Point beside=HudLayout.information(509,189,110,31);
            yes(beside.x()+110<=509/2-91);yes(beside.y()+31<=189);
            HudLayout.Point above=HudLayout.information(320,180,110,31);
            eq(6,above.x());yes(above.y()+31<=180-22-6);
        });
        test("client form snapshot cannot overwrite logical server snapshot",()->{
            var initial=FormRegistry.parse(forms().toString());FormRegistry.installServer(initial);
            JsonObject edited=forms();
            edited.getAsJsonObject("shinigami").getAsJsonObject("zanpakuto").getAsJsonObject("forms").getAsJsonObject("shikai").addProperty("energyDrain",.2);
            FormRegistry.replaceFromNetwork(edited.toString());
            eq(.2D,FormRegistry.getForm("shinigami","zanpakuto","shikai").getEnergyDrain());
            java.util.concurrent.FutureTask<Double> task=new java.util.concurrent.FutureTask<>(()->FormRegistry.getForm("shinigami","zanpakuto","shikai").getEnergyDrain());
            new Thread(net.minecraftforge.fml.util.thread.SidedThreadGroups.SERVER,task,"registry-regression").start();
            try { eq(.08D,task.get(5,java.util.concurrent.TimeUnit.SECONDS)); } catch(Exception e) { throw new AssertionError(e); }
            FormRegistry.clearClient();
        });
        test("self and forward form prerequisites are rejected",()->{
            for(String target:List.of("shikai","bankai")) {
                JsonObject json=forms();
                json.getAsJsonObject("shinigami").getAsJsonObject("zanpakuto").getAsJsonObject("forms").getAsJsonObject("shikai").addProperty("formRequisite","zanpakuto."+target);
                rejects(()->FormRegistry.parse(json.toString()));
            }
        });
        test("prerequisite mastery must be attainable",()->{
            JsonObject json=forms();
            JsonObject map=json.getAsJsonObject("shinigami").getAsJsonObject("zanpakuto").getAsJsonObject("forms");
            map.getAsJsonObject("shikai").addProperty("maxMastery",60);
            map.getAsJsonObject("bankai").addProperty("unlockOnMastery",61);
            rejects(()->FormRegistry.parse(json.toString()));
        });
        System.out.println("PASS: "+count+" regression scenarios");
    }
    private static PlayerData player(){PlayerData d=new PlayerData();d.initializeShinigami();return d;}
    private static Quest quest(){Quest q=new Quest();q.setStringId("q");q.getObjectives().add(new KillObjective("minecraft:zombie",5,KillObjective.SpawnMode.NATURAL,KillObjective.CountMode.ANY_MATCHING));q.getRewards().add(new TpsReward(100));return q;}
    private static JsonObject forms(){JsonObject r=new JsonObject(),groups=new JsonObject();groups.add("zanpakuto",FormDefaults.shinigamiGroup().toJson());r.add("shinigami",groups);return r;}
    private static void test(String name,Runnable action){try{action.run();count++;System.out.println("PASS "+name);}catch(Throwable e){throw new AssertionError(name,e);}}
    private static void yes(boolean value){if(!value)throw new AssertionError("Expected true");}
    private static void eq(Object expected,Object actual){if(!Objects.equals(expected,actual))throw new AssertionError("Expected "+expected+" but got "+actual);}
    private static void rejects(Runnable action){try{action.run();}catch(IllegalArgumentException e){return;}throw new AssertionError("Expected validation failure");}
}
