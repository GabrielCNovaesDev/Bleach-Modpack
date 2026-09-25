package com.bleachmod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class HollowBossEntity extends HollowEntity {

    public HollowBossEntity(
            EntityType<? extends HollowBossEntity> entityType,
            Level level
    ) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {

        return HollowEntity.createAttributes()

                // Vida do Boss: 50 HP = 25 corações
                .add(
                        Attributes.MAX_HEALTH,
                        50.0D
                )

                // Dano maior que o Hollow comum
                .add(
                        Attributes.ATTACK_DAMAGE,
                        8.0D
                )

                // Mais resistência a knockback
                .add(
                        Attributes.KNOCKBACK_RESISTANCE,
                        0.50D
                );
    }
}