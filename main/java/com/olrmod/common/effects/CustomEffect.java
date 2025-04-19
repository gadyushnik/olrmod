package com.olrmod.common.effects;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;

/**
 * Класс для управления кастомными эффектами с тремя стадиями.
 */
public class CustomEffect {
    private final int weakThreshold;
    private final int strongThreshold;
    private final PotionEffect weakEffect;
    private final PotionEffect mediumEffect;
    private final PotionEffect strongEffect;
    private int accumulatedValue;

    public CustomEffect(int weakThreshold, int strongThreshold, PotionEffect weakEffect, PotionEffect mediumEffect, PotionEffect strongEffect) {
        this.weakThreshold = weakThreshold;
        this.strongThreshold = strongThreshold;
        this.weakEffect = weakEffect;
        this.mediumEffect = mediumEffect;
        this.strongEffect = strongEffect;
        this.accumulatedValue = 0;
    }

    /**
     * Обновляет эффект на основе накопленного значения.
     */
    public void updateEffect(EntityLivingBase entity) {
        accumulatedValue++;
        if (accumulatedValue >= strongThreshold) {
            entity.addPotionEffect(strongEffect);
        } else if (accumulatedValue >= weakThreshold) {
            entity.addPotionEffect(mediumEffect);
        } else {
            entity.addPotionEffect(weakEffect);
        }
    }

    /**
     * Сбрасывает накопленное значение.
     */
    public void reset() {
        accumulatedValue = 0;
    }
} 