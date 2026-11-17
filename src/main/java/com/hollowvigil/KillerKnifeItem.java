package com.hollowvigil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Faca do Assassino: dano alto e rápida. As entidades só sofrem dano de quem perdeu toda a honra. */
public class KillerKnifeItem extends SwordItem {
    public KillerKnifeItem(Properties props) {
        super(Tiers.NETHERITE, 6, -1.0F, props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hollowvigil.killer_knife.desc").withStyle(ChatFormatting.DARK_RED));
    }
}
