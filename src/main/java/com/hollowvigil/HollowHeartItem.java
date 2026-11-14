package com.hollowvigil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Coração Oco: ao comer, restaura a sanidade por completo. */
public class HollowHeartItem extends Item {
    public HollowHeartItem(Properties props) {
        super(props);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof ServerPlayer sp) {
            PlayerStats.setSanity(sp, 100.0F);
            PlayerStats.sync(sp);
            Sfx.atPlayer(sp, "block.amethyst_block.chime", 1.0F, 0.6F);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hollowvigil.hollow_heart.desc").withStyle(ChatFormatting.GRAY));
    }
}
