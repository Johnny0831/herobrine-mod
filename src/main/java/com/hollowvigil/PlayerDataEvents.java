package com.hollowvigil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerDataEvents {
    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        PlayerStats.sync(sp);
        if (HollowConfig.WELCOME_MESSAGE.get() && !PlayerStats.introDone(sp)) {
            sp.sendSystemMessage(Component.translatable("hollow.welcome").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
        HorrorDirector.INSTANCE.scheduleIntro(sp);
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone e) {
        CompoundTag old = e.getOriginal().getPersistentData();
        if (old.contains(Player.PERSISTED_NBT_TAG)) {
            e.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG, old.getCompound(Player.PERSISTED_NBT_TAG).copy());
        }
        if (e.isWasDeath()) {
            // assassinos e anjos não têm medo: a sanidade não cai por morrer (antes caía a 70 e voltava a 100)
            boolean fearless = PlayerStats.isKiller(e.getEntity()) || PlayerStats.isAngel(e.getEntity());
            PlayerStats.setSanity(e.getEntity(), fearless ? 100.0F : 70.0F);
        }
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) Modes.onRespawn(sp);
    }
}
