package com.herobrinemod;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, HerobrineMod.MODID);

    public static final RegistryObject<Item> HEROBRINE_SPAWN_EGG = ITEMS.register("herobrine_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.HEROBRINE, 0x1B1B1B, 0xFFFFFF, new Item.Properties()));

    public static final RegistryObject<Item> CORRUPTED_SPAWN_EGG = ITEMS.register("herobrine_corrupted_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.HEROBRINE_CORRUPTED, 0x101010, 0xCC0000, new Item.Properties()));
}
