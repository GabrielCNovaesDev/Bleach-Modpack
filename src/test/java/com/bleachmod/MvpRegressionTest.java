package com.bleachmod;

import com.bleachmod.common.data.*;
import com.bleachmod.common.quest.*;
import com.bleachmod.common.quest.objectives.*;
import com.bleachmod.common.quest.rewards.*;
import com.bleachmod.common.evolution.*;
import com.bleachmod.common.ProgressionService;
import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import java.util.*;

/** Dependency-free regression runner, executed by Gradle check even offline. */
public final class MvpRegressionTest {
    private static int count;
    public static void main(String[] args) {
        test("cleared tracking replaces old client value",()->{
            PlayerQuestData server=new PlayerQuestData(), client=new PlayerQuestData();
            client.setTrackedQuestId("old"); client.load(server.save());
            eq(null,client.getTrackedQuestId());
        });
        test("exact balance purchases one attribute",()->{
            ResourcesData r=new ResourcesData();r.addTrainingPoints(100);
            AttributeData a=new AttributeData();
            yes(a.purchase("power",r));eq(1,a.level("power"));eq(0F,r.getTrainingPoints());
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
        test("attribute cap survives repeated purchases",()->{
            ResourcesData r=new ResourcesData();r.addTrainingPoints(10000);AttributeData a=new AttributeData();
            for(int i=0;i<5;i++)yes(a.purchase("control",r));
            float before=r.getTrainingPoints();yes(!a.purchase("control",r));eq(before,r.getTrainingPoints());eq(5,a.level("control"));
        });
        test("negative/NaN debit cannot create money or energy",()->{
            ResourcesData r=new ResourcesData();yes(!r.consumeTrainingPoints(-1));yes(!r.consumeReiatsu(Float.NaN));eq(100F,r.getCurrentReiatsu());
        });
        test("attribute NBT rejects negative and excessive ranks",()->{
            CompoundTag tag=new CompoundTag();tag.putInt("power",-50);tag.putInt("reserve",900);
            AttributeData a=new AttributeData();a.load(tag);eq(0,a.level("power"));eq(5,a.level("reserve"));
        });
        test("save roundtrip preserves progression",()->{
            PlayerData d=player();d.getResources().addTrainingPoints(750);d.getAttributes().purchase("reserve",d.getResources());d.refreshDerivedResources();
            d.getCharacter().unlockForm("shikai");d.getCharacter().setMastery("zanpakuto","shikai",42);
            PlayerData copy=new PlayerData();copy.load(d.save());eq(650F,copy.getResources().getTrainingPoints());eq(120F,copy.getResources().getMaxReiatsu());
            eq(42D,copy.getCharacter().getMastery("zanpakuto","shikai"));yes(copy.getCharacter().isFormDiscovered("shikai"));
        });
        test("legacy skills migrate discovered forms without losing mastery",()->{
            PlayerData d=player();d.getSkills().setSkillLevel("zanpakuto",2);d.getCharacter().setMastery("zanpakuto","bankai",100);
            CompoundTag old=d.save();old.remove("attributes");old.remove("schemaVersion");old.getCompound("character").remove("unlockedForms");
            PlayerData copy=new PlayerData();copy.load(old);yes(copy.getCharacter().isFormDiscovered("bankai"));eq(100D,copy.getCharacter().getMastery("zanpakuto","bankai"));eq(0,copy.getAttributes().level("power"));
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
        test("invalid content cannot spawn quest-owned mobs",()->{
            JsonObject q=quest().toJson();q.getAsJsonArray("objectives").get(0).getAsJsonObject().addProperty("spawn","QUEST");
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
        test("client form snapshot cannot overwrite logical server snapshot",()->{
            var initial=FormRegistry.parse(forms().toString());FormRegistry.installServer(initial);
            JsonObject edited=forms();
            edited.getAsJsonObject("shinigami").getAsJsonObject("zanpakuto").getAsJsonObject("forms").getAsJsonObject("shikai").addProperty("energyDrain",.2);
            FormRegistry.replaceFromNetwork(edited.toString());
            eq(.2D,FormRegistry.getForm("shinigami","zanpakuto","shikai").getEnergyDrain());
            java.util.concurrent.FutureTask<Double> task=new java.util.concurrent.FutureTask<>(()->FormRegistry.getForm("shinigami","zanpakuto","shikai").getEnergyDrain());
            new Thread(net.minecraftforge.fml.util.thread.SidedThreadGroups.SERVER,task,"registry-regression").start();
            try { eq(.4D,task.get(5,java.util.concurrent.TimeUnit.SECONDS)); } catch(Exception e) { throw new AssertionError(e); }
            FormRegistry.clearClient();
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
