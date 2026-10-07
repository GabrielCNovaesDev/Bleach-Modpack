package com.bleachmod.gametest;

import com.bleachmod.common.data.*;
import com.bleachmod.common.technique.*;
import com.bleachmod.init.ModItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.*;
import java.util.UUID;

@GameTestHolder("bleachmod") @PrefixGameTestTemplate(false)
public final class IceRefinementGameTests {
    private static PlayerData data(FakePlayer p) { return PlayerCapability.get(p).orElseThrow(() -> new IllegalStateException("No data")); }
    private static FakePlayer player(GameTestHelper h) {
        var p = new FakePlayer(h.getLevel(), new GameProfile(UUID.randomUUID(), "IcePolish")); data(p).initializeShinigami();
        data(p).getCharacter().bindZanpakuto("hyorinmaru"); p.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.HYORINMARU.get()));
        BlockPos pos = h.absolutePos(new BlockPos(1,3,1)); p.setPos(pos.getX()+0.5,h.getLevel().getMaxBuildHeight()-64,pos.getZ()+0.5); p.setYRot(0); return p;
    }
    private static Zombie mob(GameTestHelper h, FakePlayer p, int ahead) {
        var z = EntityType.ZOMBIE.create(h.getLevel()); z.setNoAi(true); z.setNoGravity(true);
        z.setItemSlot(EquipmentSlot.HEAD, new ItemStack(net.minecraft.world.item.Items.IRON_HELMET));
        z.setPos(p.getX(),p.getY(),p.getZ()+ahead); h.getLevel().getChunkAt(z.blockPosition()); h.getLevel().addFreshEntity(z); return z;
    }
    @GameTest(template="empty",timeoutTicks=145)
    public static void sealedArmorReducesDamageAndExpiresWithoutPersistentBuff(GameTestHelper h) {
        var p = player(h); var z = mob(h,p,2); TechniqueService.executeSlot(p,data(p),3);
        var hit = new net.minecraftforge.event.entity.living.LivingHurtEvent(p,p.damageSources().mobAttack(z),4);
        com.bleachmod.server.events.CombatEvents.hurt(hit);
        h.assertTrue(hit.getAmount()==3 && IceArmorService.active(p),"Armor did not reduce physical damage by 25%");
        h.assertTrue(data(p).getResources().getCurrentReiatsu()==88,"Armor cost incorrect");
        h.assertTrue(data(p).getCharacter().saveAppearance().getBoolean("iceArmorVisual") && !data(p).getCharacter().save().contains("iceArmorVisual"),"Visual armor persisted as progress");
        h.onEachTick(() -> IceArmorService.tick(p));
        h.runAfterDelay(125,()->{ try {
            h.assertTrue(!IceArmorService.active(p) && !data(p).getCharacter().isIceArmorVisual(),"Expired armor remained active");
            var normal = new net.minecraftforge.event.entity.living.LivingHurtEvent(p,p.damageSources().mobAttack(z),4);
            com.bleachmod.server.events.CombatEvents.hurt(normal); h.assertTrue(normal.getAmount()==4,"Expired armor still reduced damage");
        } finally { HyorinmaruTechniqueService.cancel(p); z.discard(); } h.succeed(); });
    }
    @GameTest(template="empty",timeoutTicks=70)
    public static void sealedDuelFreezesOnlyNearestTargetAndDealsVanillaColdDamage(GameTestHelper h) {
        var p=player(h); var near=mob(h,p,3); var far=mob(h,p,4);
        h.runAfterDelay(5,()->{
            TechniqueService.executeSlot(p,data(p),4);
            h.assertTrue(near.getTicksFrozen()>=near.getTicksRequiredToFreeze() && far.getTicksFrozen()==0,"Duel was not single-target");
            h.assertTrue(near.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN) && near.getDeltaMovement().lengthSqr()>0,"Duel lacked slow or knockback");
            h.assertTrue(data(p).getResources().getCurrentReiatsu()==82,"Duel cost incorrect");
        });
        h.runAfterDelay(50,()->{ try { h.assertTrue(near.getHealth()<20 && far.getHealth()==20,"Vanilla freezing damage missing or affected second target"); }
            finally { HyorinmaruTechniqueService.cancel(p); near.discard(); far.discard(); } h.succeed(); });
    }
    @GameTest(template="empty",timeoutTicks=55)
    public static void sealedSlashReachesSixBlocksAndIceDragonIsTemporary(GameTestHelper h) {
        var p=player(h); var z=mob(h,p,5);
        h.runAfterDelay(5,()->{
            TechniqueService.executeSlot(p,data(p),1); h.assertTrue(z.getHealth()<20,"Longer sealed slash missed target at five blocks");
            data(p).getStatus().setTechniqueSlot1CooldownTicks(0); data(p).getCharacter().unlockForm("shikai"); data(p).getSkills().get("zanpakuto").setLevel(1);
            data(p).getCharacter().setActiveForm("zanpakuto","shikai"); TechniqueService.executeSlot(p,data(p),1);
            var dragons=h.getLevel().getEntitiesOfClass(com.bleachmod.entity.IceDragonEntity.class,p.getBoundingBox().inflate(6));
            h.assertTrue(!dragons.isEmpty() && dragons.stream().noneMatch(net.minecraft.world.entity.Entity::isPickable),"Native ice model not spawned or pickable");
        });
        h.runAfterDelay(35,()->{ try {
            h.assertTrue(h.getLevel().getEntitiesOfClass(com.bleachmod.entity.IceDragonEntity.class,p.getBoundingBox().inflate(20)).isEmpty(),"Expired cosmetic dragon remains");
        } finally { HyorinmaruTechniqueService.cancel(p); z.discard(); } h.succeed(); });
    }
    @GameTest(template="empty",batch="ice_weather")
    public static void weatherRestoresOverlapAndRestartAndRespectsDesertAndDimensions(GameTestHelper h) {
        var level=h.getLevel(); var weather=level.getServer().getWorldData().overworldData();
        int oldClear=weather.getClearWeatherTime(),oldRain=weather.getRainTime(),oldThunder=weather.getThunderTime();
        boolean rain=weather.isRaining(),thunder=weather.isThundering(); float rainLevel=level.getRainLevel(1),thunderLevel=level.getThunderLevel(1);
        try {
            level.setWeatherParameters(400,600,false,false);
            var control=new BlizzardWeatherData(); UUID first=UUID.randomUUID(),second=UUID.randomUUID();
            control.acquire(level,first);control.acquire(level,second);control.release(level,first);
            h.assertTrue(weather.isRaining() && control.isManaged(),"First release cancelled another Bankai");
            var persisted=control.save(new CompoundTag());control.release(level,second);
            h.assertTrue(!weather.isRaining() && weather.getClearWeatherTime()==400,"Previous climate not restored exactly");
            control.acquire(level,first); var recovered=BlizzardWeatherData.load(control.save(new CompoundTag()));recovered.tick(level);
            h.assertTrue(!weather.isRaining() && !recovered.isManaged(),"Save/restart left a storm without owners");
            var biomes=level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
            h.assertTrue(!BlizzardWeatherData.supportsBiome(biomes.get(net.minecraft.world.level.biome.Biomes.DESERT),BlockPos.ZERO),"Desert accepts snowfall");
            var nether=level.getServer().getLevel(net.minecraft.world.level.Level.NETHER);
            h.assertTrue(nether==null || !BlizzardWeatherData.eligible(nether,BlockPos.ZERO),"Nether accepts climate changes");
            control=new BlizzardWeatherData();control.acquire(level,first);level.setWeatherParameters(900,900,false,false);control.release(level,first);
            h.assertTrue(weather.getClearWeatherTime()==900,"Cleanup undid subsequent /weather command");
        } finally {
            weather.setClearWeatherTime(oldClear);weather.setRainTime(oldRain);weather.setThunderTime(oldThunder);weather.setRaining(rain);weather.setThundering(thunder);
            level.setRainLevel(rainLevel);level.setThunderLevel(thunderLevel);
        } h.succeed();
    }

    @GameTest(template="empty",batch="weather_elapsed",timeoutTicks=85)
    public static void weatherRestoresAfterElapsedTicksAndManualClearStaysClear(GameTestHelper h) {
        var level = h.getLevel(); var weather = level.getServer().getWorldData().overworldData();
        var original = new BlizzardWeatherData(); UUID owner = UUID.randomUUID();
        level.setWeatherParameters(400, 600, false, false); original.acquire(level, owner);
        h.onEachTick(() -> { if (h.getTick() < 35) original.acquire(level, owner); });
        h.runAfterDelay(35, () -> {
            // The vanilla timers can diverge from game time (e.g. sleep/mods); still restore an owned storm.
            weather.setRainTime(119000); original.release(level, owner);
            h.assertTrue(!weather.isRaining() && weather.getClearWeatherTime() == 400, "Elapsed storm lost its original snapshot");
            var shared = BlizzardWeatherData.get(level); shared.acquire(level, owner);
            level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack(), "weather clear 100");
            shared.acquire(level, owner); shared.tick(level);
            h.assertTrue(!weather.isRaining() && !shared.isManaged(), "Caster overwrote manual clear");
            shared.acquire(level, UUID.randomUUID());
            h.assertTrue(!weather.isRaining(), "Second Bankai overwrote manual clear");
            shared.release(level, owner);
            h.succeed();
        });
    }

    @GameTest(template="empty")
    public static void releasedPassivesTrackIdentityAndFormWithoutItemMutation(GameTestHelper h) {
        var p=player(h); var z=mob(h,p,2); var d=data(p);
        d.getCharacter().setActiveForm("zanpakuto","shikai");
        h.assertTrue(ReleasePassives.resistance(p)==0.15F,"Shikai protection missing");
        d.getCharacter().setActiveForm("zanpakuto","bankai");
        var hit=new net.minecraftforge.event.entity.living.LivingHurtEvent(p,p.damageSources().mobAttack(z),10);
        com.bleachmod.server.events.CombatEvents.hurt(hit); h.assertTrue(hit.getAmount()==7,"Bankai protection missing");
        d.getCharacter().setActiveForm("zanpakuto","sealed"); h.assertTrue(ReleasePassives.resistance(p)==0,"Protection leaked to sealed form");
        d.getCharacter().bindZanpakuto("ryujin_jakka"); p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(ModItems.RYUJIN_JAKKA.get()));
        d.getCharacter().setActiveForm("zanpakuto","shikai"); ReleasePassives.flameHit(p,d,z); h.assertTrue(z.isOnFire(),"Shikai passive does not ignite");
        z.clearFire(); d.getCharacter().setActiveForm("zanpakuto","bankai"); ReleasePassives.flameHit(p,d,z);
        h.assertTrue(z.getRemainingFireTicks()==160 && !p.getMainHandItem().isEnchanted(),"Bankai fire missing or permanent enchantment added");
        z.clearFire(); d.getCharacter().setActiveForm("zanpakuto","sealed"); ReleasePassives.flameHit(p,d,z);
        h.assertTrue(!z.isOnFire(),"Fire passive leaked to sealed form"); z.discard(); h.succeed();
    }

    @GameTest(template="empty")
    public static void snowPersistsExpiresAndPreservesOtherBlocks(GameTestHelper h) {
        var level=h.getLevel(); var pos=h.absolutePos(new BlockPos(1,3,1));
        level.setBlock(pos.below(),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
        level.setBlock(pos,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
        UUID a=UUID.randomUUID(),b=UUID.randomUUID(); var snow=new GlacialSnowData();
        h.assertTrue(snow.place(level,pos,a,level.getGameTime()+60,3),"Snow patch missing");
        h.assertTrue(snow.place(level,pos,b,level.getGameTime()+60,1),"Overlapping snow lease missing");
        var recovered=GlacialSnowData.load(snow.save(new CompoundTag())); recovered.release(level,a);
        h.assertTrue(level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.SNOW),"Overlap removed another owner's snow");
        recovered.release(level,b); h.assertTrue(level.getBlockState(pos).isAir(),"Snow cleanup left residue");
        snow=new GlacialSnowData(); snow.place(level,pos,a,level.getGameTime(),2); snow.expire(level);
        h.assertTrue(level.getBlockState(pos).isAir(),"Expired snow remained");
        snow.place(level,pos,a,level.getGameTime()+60,2); level.setBlock(pos,net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK.defaultBlockState(),3);
        snow.release(level,a); h.assertTrue(level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK),"Cleanup overwrote a player block");
        h.assertTrue(!snow.place(level,pos,a,level.getGameTime()+60,1),"Snow replaced an occupied block"); h.succeed();
    }

    @GameTest(template="empty",batch="zone_radius",timeoutTicks=45)
    public static void bankaiZoneDamagesInteriorAndDistantTargetsButNotOutside(GameTestHelper h) {
        var p=player(h); p.setPos(p.getX(),h.getLevel().getMaxBuildHeight()-24,p.getZ()); var d=data(p);
        d.getSkills().get("zanpakuto").setLevel(2); d.getCharacter().unlockForm("shikai"); d.getCharacter().unlockForm("bankai");
        d.getCharacter().setMastery("zanpakuto","shikai",100); d.getCharacter().setActiveForm("zanpakuto","bankai");
        var center=mob(h,p,2); var far=mob(h,p,28); var outside=mob(h,p,32);
        TechniqueService.executeSlot(p,d,4); h.onEachTick(() -> HyorinmaruTechniqueService.tick(p,d));
        h.runAfterDelay(20,() -> { try {
            h.assertTrue(center.getHealth()<20 && far.getHealth()<20,"Zone failed to cover interior or 28-block target");
            h.assertTrue(outside.getHealth()==20,"Zone exceeded 30-block radius");
        } finally { HyorinmaruTechniqueService.cancel(p); center.discard(); far.discard(); outside.discard(); } h.succeed(); });
    }
}
