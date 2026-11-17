package com.hollowvigil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** GERADO: eventos de som (arquivos .ogg em assets/hollowvigil/sounds, descritos em sounds.json). */
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HollowVigilMod.MODID);

    private static RegistryObject<SoundEvent> reg(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(HollowVigilMod.MODID, name)));
    }

    public static final RegistryObject<SoundEvent> ANGEL_CHOIR = reg("angel_choir");
    public static final RegistryObject<SoundEvent> BREATH = reg("breath");
    public static final RegistryObject<SoundEvent> CHIME = reg("chime");
    public static final RegistryObject<SoundEvent> CREAK = reg("creak");
    public static final RegistryObject<SoundEvent> DRONE = reg("drone");
    public static final RegistryObject<SoundEvent> HEARTBEAT = reg("heartbeat");
    public static final RegistryObject<SoundEvent> HEARTBEAT_FAST = reg("heartbeat_fast");
    public static final RegistryObject<SoundEvent> HOLLOW_IDLE = reg("hollow_idle");
    public static final RegistryObject<SoundEvent> JUMPSCARE = reg("jumpscare");
    public static final RegistryObject<SoundEvent> KNOCK = reg("knock");
    public static final RegistryObject<SoundEvent> RAGE = reg("rage");
    public static final RegistryObject<SoundEvent> REAPER_ROAR = reg("reaper_roar");
    public static final RegistryObject<SoundEvent> SCRAPE = reg("scrape");
    public static final RegistryObject<SoundEvent> SCREAM_FAR = reg("scream_far");
    public static final RegistryObject<SoundEvent> STATIC_BURST = reg("static_burst");
    public static final RegistryObject<SoundEvent> STINGER = reg("stinger");
    public static final RegistryObject<SoundEvent> TOLL = reg("toll");
    public static final RegistryObject<SoundEvent> WHISPER = reg("whisper");
}
