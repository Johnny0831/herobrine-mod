package com.hollowvigil;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Marca um animal como possuído (sincronizada com o cliente, que desenha o brilho vermelho). */
public class PossessedEffect extends MobEffect {
    public PossessedEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);
    }
}
