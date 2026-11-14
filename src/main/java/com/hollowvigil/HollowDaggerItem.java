package com.hollowvigil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Adaga Oca: dano extra contra O Oco, Ceifador e alucinações; golpes nelas devolvem sanidade. */
public class HollowDaggerItem extends SwordItem {
    public HollowDaggerItem(Properties props) {
        super(Tiers.DIAMOND, 2, -1.8F, props);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target instanceof HollowEntity || target instanceof HollowReaperEntity || target instanceof HallucinationEntity) {
            target.invulnerableTime = 0;
            target.hurt(target.level().damageSources().magic(), 6.0F);
            if (attacker instanceof ServerPlayer sp) PlayerStats.addSanity(sp, 3.0F);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hollowvigil.hollow_dagger.desc").withStyle(ChatFormatting.GRAY));
    }
}
