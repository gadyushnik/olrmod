package com.olrmod.common.effects;

import net.minecraft.util.DamageSource;

/**
 * Регистрирует источники урона для мода
 */
public class DamageSourceRegistry {
    
    // Используем ванильные источники урона где это возможно
    public static final DamageSource CHEMICAL_DAMAGE = new DamageSource("chemical").setDamageBypassesArmor();
    public static final DamageSource ELECTRIC_DAMAGE = new DamageSource("electric").setDamageBypassesArmor();
    
    // Для гравитационных аномалий используем ванильный void damage
    public static final DamageSource GRAVITY_DAMAGE = DamageSource.OUT_OF_WORLD;
    
    // Для псионического и радиационного урона используем модифицированные источники
    public static final DamageSource PSY_DAMAGE = new DamageSource("psy").setDamageBypassesArmor().setDamageIsAbsolute();
    public static final DamageSource RADIATION_DAMAGE = new DamageSource("radiation").setDamageBypassesArmor();
    
    /**
     * Инициализация источников урона
     */
    public static void init() {
        // Нет необходимости в дополнительной инициализации
    }
} 