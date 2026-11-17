package com.hollowvigil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Lanterna Oca: segurada na mão, conta como fonte de luz forte para a sanidade (o efeito está no diretor). */
public class HollowLanternItem extends Item {
    public HollowLanternItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hollowvigil.hollow_lantern.desc").withStyle(ChatFormatting.GRAY));
    }
}
