package it.hurts.sskirillss.raccompat.init;

import it.hurts.sskirillss.raccompat.RACCompat;
import it.hurts.sskirillss.raccompat.items.AcidWalkerItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RACCompat.MODID);

    public static final RegistryObject<Item> ACID_WALKER = ITEMS.register("acid_walker", AcidWalkerItem::new);

    public static void register() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}