package it.hurts.sskirillss.raccompat.init;

import it.hurts.sskirillss.raccompat.RACCompat;
import it.hurts.sskirillss.raccompat.entities.AcidCloudEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RACCompat.MODID);

    public static final RegistryObject<EntityType<AcidCloudEntity>> ACID_CLOUD = ENTITIES.register("acid_cloud", () ->
            EntityType.Builder.<AcidCloudEntity>of(AcidCloudEntity::new, MobCategory.MISC)
                    .sized(1.5F, 0.5F)
                    .build("acid_cloud")
    );

    public static void register() {
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}