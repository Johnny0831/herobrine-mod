package com.herobrinemod;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(HerobrineMod.MODID)
public class HerobrineMod {
    public static final String MODID = "herobrine";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HerobrineMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(bus);
        ModItems.ITEMS.register(bus);
        bus.addListener(this::onAttributes);
        bus.addListener(this::onCreativeTab);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HerobrineConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, HerobrineConfig.CLIENT_SPEC);

        HorrorNet.register();
        MinecraftForge.EVENT_BUS.register(HorrorDirector.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new HerobrineCommands());
    }

    private void onAttributes(EntityAttributeCreationEvent e) {
        e.put(ModEntities.HEROBRINE.get(), HerobrineEntity.createAttributes().build());
        e.put(ModEntities.HEROBRINE_CORRUPTED.get(), HerobrineCorruptedEntity.createAttributes().build());
    }

    private void onCreativeTab(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            e.accept(ModItems.HEROBRINE_SPAWN_EGG);
            e.accept(ModItems.CORRUPTED_SPAWN_EGG);
        }
    }
}
