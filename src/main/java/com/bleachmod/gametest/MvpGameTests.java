package com.bleachmod.gametest;

import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.data.PlayerData;
import com.bleachmod.common.quest.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

/** Integration fixtures, only registered when Forge game tests are enabled. */
@GameTestHolder("bleachmod")
@PrefixGameTestTemplate(false)
public final class MvpGameTests {
    private static FakePlayer player(GameTestHelper helper) {
        return new FakePlayer(helper.getLevel(),new GameProfile(UUID.randomUUID(),"MvpTest"));
    }

    @GameTest(template="empty")
    public static void clonePreservesProgressAfterInvalidation(GameTestHelper helper) {
        FakePlayer original=player(helper), replacement=player(helper);
        PlayerData data=PlayerCapability.get(original).orElseThrow(() -> new IllegalStateException("Missing test fixture"));
        data.initializeShinigami();
        data.getResources().addTrainingPoints(350);
        data.getCharacter().unlockForm("shikai");
        data.getSkills().setSkillLevel("zanpakuto",1);
        data.getCharacter().setMastery("zanpakuto","shikai",37);
        data.getCharacter().setActiveForm("zanpakuto","shikai");
        data.getStatus().setActionCharging(true);
        data.getResources().setActionCharge(80);
        original.invalidateCaps();
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.Clone(replacement,original,true));
        PlayerData restored=PlayerCapability.get(replacement).orElseThrow(() -> new IllegalStateException("Missing test fixture"));
        helper.assertTrue(restored.getResources().getTrainingPoints()==350,"Points lost during clone");
        helper.assertTrue(restored.getCharacter().getMastery("zanpakuto","shikai")==37,"Mastery lost during clone");
        helper.assertTrue(restored.getCharacter().getActiveForm().equals("sealed"),"Respawn must be sealed");
        helper.assertTrue(!restored.getStatus().isActionCharging()&&restored.getResources().getActionCharge()==0,"Charge persisted after clone");
        helper.succeed();
    }

    @GameTest(template="empty")
    public static void batchRewardCannotBeClaimedTwice(GameTestHelper helper) {
        FakePlayer player=player(helper);
        PlayerData data=PlayerCapability.get(player).orElseThrow(() -> new IllegalStateException("Missing test fixture"));
        data.initializeShinigami();
        Quest quest=QuestRegistry.allQuests().stream().filter(q->"rukia_basic_training".equals(q.getStringId())).findFirst().orElseThrow(() -> new IllegalStateException("Missing test fixture"));
        data.getPlayerQuestData().acceptQuest(quest.getQuestKey(),quest);
        data.getPlayerQuestData().completeQuest(quest.getQuestKey());
        QuestService.claimReward(player,quest.getQuestKey(),-1);
        float balance=data.getResources().getTrainingPoints();
        helper.assertTrue(balance>0,"Batch did not deliver points");
        QuestService.claimReward(player,quest.getQuestKey(),-1);
        helper.assertTrue(data.getResources().getTrainingPoints()==balance,"Duplicate batch delivered twice");
        helper.assertTrue(data.getPlayerQuestData().getProgress(quest.getQuestKey()).canRepeat(quest),"Claimed training cannot repeat");
        helper.succeed();
    }
}
