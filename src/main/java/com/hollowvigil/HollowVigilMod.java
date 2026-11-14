package com.hollowvigil;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(HollowVigilMod.MODID)
public class HollowVigilMod {
    public static final String MODID = "hollowvigil";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HollowVigilMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(bus);
        ModItems.ITEMS.register(bus);
        ModItems.TABS.register(bus);
        ModEffects.EFFECTS.register(bus);
        bus.addListener(this::onAttributes);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HollowConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, HollowConfig.CLIENT_SPEC);

        HorrorNet.register();
        MinecraftForge.EVENT_BUS.register(HorrorDirector.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new HonorEvents());
        MinecraftForge.EVENT_BUS.register(new PlayerDataEvents());
        MinecraftForge.EVENT_BUS.register(new HollowCommands());
        LOGGER.info("Hollow Vigil carregado.");
    }

    private void onAttributes(EntityAttributeCreationEvent e) {
        e.put(ModEntities.HOLLOW.get(), HollowEntity.createAttributes().build());
        e.put(ModEntities.HOLLOW_REAPER.get(), HollowReaperEntity.createAttributes().build());
        e.put(ModEntities.HALLUCINATION.get(), HallucinationEntity.createAttributes().build());
    }
}
