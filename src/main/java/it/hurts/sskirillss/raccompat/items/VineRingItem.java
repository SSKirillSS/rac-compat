package it.hurts.sskirillss.raccompat.items;

import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import it.hurts.sskirillss.raccompat.misc.RACBackgrounds;
import it.hurts.sskirillss.raccompat.misc.RACLootCollections;
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
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import it.hurts.sskirillss.relics.utils.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.awt.*;

public class VineRingItem extends RelicItem {
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("vine")
                                .active(CastData.builder()
                                        .type(CastType.INSTANTANEOUS)
                                        .castPredicate("ceil", (player, stack) -> {
                                            int maxDistance = (int) getAbilityValue(stack, "vine", "length");

                                            return WorldUtils.getCeilDistance(player.level(), player.position(), maxDistance) <= maxDistance;
                                        })
                                        .build())
                                .stat(StatData.builder("length")
                                        .initialValue(10D, 15D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, 0.175D)
                                        .formatValue(value -> (int) (MathUtils.round(value, 0)))
                                        .build())
                                .stat(StatData.builder("cooldown")
                                        .initialValue(20D, 30D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, -0.1)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingData(100, 10, 200))
                .style(StyleData.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xFF274705)
                                .borderBottom(0xFF29190f)
                                .textured(true)
                                .build())
                        .background(RACBackgrounds.PRIMORDIAL)
                        .build())
                .loot(LootData.builder()
                        .entry(RACLootCollections.PRIMORDIAL)
                        .build())
                .build();
    }

    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        if (!ability.equals("vine"))
            return;

        Level level = player.level();
        RandomSource random = level.getRandom();

        double height = WorldUtils.getCeilHeight(level, player.position(), (int) getAbilityValue(stack, "vine", "length"));

        spreadExperience(player, stack, (int) height);

        int offset = -1;

        while (player.getY() + offset < height) {
            BlockPos pos = player.blockPosition().above(offset++);

            if (!level.getBlockState(pos).canBeReplaced())
                continue;

            level.setBlockAndUpdate(pos, ACBlockRegistry.ARCHAIC_VINE.get().defaultBlockState());

            for (int i = 0; i < 5; i++) {
                level.addDestroyBlockEffect(pos, ACBlockRegistry.ARCHAIC_VINE.get().defaultBlockState());

                level.addParticle(ParticleUtils.constructSimpleSpark(new Color(140 + random.nextInt(50), 255 - random.nextInt(50), 0), 0.1F + random.nextFloat() * 0.1F, 60 + random.nextInt(60), 0.98F),
                        pos.getX() + random.nextFloat(), pos.getY() + random.nextFloat(), pos.getZ() + random.nextFloat(),
                        MathUtils.randomFloat(random) * 0.01F, 0, MathUtils.randomFloat(random) * 0.01F);
            }

            level.playSound(null, pos, SoundEvents.VINE_PLACE, SoundSource.MASTER, 1F, 1F + random.nextFloat());
        }

        player.setDeltaMovement(0F, Math.log(player.position().distanceTo(player.position().with(Direction.Axis.Y, height))) * 0.4F, 0F);

        addAbilityCooldown(stack, "vine", (int) (getAbilityValue(stack, "vine", "cooldown") * 20));
    }
}