package com.bleachmod.gametest;

import com.bleachmod.common.data.PlayerCapability;
import com.bleachmod.common.data.PlayerData;
import com.bleachmod.common.technique.RyujinTechniqueService;
import com.bleachmod.common.technique.TechniqueService;
import com.bleachmod.init.ModItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

@GameTestHolder("bleachmod")
@PrefixGameTestTemplate(false)
public final class RyujinGameTests {
    private static FakePlayer player(GameTestHelper helper) {
        FakePlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "RyujinTest"));
        data(player).initializeShinigami();
        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.RYUJIN_JAKKA.get()));
        BlockPos pos = helper.absolutePos(new BlockPos(1, 3, 1));
        player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        return player;
    }
    private static PlayerData data(FakePlayer player) {
        return PlayerCapability.get(player).orElseThrow(() -> new IllegalStateException("Capability missing"));
    }
    private static Zombie target(GameTestHelper helper, Vec3 position) {
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        target.setNoAi(true);
        target.setNoGravity(true);
        target.setPos(position);
        helper.getLevel().addFreshEntity(target);
        return target;
    }

    @GameTest(template = "empty")
    public static void circleHitsOnlyInsideAndChargesOnce(GameTestHelper helper) {
        FakePlayer player = player(helper);
        PlayerData data = data(player);
        Zombie inside = target(helper, player.position().add(2, 0, 0));
        Zombie outside = target(helper, player.position().add(7, 0, 0));
        try {
            TechniqueService.executeSlot(player, data, 3);
            helper.assertTrue(inside.getHealth() < 20, "Inside target not damaged");
            helper.assertTrue(outside.getHealth() == 20, "Outside target damaged");
            helper.assertTrue(player.getHealth() == player.getMaxHealth(), "Caster damaged");
            helper.assertTrue(data.getResources().getCurrentReiatsu() == 70, "Wrong circle debit");
            TechniqueService.executeSlot(player, data, 3);
            helper.assertTrue(data.getResources().getCurrentReiatsu() == 70, "Cooldown consumed more reiatsu");
        } finally { RyujinTechniqueService.cancel(player); inside.discard(); outside.discard(); }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void invalidCircleDoesNotDebit(GameTestHelper helper) {
        FakePlayer player = player(helper);
        PlayerData data = data(player);
        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.ASAUCHI.get()));
        TechniqueService.executeSlot(player, data, 3);
        helper.assertTrue(data.getResources().getCurrentReiatsu() == 100, "Asauchi authorized circle");
        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.RYUJIN_JAKKA.get()));
        data.getResources().setCurrentReiatsu(29);
        TechniqueService.executeSlot(player, data, 3);
        helper.assertTrue(data.getResources().getCurrentReiatsu() == 29, "Insufficient balance debited");
        helper.assertTrue(data.getStatus().getTechniqueSlot3CooldownTicks() == 0, "Failed cast started cooldown");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tornadoUsesBankaiCostAndSharedSlot(GameTestHelper helper) {
        FakePlayer player = player(helper);
        PlayerData data = data(player);
        data.getCharacter().setActiveForm("zanpakuto", "bankai");
        try {
            TechniqueService.executeSlot(player, data, 3);
            helper.assertTrue(data.getResources().getCurrentReiatsu() == 60, "Wrong tornado debit");
            helper.assertTrue(data.getStatus().getTechniqueSlot3CooldownTicks() == 300, "Wrong tornado cooldown");
            data.getCharacter().setActiveForm("zanpakuto", "shikai");
            data.getStatus().clearTransformationState();
            TechniqueService.executeSlot(player, data, 3);
            helper.assertTrue(data.getResources().getCurrentReiatsu() == 60, "Changing form bypassed slot cooldown");
        } finally { RyujinTechniqueService.cancel(player); }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void batSwarmBlocksDuplicatesAndCleansEntities(GameTestHelper helper) {
        FakePlayer player = player(helper);
        PlayerData data = data(player);
        AABB box = player.getBoundingBox().inflate(4);
        try {
            TechniqueService.executeSlot(player, data, 4);
            var bats = helper.getLevel().getEntitiesOfClass(Bat.class, box,
                    bat -> bat.getPersistentData().hasUUID("bleachmod_flame_bat_owner")
                            && bat.getPersistentData().getUUID("bleachmod_flame_bat_owner").equals(player.getUUID()));
            helper.assertTrue(bats.size() == 5, "Must spawn exactly five bats");
            helper.assertTrue(bats.stream().allMatch(bat -> bat.getMaxHealth() == 4), "Wrong summon health");
            data.getStatus().setTechniqueSlot4CooldownTicks(0);
            TechniqueService.executeSlot(player, data, 4);
            helper.assertTrue(data.getResources().getCurrentReiatsu() == 75, "Active swarm permitted duplicate debit");
            RyujinTechniqueService.cancel(player);
            helper.assertTrue(bats.stream().allMatch(Bat::isRemoved), "Cancel left live summons");
            TechniqueService.executeSlot(player, data, 4);
            helper.assertTrue(data.getResources().getCurrentReiatsu() == 50, "Cleanup left cast blocked");
        } finally { RyujinTechniqueService.cancel(player); }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void steamCutCooldownSurvivesFormSwitch(GameTestHelper helper) {
        FakePlayer player = player(helper);
        PlayerData data = data(player);
        data.getCharacter().setActiveForm("zanpakuto", "bankai");
        data.getStatus().setTechniqueSlot4CooldownTicks(TechniqueService.STEAM_CUT_COOLDOWN_TICKS);
        data.getStatus().clearTransformationState();
        TechniqueService.executeSlot(player, data, 4);
        helper.assertTrue(data.getResources().getCurrentReiatsu() == 100, "Cooldown charged steam cut");
        helper.assertTrue(data.getStatus().getTechniqueSlot4CooldownTicks() == 1200, "Minute cooldown lost");
        helper.succeed();
    }
}
