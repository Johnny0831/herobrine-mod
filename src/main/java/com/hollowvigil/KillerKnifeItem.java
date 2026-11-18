package com.hollowvigil;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Faca do Assassino: durabilidade INFINITA e fica mais forte a cada criatura caçada (+0,5 de dano por nível).
 * As entidades só sofrem dano de quem perdeu toda a honra.
 */
public class KillerKnifeItem extends SwordItem {
    private static final UUID BONUS_UUID = UUID.fromString("5b1c3f4e-8a52-4c3e-9f6d-2a7e4d9b1c30");
    public static final double DAMAGE_PER_LEVEL = 0.5D;

    public KillerKnifeItem(Properties props) {
        super(Tiers.NETHERITE, 6, -1.0F, props);
    }

    public static int getLevel(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt("KnifeLevel") : 0;
    }

    public static void setLevel(ItemStack stack, int level) {
        stack.getOrCreateTag().putInt("KnifeLevel", Math.max(0, level));
    }

    // ---- sem desgaste algum
    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        return true;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getLevel(stack) >= 5;
    }

    // ---- dano cresce com o nível
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> base = super.getAttributeModifiers(slot, stack);
        int level = getLevel(stack);
        if (slot != EquipmentSlot.MAINHAND || level <= 0) return base;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> b = ImmutableMultimap.builder();
        b.putAll(base);
        b.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BONUS_UUID, "Knife level bonus",
                level * DAMAGE_PER_LEVEL, AttributeModifier.Operation.ADDITION));
        return b.build();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hollowvigil.killer_knife.desc").withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("item.hollowvigil.killer_knife.level", getLevel(stack),
                String.format("%.1f", getLevel(stack) * DAMAGE_PER_LEVEL)).withStyle(ChatFormatting.GOLD));
    }
}
