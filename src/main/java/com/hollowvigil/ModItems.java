package com.hollowvigil;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HollowVigilMod.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HollowVigilMod.MODID);

    public static final RegistryObject<Item> HOLLOW_EGG = ITEMS.register("hollow_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.HOLLOW, 0x15151A, 0xFFFFFF, new Item.Properties()));
    public static final RegistryObject<Item> REAPER_EGG = ITEMS.register("hollow_reaper_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.HOLLOW_REAPER, 0x0B0B0D, 0xB00000, new Item.Properties()));
    public static final RegistryObject<Item> HALLUCINATION_EGG = ITEMS.register("hallucination_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.HALLUCINATION, 0x202028, 0x8080A8, new Item.Properties()));

    public static final RegistryObject<Item> SANITY_CHARM = ITEMS.register("sanity_charm",
            () -> new SanityCharmItem(new Item.Properties().durability(12).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> HOLLOW_HEART = ITEMS.register("hollow_heart",
            () -> new HollowHeartItem(new Item.Properties().rarity(Rarity.EPIC).fireResistant()
                    .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.8F).alwaysEat()
                            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 200, 1), 1.0F)
                            .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 2400, 1), 1.0F)
                            .build())));

    public static final RegistryObject<Item> HOLLOW_DAGGER = ITEMS.register("hollow_dagger",
            () -> new HollowDaggerItem(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hollowvigil"))
                    .icon(() -> new ItemStack(SANITY_CHARM.get()))
                    .displayItems((params, out) -> {
                        out.accept(SANITY_CHARM.get());
                        out.accept(HOLLOW_HEART.get());
                        out.accept(HOLLOW_DAGGER.get());
                        out.accept(HOLLOW_EGG.get());
                        out.accept(REAPER_EGG.get());
                        out.accept(HALLUCINATION_EGG.get());
                    })
                    .build());
}
