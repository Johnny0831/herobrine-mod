package com.hollowvigil;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, HollowVigilMod.MODID);

    public static final RegistryObject<MobEffect> POSSESSED = EFFECTS.register("possessed", PossessedEffect::new);
}
