package it.hurts.sskirillss.raccompat.items;

import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.hurts.sskirillss.raccompat.misc.RACBackgrounds;
import it.hurts.sskirillss.raccompat.misc.RACLootCollections;
import it.hurts.sskirillss.relics.client.models.items.CurioModel;
import it.hurts.sskirillss.relics.client.models.items.SidedCurioModel;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.IRenderableCurio;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.WorldUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.List;

public class PolarBootItem extends RelicItem implements IRenderableCurio {
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("polarity")
                                .active(CastData.builder()
                                        .type(CastType.TOGGLEABLE)
                                        .build())
                                .icon((player, stack, ability) -> ability + (isAbilityTicking(stack, ability) ? "_blue" : "_red"))
                                .stat(StatData.builder("speed")
                                        .initialValue(0.9D, 0.95D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.015D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingData(100, 10, 200))
                .style(StyleData.builder()
                        .background(RACBackgrounds.MAGNETIC)
                        .build())
                .loot(LootData.builder()
                        .entry(RACLootCollections.MAGNETIC)
                        .build())
                .build();
    }

    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        if (stage == CastStage.TICK || !ability.equals("polarity"))
            return;

        Level level = player.level();
        RandomSource random = level.getRandom();

        boolean isEnabling = isAbilityTicking(stack, "polarity");

        level.playSound(null, player.blockPosition(), ACSoundRegistry.NEODYMIUM_PLACE.get(), SoundSource.MASTER, 1F, isEnabling ? 1.75F : 0.75F);

