package com.hollowvigil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HollowVigilMod.MODID);

    public static final RegistryObject<EntityType<HollowEntity>> HOLLOW = ENTITIES.register("hollow",
            () -> EntityType.Builder.of(HollowEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F).clientTrackingRange(10)
                    .build(new ResourceLocation(HollowVigilMod.MODID, "hollow").toString()));

    public static final RegistryObject<EntityType<HollowReaperEntity>> HOLLOW_REAPER = ENTITIES.register("hollow_reaper",
            () -> EntityType.Builder.of(HollowReaperEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 3.2F).clientTrackingRange(12).fireImmune()
                    .build(new ResourceLocation(HollowVigilMod.MODID, "hollow_reaper").toString()));

    public static final RegistryObject<EntityType<HallucinationEntity>> HALLUCINATION = ENTITIES.register("hallucination",
            () -> EntityType.Builder.of(HallucinationEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).fireImmune()
                    .build(new ResourceLocation(HollowVigilMod.MODID, "hallucination").toString()));
}
