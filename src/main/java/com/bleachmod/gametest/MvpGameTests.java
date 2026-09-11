package com.bleachmod.gametest;

import com.bleachmod.common.RegistryReload;
import com.bleachmod.common.data.*;
import com.bleachmod.common.quest.*;
import com.bleachmod.common.quest.rewards.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.nio.file.Files;
import java.util.*;

/** Integration fixtures run only when Forge game tests are enabled. */
@GameTestHolder("bleachmod")
@PrefixGameTestTemplate(false)
public final class MvpGameTests {
    private static FakePlayer player(GameTestHelper helper) {
        FakePlayer player=new FakePlayer(helper.getLevel(),new GameProfile(UUID.randomUUID(),"MvpTest"));
        data(player).initializeShinigami();
        return player;
    }
    private static PlayerData data(FakePlayer player) {
        return PlayerCapability.get(player).orElseThrow(()->new IllegalStateException("Capability missing"));
    }

    @GameTest(template="empty")
    public static void clonePreservesProgressAfterInvalidation(GameTestHelper helper) {
        FakePlayer original=player(helper), replacement=player(helper);
        PlayerData data=data(original);
        data.getResources().addTrainingPoints(350);
        data.getCharacter().unlockForm("shikai");
        data.getSkills().setSkillLevel("zanpakuto",1);
        data.getCharacter().setMastery("zanpakuto","shikai",37);
        data.getCharacter().setActiveForm("zanpakuto","shikai");
        data.getStatus().setActionCharging(true);
        data.getResources().setActionCharge(80);
        original.invalidateCaps();
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.Clone(replacement,original,true));
        PlayerData restored=data(replacement);
        helper.assertTrue(restored.getResources().getTrainingPoints()==350,"Points lost during clone");
        helper.assertTrue(restored.getCharacter().getMastery("zanpakuto","shikai")==37,"Mastery lost during clone");
        helper.assertTrue(restored.getCharacter().getActiveForm().equals("sealed"),"Respawn must be sealed");
        helper.assertTrue(!restored.getStatus().isActionCharging()&&restored.getResources().getActionCharge()==0,"Charge persisted after clone");
        helper.succeed();
    }

    @GameTest(template="empty")
    public static void batchRewardsAreDeliveredOnce(GameTestHelper helper) {
        FakePlayer player=player(helper);
        PlayerData data=data(player);
        Quest quest=QuestRegistry.allQuests().stream().filter(q->"rukia_basic_training".equals(q.getStringId())).findFirst().orElseThrow();
        var original=new ArrayList<>(quest.getRewards());
        try {
            quest.getRewards().add(new ItemReward("minecraft:diamond",2));
            quest.getRewards().add(new TransformationReward("zanpakuto","shikai",0));
            data.getPlayerQuestData().acceptQuest(quest.getQuestKey(),quest);
            data.getPlayerQuestData().completeQuest(quest.getQuestKey());
            QuestService.claimReward(player,quest.getQuestKey(),-1);
            float balance=data.getResources().getTrainingPoints();
            helper.assertTrue(balance>0,"Batch did not deliver points");
            for(int n=0;n<5;n++) QuestService.claimReward(player,quest.getQuestKey(),-1);
            helper.assertTrue(data.getResources().getTrainingPoints()==balance,"Duplicate points");
            helper.assertTrue(player.getInventory().countItem(Items.DIAMOND)==2,"Duplicate or missing items");
            helper.assertTrue(data.getCharacter().isFormDiscovered("shikai"),"Missing transformation reward");
            helper.assertTrue(data.getCharacter().getMastery("zanpakuto","shikai")==0,"Unexpected mastery");
            helper.assertTrue(data.getPlayerQuestData().getProgress(quest.getQuestKey()).canRepeat(quest),"Training cannot repeat");
        } finally { quest.getRewards().clear(); quest.getRewards().addAll(original); }
        helper.succeed();
    }

    @GameTest(template="empty")
    public static void developerCommandsRequireOperator(GameTestHelper helper) {
        var root=helper.getLevel().getServer().getCommands().getDispatcher().getRoot();
        var source=player(helper).createCommandSourceStack();
        for(String command:List.of("bleachdev","bleachreload")) {
            helper.assertTrue(!root.getChild(command).canUse(source.withPermission(0)),"Non-operator can use "+command);
            helper.assertTrue(root.getChild(command).canUse(source.withPermission(2)),"Operator cannot use "+command);
        }
        helper.succeed();
    }

    @GameTest(template="empty")
    public static void invalidReloadPreservesActiveQuest(GameTestHelper helper) throws Exception {
        var server=helper.getLevel().getServer();
        var path=server.getWorldPath(LevelResource.ROOT).resolve("bleachmod/forms/shinigami.json");
        byte[] original=Files.readAllBytes(path);
        FakePlayer player=player(helper);
        Quest quest=QuestRegistry.allQuests().iterator().next();
        data(player).getPlayerQuestData().acceptQuest(quest.getQuestKey(),quest);
        try {
            Files.writeString(path,"{}");
            boolean rejected=false;
            try { RegistryReload.reload(server); } catch(Exception expected) { rejected=true; }
            helper.assertTrue(rejected,"Invalid reload accepted");
            helper.assertTrue(QuestRegistry.getQuest(quest.getQuestKey())==quest,"Previous registry replaced on failure");
            helper.assertTrue(data(player).getPlayerQuestData().isAccepted(quest.getQuestKey()),"Active quest lost");
        } finally { Files.write(path,original); RegistryReload.reload(server); }
        helper.assertTrue(data(player).getPlayerQuestData().getProgress(quest.getQuestKey()).matchesDefinition(QuestRegistry.getQuest(quest.getQuestKey())),"Valid reload invalidated progress");
        helper.succeed();
    }
}
