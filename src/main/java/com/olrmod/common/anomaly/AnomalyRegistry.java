package com.olrmod.common.anomaly;

import java.util.HashMap;
import java.util.Map;

import com.olrmod.StalkerMod;
import com.olrmod.common.effects.DamageSourceRegistry;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;

/**
 * Реестр эффектов аномалий
 * 
 * // TODO: Этот класс больше не управляет эффектами как таковыми (логика в AnomalyManager).
 * // Возможно, его стоит переименовать или удалить, оставив только регистрацию базовых свойств, если нужно.
 */
public class AnomalyRegistry {
    
    private static final Map<AnomalyType, AnomalyEffect> REGISTRY = new HashMap<>();
    
    /**
     * Инициализация реестра эффектов аномалий
     */
    public static void init() {
        StalkerMod.logger.info("Инициализация реестра эффектов аномалий");
        
        // Регистрация базовых данных/эффектов для каждого типа аномалий
        // Используем специфичные источники урона из DamageSourceRegistry
        // Флаг isInstantKill убран для MOSQUITO, т.к. убийство обрабатывается в AnomalyManager
        register(AnomalyType.MOSQUITO, createEffect("mosquito", DamageSourceRegistry.GRAVITY_DAMAGE, 20.0f, SoundEvents.ENTITY_ENDERMEN_AMBIENT, false));
        register(AnomalyType.MINCER, createEffect("mincer", DamageSourceRegistry.ELECTRIC_DAMAGE, 10.0f, SoundEvents.ENTITY_GENERIC_HURT, true)); // Оставим isInstantKill для примера, хотя логика урона в AnomalyManager
        register(AnomalyType.GALANTINE, createEffect("galantine", DamageSourceRegistry.CHEMICAL_DAMAGE, 15.0f, SoundEvents.ENTITY_SLIME_SQUISH, true)); // Оставим isInstantKill для примера
        register(AnomalyType.ELECTRO, createEffect("electro", DamageSourceRegistry.ELECTRIC_DAMAGE, 8.0f, SoundEvents.ENTITY_CREEPER_HURT, false));
        register(AnomalyType.CHEMICAL, createEffect("chemical", DamageSourceRegistry.CHEMICAL_DAMAGE, 6.0f, SoundEvents.ENTITY_GENERIC_SPLASH, false));
        register(AnomalyType.GRAVITATIONAL, createEffect("gravitational", DamageSourceRegistry.GRAVITY_DAMAGE, 5.0f, SoundEvents.ENTITY_GENERIC_BIG_FALL, false));
        register(AnomalyType.THERMAL, createEffect("thermal", DamageSource.ON_FIRE, 4.0f, SoundEvents.BLOCK_FIRE_AMBIENT, false));
        register(AnomalyType.BURNING_FUZZ, createEffect("burning_fuzz", DamageSource.ON_FIRE, 7.0f, SoundEvents.BLOCK_FIRE_EXTINGUISH, false));
        // Используем ELECTRIC для RUSTY_HAIR, как наиболее подходящий из доступных специфичных
        register(AnomalyType.RUSTY_HAIR, createEffect("rusty_hair", DamageSourceRegistry.ELECTRIC_DAMAGE, 3.0f, SoundEvents.ENTITY_SPIDER_AMBIENT, false)); 
        register(AnomalyType.BLACK_NEEDLES, createEffect("black_needles", DamageSourceRegistry.PSY_DAMAGE, 10.0f, SoundEvents.ENTITY_ENDERMEN_STARE, false));
    }
    
    /**
     * Регистрирует эффект аномалии для указанного типа
     * 
     * @param type Тип аномалии
     * @param effect Эффект аномалии
     */
    public static void register(AnomalyType type, AnomalyEffect effect) {
        REGISTRY.put(type, effect);
        StalkerMod.logger.info("Зарегистрирован эффект аномалии для типа: " + type.name());
    }
    
    /**
     * Получает эффект аномалии по типу
     * 
     * @param type Тип аномалии
     * @return Эффект аномалии или null если не найден
     */
    public static AnomalyEffect getEffect(AnomalyType type) {
        return REGISTRY.getOrDefault(type, null);
    }
    
    /**
     * Создает эффект аномалии с указанными параметрами
     * 
     * @param name Название эффекта
     * @param damageSource Источник урона
     * @param damageAmount Количество урона
     * @param hitSound Звук при попадании
     * @param isInstantKill Мгновенно убивает ли сущность
     * @return Созданный эффект аномалии
     */
    public static AnomalyEffect createEffect(String name, DamageSource damageSource, 
                                          float damageAmount, SoundEvent hitSound, 
                                          boolean isInstantKill) {
        return new AnomalyEffect(name, damageSource, damageAmount, hitSound, isInstantKill);
    }
} 