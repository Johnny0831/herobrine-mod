package com.herobrinemod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HerobrineMod.MODID);

    public static final RegistryObject<EntityType<HerobrineEntity>> HEROBRINE = ENTITIES.register("herobrine",
            () -> EntityType.Builder.of(HerobrineEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build(new ResourceLocation(HerobrineMod.MODID, "herobrine").toString()));

    public static final RegistryObject<EntityType<HerobrineCorruptedEntity>> HEROBRINE_CORRUPTED =
            ENTITIES.register("herobrine_corrupted",
                    () -> EntityType.Builder.of(HerobrineCorruptedEntity::new, MobCategory.MONSTER)
                            .sized(0.8F, 3.2F)
                            .clientTrackingRange(12)
                            .fireImmune()
                            .build(new ResourceLocation(HerobrineMod.MODID, "herobrine_corrupted").toString()));
}
