package it.hurts.sskirillss.raccompat.init;

import it.hurts.sskirillss.raccompat.RACCompat;
import it.hurts.sskirillss.raccompat.items.AcidWalkerItem;
import it.hurts.sskirillss.relics.client.renderer.entities.NullRenderer;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.NBTUtils;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RACCompat.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RemoteRegistry {
    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event) {
        ItemProperties.register(ItemRegistry.ACID_WALKER.get(), new ResourceLocation(RACCompat.MODID, "erosion"),
                (stack, world, entity, id) -> NBTUtils.getInt(stack, AcidWalkerItem.TAG_DURATION, 0) >= ((IRelicItem) stack.getItem()).getAbilityValue(stack, "walking", "duration") ? 1 : 0);
        ItemProperties.register(ItemRegistry.POLAR_BOOT.get(), new ResourceLocation(RACCompat.MODID, "blue"),
                (stack, world, entity, id) -> ((IRelicItem) stack.getItem()).isAbilityTicking(stack, "polarity") ? 1 : 0);
    }

    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.ACID_CLOUD.get(), NullRenderer::new);
    }
}