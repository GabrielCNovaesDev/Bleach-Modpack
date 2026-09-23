package com.bleachmod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;

public class HollowEntity extends Monster {

    public HollowEntity(
            EntityType<? extends Monster> entityType,
            Level level
    ) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {

        this.goalSelector.addGoal(
                0,
                new FloatGoal(this)
        );

        this.goalSelector.addGoal(
                1,
                new MeleeAttackGoal(
                        this,
                        1.2D,
                        true
                )
        );

        this.goalSelector.addGoal(
                2,
                new WaterAvoidingRandomStrollGoal(
                        this,
                        1.0D
                )
        );

        this.goalSelector.addGoal(
                3,
                new LookAtPlayerGoal(
                        this,
                        Player.class,
                        8.0F
                )
        );

        this.goalSelector.addGoal(
                4,
                new RandomLookAroundGoal(this)
        );

        this.targetSelector.addGoal(
                1,
                new HurtByTargetGoal(this)
        );

        this.targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this,
                        Player.class,
                        true
                )
        );

        this.targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this,
                        Villager.class,
                        true)
        );

        this.targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this,
                        IronGolem.class,
                        true)
        );
    }

    public static boolean canSpawn(
            EntityType<HollowEntity> entityType,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random
    ) {
        return Mob.checkMobSpawnRules(
                entityType,
                level,
                spawnType,
                pos,
                random
        );
    }

    @Override
    public int getMaxSpawnClusterSize() {
        if (this.level().isDay()) {
            return 3;
        }

        return 6;
    }

    public static AttributeSupplier.Builder createAttributes() {

        return Monster.createMonsterAttributes()

                // Vida
                .add(
                        Attributes.MAX_HEALTH,
                        40.0D
                )

                // Velocidade
                .add(
                        Attributes.MOVEMENT_SPEED,
                        0.30D
                )

                // Dano
                .add(
                        Attributes.ATTACK_DAMAGE,
                        6.0D
                )

                // Distância de detecção
                .add(
                        Attributes.FOLLOW_RANGE,
                        32.0D
                )

                // Resistência a knockback
                .add(
                        Attributes.KNOCKBACK_RESISTANCE,
                        0.15D
                );
    }
}