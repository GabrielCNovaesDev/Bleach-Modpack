package com.bleachmod.client.events;

import com.bleachmod.Reference;
import com.bleachmod.client.model.ModModelLayers;
import com.bleachmod.entity.HollowEntity;
import com.bleachmod.entity.QuestNpcEntity;
import com.bleachmod.registry.ModEntities;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.bleachmod.client.model.HollowBossModel;
import com.bleachmod.client.model.ModModelLayers;
import com.bleachmod.entity.HollowBossEntity;

@Mod.EventBusSubscriber(
        modid = Reference.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ModEntityRenderers {

    @SubscribeEvent
    public static void registerRenderers(
            EntityRenderersEvent.RegisterRenderers event
    ) {
        event.registerEntityRenderer(
                ModEntities.HOLLOW.get(),
                HollowRenderer::new
        );

        event.registerEntityRenderer(
                ModEntities.HOLLOW_BOSS.get(),
                HollowBossRenderer::new
        );

        ModEntities.questNpcs().values().forEach(type ->
                event.registerEntityRenderer(type.get(), QuestNpcRenderer::new)
        );
    }

    public static class HollowRenderer
            extends MobRenderer<
            HollowEntity,
            HumanoidModel<HollowEntity>
            > {

        private static final ResourceLocation TEXTURE =
                Reference.id("textures/entity/hollow.png");

        public HollowRenderer(
                EntityRendererProvider.Context context
        ) {
            super(
                    context,
                    new HumanoidModel<>(
                            context.bakeLayer(ModelLayers.ZOMBIE)
                    ),
                    0.5F
            );
        }

        @Override
        public ResourceLocation getTextureLocation(
                HollowEntity entity
        ) {
            return TEXTURE;
        }
    }

    public static class HollowBossRenderer
            extends MobRenderer<
            HollowBossEntity,
            HollowBossModel
            > {

        private static final ResourceLocation TEXTURE =
                Reference.id("textures/entity/hollow_boss.png");

        public HollowBossRenderer(
                EntityRendererProvider.Context context
        ) {
            super(
                    context,
                    new HollowBossModel(
                            context.bakeLayer(
                                    ModModelLayers.HOLLOW_BOSS
                            )
                    ),
                    0.7F
            );
        }

        @Override
        public ResourceLocation getTextureLocation(
                HollowBossEntity entity
        ) {
            return TEXTURE;
        }
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(
            EntityRenderersEvent.RegisterLayerDefinitions event
    ) {
        event.registerLayerDefinition(
                ModModelLayers.HOLLOW_BOSS,
                HollowBossModel::createBodyLayer
        );
    }

    public static class QuestNpcRenderer extends MobRenderer<QuestNpcEntity, PlayerModel<QuestNpcEntity>> {
        public QuestNpcRenderer(EntityRendererProvider.Context context) {
            super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(QuestNpcEntity entity) {
            return Reference.id("textures/entity/" + entity.getNpcId() + ".png");
        }
    }
}