        for (int i = 0; i < 15; i++) {
            Vec3 center = player.position().add(MathUtils.randomFloat(random) * 0.25F, isEnabling ? player.getBbHeight() : 0, MathUtils.randomFloat(random) * 0.25F);

            level.addParticle(isEnabling ? ACParticleRegistry.SCARLET_SHIELD_LIGHTNING.get() : ACParticleRegistry.AZURE_SHIELD_LIGHTNING.get(), center.x(), center.y(), center.z(),
                    center.x() + MathUtils.randomFloat(random) * 3, center.y() + (player.getBbHeight() * (isEnabling ? -1 : 1) * 2), center.z() + MathUtils.randomFloat(random) * 3);
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player))
            return;

        Level level = player.level();

        RandomSource random = level.getRandom();

        int horizontalRadius = 3;
        int verticalRadius = 8;

        Vec3 motion = Vec3.ZERO;

        if (isAbilityTicking(stack, "polarity")) {
            if (player.tickCount % 20 == 0)
                addExperience(player, stack, 1);

            for (int x = -horizontalRadius; x <= horizontalRadius; x++) {
                for (int y = -verticalRadius; y <= verticalRadius / 2; y++) {
                    for (int z = -horizontalRadius; z <= horizontalRadius; z++) {
                        BlockPos pos = player.blockPosition().offset(x, y, z);

                        if (pos.getY() > player.getEyeY() || !level.getBlockState(pos).isSolid())
                            continue;

                        Vec3 repulsion = player.position().subtract(Vec3.atCenterOf(pos));

                        double distance = repulsion.lengthSqr();

                        double verticalDamping = (1D - (Math.abs(player.getY() - pos.getY()) / (verticalRadius * 2D))) * 0.2D;

                        if (distance > 0)
                            motion = motion.add(repulsion.multiply(verticalDamping, 1D, verticalDamping).normalize().scale(0.65D / distance * 0.025D));
                    }
                }
            }

            if (level.isClientSide())
                motion = handleAirController(player, motion);

            if (motion.lengthSqr() > 0) {
                double horizontalSpeed = getAbilityValue(stack, "polarity", "speed");

                player.setDeltaMovement(player.getDeltaMovement().multiply(horizontalSpeed, 1F, horizontalSpeed).add(motion));

                player.hasImpulse = true;
                player.fallDistance = 0F;

                if (level.isClientSide && player.tickCount % 2 == 0) {
                    double diff = Math.min(24, player.getY() - WorldUtils.getGroundHeight(level, player.position(), 24));

                    if (diff > 0) {
                        Vec3 start = player.position().add(player.getDeltaMovement()).add(MathUtils.randomFloat(random) * 0.25F, 0, MathUtils.randomFloat(random) * 0.25F);
                        Vec3 delta = player.position().add(new Vec3(MathUtils.randomFloat(random), -diff, MathUtils.randomFloat(random)));

                        level.addParticle(ACParticleRegistry.AZURE_SHIELD_LIGHTNING.get(), start.x(), start.y(), start.z(), delta.x, delta.y, delta.z);
                    }
                }
            }
        } else {
            if (!player.onGround() && !player.isFallFlying() && !player.getAbilities().flying && !player.isSwimming()) {
                player.setDeltaMovement(player.getDeltaMovement().add(0, Math.min(-0.02F, motion.y() * 1.075F), 0));

                player.hasImpulse = true;

                if (level.isClientSide && player.tickCount % 2 == 0) {
                    double diff = Math.min(24, player.getY() - WorldUtils.getGroundHeight(level, player.position(), 24));

                    if (diff > 0) {
                        Vec3 start = player.position().add(player.getDeltaMovement()).add(MathUtils.randomFloat(random) * 0.25F, 0, MathUtils.randomFloat(random) * 0.25F);
                        Vec3 delta = player.position().add(new Vec3(MathUtils.randomFloat(random), -diff, MathUtils.randomFloat(random)));

                        level.addParticle(ACParticleRegistry.SCARLET_SHIELD_LIGHTNING.get(), start.x(), start.y(), start.z(), delta.x, delta.y, delta.z);
                    }
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public Vec3 handleAirController(Player player, Vec3 motion) {
        if (!(player instanceof LocalPlayer localPlayer))
            return motion;

        if (localPlayer.isShiftKeyDown())
            motion = motion.add(0D, -0.05D, 0D);

        if (localPlayer.input.jumping && motion.y() > 0D)
            motion = motion.multiply(1D, 1.5D, 1D);

        return motion;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        Level level = entity.level();

        RandomSource random = level.getRandom();

        if (stack.getItem() instanceof IRelicItem relic) {
            Vec3 center = entity.position().add(MathUtils.randomFloat(random) * 0.1F, 0, MathUtils.randomFloat(random) * 0.1F);

            double distance = Math.min(5, WorldUtils.getGroundDistance(level, center, 5));

            if (relic.isAbilityTicking(stack, "polarity")) {
                if (entity.tickCount % 5 != 0)
                    level.addParticle(ACParticleRegistry.AZURE_SHIELD_LIGHTNING.get(), center.x(), center.y() + entity.getBbHeight(), center.z(), center.x(), center.y() - distance, center.z());

                if (distance <= 1) {
                    entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.05F, 0));

                    entity.hasImpulse = true;
                }
            } else {
                if (entity.tickCount % 5 != 0)
                    level.addParticle(ACParticleRegistry.SCARLET_SHIELD_LIGHTNING.get(), center.x(), center.y() + entity.getBbHeight(), center.z(), center.x(), center.y() - distance, center.z());
            }
        }

        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public CurioModel getModel(ItemStack stack) {
        return new SidedCurioModel(stack.getItem());
    }

    @Override
    public ResourceLocation getTexture(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());

        assert id != null;

        return new ResourceLocation(id.getNamespace(), "textures/models/items/" + id.getPath() + (isAbilityTicking(stack, "polarity") ? "_blue" : "_red") + ".png");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        CurioModel model = getModel(stack);

        if (!(model instanceof SidedCurioModel sidedModel))
            return;

        sidedModel.setSlot(slotContext.index());

        matrixStack.pushPose();

        LivingEntity entity = slotContext.entity();

        sidedModel.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        sidedModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ICurioRenderer.followBodyRotations(entity, sidedModel);

        VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(renderTypeBuffer, RenderType.armorCutoutNoCull(getTexture(stack)), false, stack.hasFoil());

        matrixStack.translate(0, 0, -0.025F);

        sidedModel.renderToBuffer(matrixStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);

        matrixStack.popPose();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LayerDefinition constructLayerDefinition() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0.0F);

        mesh.getRoot().addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 9).addBox(-2.9F, 5.5F, -2.5F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 1).addBox(-2.9F, 5.5F, -2.5F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.175F))
                .texOffs(18, 9).addBox(-2.9F, 9.5F, -4.5F, 6.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(25, 0).addBox(-1.9F, 6.5F, 3.5F, 4.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.5F));

        mesh.getRoot().addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 9).addBox(-2.9F, 5.5F, -2.5F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 1).addBox(-2.9F, 5.5F, -2.5F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.175F))
                .texOffs(18, 9).addBox(-2.9F, 9.5F, -4.5F, 6.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(25, 0).addBox(-1.9F, 6.5F, 3.5F, 4.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.5F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public List<String> bodyParts() {
        return Lists.newArrayList("right_leg", "left_leg");
    }
}