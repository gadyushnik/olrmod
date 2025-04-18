package com.olrmod.common.effects;

import net.minecraft.potion.Potion;

/**
 * Утилитарный класс для хранения ссылок на эффекты аномалий.
 * В серверной версии мы используем в основном ванильные эффекты зелий.
 */
public class AnomalyEffects {
    
    // Эффекты используются только для внутренних проверок, не создаем новые, а переиспользуем существующие
    public static final Potion RADIATION_DAMAGE_WEAK = net.minecraft.init.MobEffects.HUNGER;
    public static final Potion RADIATION_DAMAGE_NORMAL = net.minecraft.init.MobEffects.MINING_FATIGUE;
    public static final Potion RADIATION_DAMAGE_STRONG = net.minecraft.init.MobEffects.WITHER;
    
    public static final Potion CHEMICAL_BURN_NORMAL = net.minecraft.init.MobEffects.POISON;
    public static final Potion ELECTRIC_SHOCK = net.minecraft.init.MobEffects.WEAKNESS;
    public static final Potion GRAVITY_DAMAGE = net.minecraft.init.MobEffects.SLOWNESS;
    
    /**
     * Инициализация эффектов
     */
    public static void init() {
        // Нет необходимости в дополнительной инициализации, так как используем ванильные эффекты
    }
} 