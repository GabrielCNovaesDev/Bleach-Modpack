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
    @GameTest(template = "empty")
    public static void spiritFlameSurvivesSaveAndPreservesOverlapAndReplacement(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlock(pos.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
        level.removeBlock(pos, false);
        var service = new com.bleachmod.common.technique.SpiritFlameService();
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        helper.assertTrue(service.place(level, pos, first) && service.place(level, pos, second), "Owners cannot overlap");
        var tag = service.save(new net.minecraft.nbt.CompoundTag());
        var owners = tag.getList("flames", 10).getCompound(0).getList("owners", 10);
        owners.getCompound(0).putLong("expires", level.getGameTime() - 1);
        var reloaded = com.bleachmod.common.technique.SpiritFlameService.load(tag);
        reloaded.expire(level);
        helper.assertTrue(level.getBlockState(pos).is(com.bleachmod.init.ModBlocks.SPIRIT_FLAME.get()), "One expired owner erased another's flame");
        var remaining = reloaded.save(new net.minecraft.nbt.CompoundTag());
        remaining.getList("flames", 10).getCompound(0).getList("owners", 10).getCompound(0).putLong("expires", level.getGameTime() - 1);
        com.bleachmod.common.technique.SpiritFlameService.load(remaining).expire(level);
        helper.assertTrue(level.getBlockState(pos).isAir(), "Persisted expired flame remains");
        helper.assertTrue(service.place(level, pos, first), "Could not replace expired flame");
        level.setBlock(pos, net.minecraft.world.level.block.Blocks.GOLD_BLOCK.defaultBlockState(), 3);
        service.expire(level);
        helper.assertTrue(level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.GOLD_BLOCK), "Cleanup erased a subsequent block");
        helper.assertTrue(!service.place(level, pos, second), "Flame overwrote solid terrain");
        level.removeBlock(pos, false);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 160)
    public static void spiritFlameExpiresInWorld(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlock(pos.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
        level.removeBlock(pos, false);
        helper.assertTrue(com.bleachmod.common.technique.SpiritFlameService.get(level).place(level, pos, UUID.randomUUID()), "World flame not placed");
        helper.runAfterDelay(125, () -> {
            helper.assertTrue(level.getBlockState(pos).isAir(), "World tick did not remove expired flame");
            helper.succeed();
        });
    }
    @GameTest(template = "empty")
    public static void shorterWallReachesFartherAndNeverBurnsRejectedTargets(GameTestHelper helper) {
        FakePlayer player = player(helper);
        // Templates are underground; a long wall must be tested in actual open air, outside the carved cell.
        player.setPos(player.getX(), helper.getLevel().getMaxBuildHeight() - 64, player.getZ());
        helper.getLevel().getChunkAt(BlockPos.containing(player.position().add(0, 0, 18)));
        Zombie distant = target(helper, player.position().add(0, 0, 18));
        Zombie high = target(helper, player.position().add(0, 9, 3));
        Zombie immune = target(helper, player.position().add(0, 0, 4));
        immune.setInvulnerable(true);
        helper.runAfterDelay(5, () -> { try {
            helper.assertTrue(player.hasLineOfSight(distant), "Distant fixture has obstructed sight");
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(Zombie.class, player.getBoundingBox().inflate(20))
                    .contains(distant), "Distant fixture not accessible in chunk entity lookup");
            TechniqueService.executeSlot(player, data(player), 3);
            helper.assertTrue(distant.getHealth() < 20, "Extended wall missed distant target");
            helper.assertTrue(high.getHealth() == 20, "Lower wall hit target above it");
            helper.assertTrue(!immune.isOnFire(), "Rejected damage still applied fire");
            helper.assertTrue(data(player).getResources().getCurrentReiatsu() == 60, "Wall cost changed");
        } finally { RyujinTechniqueService.cancel(player); distant.discard(); high.discard(); immune.discard(); }
        helper.succeed(); });
    }

    @GameTest(template = "empty")
    public static void bankaiFanUsesSharedAllyPolicy(GameTestHelper helper) {
        FakePlayer player = player(helper);
        data(player).getCharacter().setActiveForm("zanpakuto", "bankai");
        Zombie ally = target(helper, player.position().add(0, 0, 3));
        Zombie enemy = target(helper, player.position().add(1, 0, 4));
        var scoreboard = helper.getLevel().getScoreboard();
        var team = scoreboard.addPlayerTeam("ryu" + UUID.randomUUID().toString().substring(0, 8));
        scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
        scoreboard.addPlayerToTeam(ally.getScoreboardName(), team);
        try {
            TechniqueService.executeSlot(player, data(player), 2);
            helper.assertTrue(ally.getHealth() == 20 && !ally.isOnFire(), "Bankai fan harmed ally");
            helper.assertTrue(enemy.getHealth() < 20, "Bankai fan missed enemy");
        } finally { scoreboard.removePlayerTeam(team); ally.discard(); enemy.discard(); }
        helper.succeed();
    }
    private static void command(FakePlayer player, String command) {
        try {
            int result = player.getServer().getCommands().getDispatcher().execute(
                    "bleachdev " + command + " @s", player.createCommandSourceStack().withPermission(2));
            if (result != 1) throw new IllegalStateException("Command failed: " + command);
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @GameTest(template = "empty")
    public static void operatorCommandsAllowRepeatedFreeCastsAndRestoreRules(GameTestHelper helper) {
        FakePlayer player = player(helper);
        PlayerData data = data(player);
        try {
            data.getResources().setCurrentReiatsu(0);
            command(player, "reiatsu free");
            command(player, "cooldowns disable");
            for (int i = 0; i < 3; i++) {
                TechniqueService.executeSlot(player, data, 3);
                TechniqueService.executeSlot(player, data, 4);
            }
            helper.assertTrue(data.getResources().getCurrentReiatsu() == 0, "Free casts changed balance");
            helper.assertTrue(data.getStatus().getTechniqueSlot3CooldownTicks() == 0, "Wall cooldown enabled");
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(Bat.class,
                    player.getBoundingBox().inflate(4), bat -> bat.getPersistentData().hasUUID("bleachmod_flame_bat_owner")
                            && bat.getPersistentData().getUUID("bleachmod_flame_bat_owner").equals(player.getUUID())).size() == 5,
                    "Repeated casts duplicated or failed to summon bats");
            data.getStatus().setIgnitionActive(true);
            command(player, "cooldowns clear");
            helper.assertTrue(data.getStatus().areCooldownsDisabled(), "Clear changed test mode");
            helper.assertTrue(data.getStatus().isIgnitionActive(), "Clear reset ignition");
            TechniqueService.tickIgnition(player, data);
            helper.assertTrue(data.getStatus().isIgnitionActive(), "Free ignition failed at zero balance");
            command(player, "cooldowns restore");
            command(player, "reiatsu restore");
            TechniqueService.tickIgnition(player, data);
            helper.assertTrue(!data.getStatus().isIgnitionActive(), "Normal ignition ignored empty balance");
            TechniqueService.executeSlot(player, data, 3);
            helper.assertTrue(data.getStatus().getTechniqueSlot3CooldownTicks() == 0, "Zero balance cast in normal mode");
            data.getResources().setCurrentReiatsu(100);
            TechniqueService.executeSlot(player, data, 3);
            helper.assertTrue(data.getResources().getCurrentReiatsu() == 60, "Wall cost not restored");
            helper.assertTrue(data.getStatus().getTechniqueSlot3CooldownTicks() == 400, "Wall cooldown not restored");
            command(player, "cooldowns clear");
            helper.assertTrue(data.getStatus().getTechniqueSlot3CooldownTicks() == 0, "Clear did not clear cooldown");
        } finally { RyujinTechniqueService.cancel(player); }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void operatorCommandsRequirePermission(GameTestHelper helper) {
        FakePlayer player = player(helper);
        for (String command : java.util.List.of("cooldowns disable", "cooldowns restore", "cooldowns clear",
                "reiatsu free", "reiatsu restore")) {
            boolean rejected = false;
            try {
                player.getServer().getCommands().getDispatcher().execute("bleachdev " + command + " @s",
                        player.createCommandSourceStack().withPermission(0));
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException expected) { rejected = true; }
            helper.assertTrue(rejected, "Non-operator command allowed: " + command);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void freeReiatsuMaintainsTransformationsAtZeroBalance(GameTestHelper helper) {
        FakePlayer player = player(helper);
        PlayerData data = data(player);
        data.getSkills().get("zanpakuto").setLevel(2);
        data.getCharacter().unlockForm("shikai");
        data.getCharacter().unlockForm("bankai");
        data.getCharacter().setMastery("zanpakuto", "shikai", 100);
        data.getCharacter().setMastery("zanpakuto", "bankai", 100);
        data.getResources().setCurrentReiatsu(0);
        command(player, "reiatsu free");
        for (String form : java.util.List.of("shikai", "bankai")) {
            data.getCharacter().setSelectedForm("zanpakuto", form);
            com.bleachmod.server.events.FormModeHandler.attemptTransform(player, data);
            helper.assertTrue(form.equals(data.getCharacter().getActiveForm()), "Free transform failed: " + form);
            com.bleachmod.server.events.TickHandler.onPlayerTick(new net.minecraftforge.event.TickEvent.PlayerTickEvent(
                    net.minecraftforge.event.TickEvent.Phase.END, player));
            helper.assertTrue(form.equals(data.getCharacter().getActiveForm()), "Free form reverted: " + form);
            helper.assertTrue(data.getResources().getCurrentReiatsu() == 0, "Free form changed balance");
        }
        command(player, "reiatsu restore");
        com.bleachmod.server.events.TickHandler.onPlayerTick(new net.minecraftforge.event.TickEvent.PlayerTickEvent(
                net.minecraftforge.event.TickEvent.Phase.END, player));
        helper.assertTrue("sealed".equals(data.getCharacter().getActiveForm()), "Normal drain did not revert empty Bankai");
        helper.succeed();
    }
    private static FakePlayer player(GameTestHelper helper) {
        FakePlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "RyujinTest"));
        data(player).initializeShinigami();
        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.RYUJIN_JAKKA.get()));
        BlockPos pos = helper.absolutePos(new BlockPos(1, 3, 1));
        player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        player.setYRot(0);
        return player;
    }
    private static PlayerData data(FakePlayer player) {
        return PlayerCapability.get(player).orElseThrow(() -> new IllegalStateException("Capability missing"));
    }
    private static Zombie target(GameTestHelper helper, Vec3 position) {
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        target.setNoAi(true);
        target.setNoGravity(true);
        target.setItemSlot(EquipmentSlot.HEAD, new ItemStack(net.minecraft.world.item.Items.IRON_HELMET));
        target.setPos(position);
        helper.getLevel().addFreshEntity(target);
        return target;
    }

    @GameTest(template = "empty")
    public static void wallHitsOnlyInsideAndChargesOnce(GameTestHelper helper) {
        FakePlayer player = player(helper);
        PlayerData data = data(player);
        Zombie inside = target(helper, player.position().add(0, 0, 3));
        Zombie outside = target(helper, player.position().add(3, 0, 3));
        try {
            TechniqueService.executeSlot(player, data, 3);
            helper.assertTrue(inside.getHealth() < 20, "Inside target not damaged");
            helper.assertTrue(outside.getHealth() == 20, "Outside target damaged");
            helper.assertTrue(player.getHealth() == player.getMaxHealth(), "Caster damaged");
            helper.assertTrue(data.getResources().getCurrentReiatsu() == 60, "Wrong wall debit");
            TechniqueService.executeSlot(player, data, 3);
            helper.assertTrue(data.getResources().getCurrentReiatsu() == 60, "Cooldown consumed more reiatsu");
        } finally { RyujinTechniqueService.cancel(player); inside.discard(); outside.discard(); }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void invalidWallDoesNotDebit(GameTestHelper helper) {
        FakePlayer player = player(helper);
        PlayerData data = data(player);
        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.ASAUCHI.get()));
        TechniqueService.executeSlot(player, data, 3);
        helper.assertTrue(data.getResources().getCurrentReiatsu() == 100, "Asauchi authorized wall");
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
    public static void tornadoCenterMovesThreeBlocksAheadForParticlesAndDamage(GameTestHelper helper) {
        var p = player(helper); data(p).getCharacter().setActiveForm("zanpakuto", "bankai");
        // Other fixtures cast long-range skills in open air at maxHeight-64; isolate this negative control vertically.
        p.setPos(p.getX(), helper.getLevel().getMaxBuildHeight() - 24, p.getZ());
        helper.getLevel().getChunkAt(BlockPos.containing(p.position().add(0, 0, 8)));
        helper.getLevel().getChunkAt(BlockPos.containing(p.position().add(0, 0, -4)));
        var forward = target(helper, p.position().add(0, 0, 8));
        var behind = target(helper, p.position().add(0, 0, -4));
        helper.runAfterDelay(5, () -> { try {
            TechniqueService.executeSlot(p, data(p), 3); RyujinTechniqueService.tickEffects(p);
            helper.assertTrue(forward.getHealth() < 20, "Offset tornado missed forward target inside shifted radius");
            helper.assertTrue(behind.getHealth() == 20, "Tornado damage remained centered on player");
        } finally { RyujinTechniqueService.cancel(p); forward.discard(); behind.discard(); } helper.succeed(); });
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

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void batsActuallyMoveAndAttackHostileMobs(GameTestHelper helper) {
        FakePlayer player = player(helper);
        Zombie hostile = target(helper, player.position().add(5, 0, 0));
        hostile.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);
        hostile.setHealth(100);
        var cow = EntityType.COW.create(helper.getLevel());
        cow.setNoAi(true);
        cow.setNoGravity(true);
        cow.setPos(player.position().add(1, 0, 0));
        helper.getLevel().addFreshEntity(cow);
        TechniqueService.executeSlot(player, data(player), 4);
        var bats = helper.getLevel().getEntitiesOfClass(Bat.class, player.getBoundingBox().inflate(4),
                bat -> bat.getPersistentData().hasUUID("bleachmod_flame_bat_owner")
                        && bat.getPersistentData().getUUID("bleachmod_flame_bat_owner").equals(player.getUUID()));
        var initialPositions = new java.util.HashMap<UUID, Vec3>();
        bats.forEach(bat -> initialPositions.put(bat.getUUID(), bat.position()));
        helper.runAtTickTime(40, () -> {
            try {
                helper.assertTrue(bats.stream().anyMatch(bat -> bat.position()
                                .distanceToSqr(initialPositions.get(bat.getUUID())) > 0.25),
                        "Summons remain stationary after actual world ticks");
                helper.assertTrue(hostile.getHealth() < 100, "Hostile mob never attacked");
                helper.assertTrue(cow.getHealth() == cow.getMaxHealth(), "Passive mob attacked");
                helper.succeed();
            } finally { RyujinTechniqueService.cancel(player); hostile.discard(); cow.discard(); }
        });
    }

    @GameTest(template = "empty", timeoutTicks = 140)
    public static void wallExpiresAfterFiveSeconds(GameTestHelper helper) {
        FakePlayer player = player(helper);
        TechniqueService.executeSlot(player, data(player), 3);
        helper.runAtTickTime(105, () -> {
            Zombie late = target(helper, player.position().add(0, 0, 3));
            try {
                RyujinTechniqueService.tickEffects(player);
                helper.assertTrue(late.getHealth() == 20, "Expired wall damaged a new target");
                data(player).getStatus().setTechniqueSlot3CooldownTicks(0);
                TechniqueService.executeSlot(player, data(player), 3);
                helper.assertTrue(data(player).getResources().getCurrentReiatsu() == 20,
                        "Expired wall still blocks a new cast");
                helper.succeed();
            } finally { RyujinTechniqueService.cancel(player); late.discard(); }
        });
    }
}
