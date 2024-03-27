package it.hurts.sskirillss.raccompat.items;

import com.github.alexmodguy.alexscaves.server.entity.ACEntityRegistry;
import com.github.alexmodguy.alexscaves.server.entity.item.DesolateDaggerEntity;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.hurts.sskirillss.raccompat.init.ItemRegistry;
import it.hurts.sskirillss.raccompat.misc.RACBackgrounds;
import it.hurts.sskirillss.raccompat.misc.RACLootCollections;
import it.hurts.sskirillss.raccompat.network.NetworkHandler;
import it.hurts.sskirillss.raccompat.network.packets.DesolateDaggerRenderStackPacket;
import it.hurts.sskirillss.relics.client.models.items.CurioModel;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.IRenderableCurio;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastPredicate;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.List;

public class DesolatePendantItem extends RelicItem implements IRenderableCurio {
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("desolate")
                                .stat(StatData.builder("chance")
                                        .initialValue(0.05D, 0.15D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .stat(StatData.builder("damage")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .build())
                        .ability(AbilityData.builder("devastate")
                                .requiredLevel(5)
                                .active(CastType.INSTANTANEOUS, CastPredicate.builder()
                                        .predicate("target", (player, stack) -> EntityUtils.rayTraceEntity(player, (entity) -> !entity.isSpectator() && entity instanceof Attackable, 16) != null)
                                        .predicate("weapon", (player, stack) -> player.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE))
                                        .build())
                                .stat(StatData.builder("count")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(UpgradeOperation.ADD, 1D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatData.builder("cooldown")
                                        .initialValue(30D, 40D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, -0.025)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingData(100, 10, 200))
                .style(StyleData.builder()
                        .background(RACBackgrounds.FORLORN)
                        .build())
                .loot(LootData.builder()
                        .entry(RACLootCollections.FORLORN)
                        .build())
                .build();
    }

    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        if (!ability.equals("devastate"))
            return;

        Level level = player.level();

        if (level.isClientSide())
            return;

        double damage = 1D;

        for (AttributeModifier modifier : player.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE))
            damage += modifier.getAmount();

        EntityHitResult result = EntityUtils.rayTraceEntity(player, (entity) -> !entity.isSpectator() && entity instanceof Attackable, 16);

        if (result == null)
            return;

        RandomSource random = player.getRandom();

        for (int i = 0; i < getAbilityValue(stack, "devastate", "count"); i++) {
            DesolateDaggerEntity dagger = new DesolateDaggerEntity(ACEntityRegistry.DESOLATE_DAGGER.get(), level);

            dagger.getPersistentData().putDouble("raccompat_damage", damage * getAbilityValue(stack, "desolate", "damage"));

            dagger.daggerRenderStack = player.getMainHandItem();
            dagger.setTargetId(result.getEntity().getId());
            dagger.setStab(-random.nextFloat());
            dagger.setPlayerId(player.getId());
            dagger.orbitFor = 20 + (i * 5);
            dagger.copyPosition(player);
            dagger.setItemStack(stack);

            level.addFreshEntity(dagger);

            if (!level.isClientSide())
                NetworkHandler.sendToClients(PacketDistributor.TRACKING_ENTITY.with(() -> dagger), new DesolateDaggerRenderStackPacket(dagger.daggerRenderStack, dagger.getId()));
        }

        addAbilityCooldown(stack, ability, (int) (getAbilityValue(stack, "devastate", "cooldown") * 20));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        CurioModel model = getModel(stack);

        matrixStack.pushPose();

        LivingEntity entity = slotContext.entity();

        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ICurioRenderer.translateIfSneaking(matrixStack, entity);
        ICurioRenderer.rotateIfSneaking(matrixStack, entity);

        ICurioRenderer.followBodyRotations(entity, model);

        VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(renderTypeBuffer, RenderType.armorCutoutNoCull(getTexture(stack)), false, stack.hasFoil());

        matrixStack.scale(0.5F, 0.5F, 0.5F);

        model.renderToBuffer(matrixStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        matrixStack.scale(2F, 2F, 2F);

        matrixStack.popPose();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LayerDefinition constructLayerDefinition() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0.0F);

        mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(45, 36).addBox(-4.0F, 6.25F, -4.7F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(27, 36).addBox(-3.0F, 5.25F, -5.2F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(16, 35).addBox(-2.0F, 4.25F, -4.7F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.0F, 0.0F, -3.65F, 16.0F, 7.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 1.5F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public List<String> headParts() {
        return Lists.newArrayList("body");
    }


    @Mod.EventBusSubscriber
    public static class DesolatePendantEvents {
        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            if (!(event.getSource().getEntity() instanceof Player player) || event.getAmount() < 1F
                    || !player.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE))
                return;

            RandomSource random = player.getRandom();

            ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.DESOLATE_PENDANT.get());

            if (stack.isEmpty() || !(stack.getItem() instanceof IRelicItem relic) || random.nextFloat() > relic.getAbilityValue(stack, "desolate", "chance"))
                return;

            Level level = player.level();

            DesolateDaggerEntity dagger = new DesolateDaggerEntity(ACEntityRegistry.DESOLATE_DAGGER.get(), level);

            dagger.getPersistentData().putDouble("raccompat_damage", event.getAmount() * relic.getAbilityValue(stack, "desolate", "damage"));
            dagger.daggerRenderStack = player.getMainHandItem();
            dagger.orbitFor = 20 + random.nextInt(40);
            dagger.setTargetId(event.getEntity().getId());
            dagger.setPlayerId(player.getId());
            dagger.copyPosition(player);
            dagger.setItemStack(stack);

            level.addFreshEntity(dagger);

            if (!level.isClientSide())
                NetworkHandler.sendToClients(PacketDistributor.TRACKING_ENTITY.with(() -> dagger), new DesolateDaggerRenderStackPacket(dagger.daggerRenderStack, dagger.getId()));
        }
    }
}