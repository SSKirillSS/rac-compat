package it.hurts.sskirillss.raccompat;

import it.hurts.sskirillss.raccompat.init.EntityRegistry;
import it.hurts.sskirillss.raccompat.init.ItemRegistry;
import it.hurts.sskirillss.raccompat.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RACCompat.MODID)
public class RACCompat {
    public static final String MODID = "raccompat";

    public RACCompat() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        ItemRegistry.register();
        EntityRegistry.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.register();
    }
}