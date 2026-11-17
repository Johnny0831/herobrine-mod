package com.hollowvigil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Presentes d'O Oco para jogadores honrados. */
public final class Gifts {
    private Gifts() {}

    public static void drop(ServerPlayer sp, ItemStack stack) {
        ItemEntity item = new ItemEntity(sp.level(), sp.getX(), sp.getY() + 0.6D, sp.getZ(), stack);
        item.setPickUpDelay(10);
        sp.level().addFreshEntity(item);
    }

    public static void give(ServerPlayer sp) {
        RandomSource r = sp.getRandom();
        double roll = r.nextDouble();
        ItemStack stack;
        if (roll < 0.35D) stack = new ItemStack(ModItems.SANITY_CHARM.get());
        else if (roll < 0.60D) stack = new ItemStack(ModItems.HOLLOW_HEART.get());
        else if (roll < 0.80D) stack = new ItemStack(ModItems.HOLLOW_LANTERN.get());
        else if (roll < 0.92D) stack = new ItemStack(Items.GOLDEN_APPLE, 2);
        else if (roll < 0.97D) stack = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
        else stack = new ItemStack(Items.TOTEM_OF_UNDYING);
        drop(sp, stack);

        PlayerStats.addSanity(sp, 15.0F);
        PlayerStats.sync(sp);
        Sfx.atPlayer(sp, ModSounds.CHIME.get(), 1.2F, 0.7F);
        Sfx.atPlayer(sp, ModSounds.ANGEL_CHOIR.get(), 0.7F, 1.0F);
        sp.sendSystemMessage(Component.literal("<").append(Component.translatable("entity.hollowvigil.hollow"))
                .append(Component.literal("> ")).append(ChatLines.line("gift", sp)).withStyle(ChatFormatting.GOLD));
    }
}
