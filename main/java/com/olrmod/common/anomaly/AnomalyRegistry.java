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
        
        // Регистрация эффектов для каждого типа аномалий с использованием конфигурации
        for (AnomalyType type : AnomalyType.values()) {
            AnomalyEffectConfig config = StalkerMod.ANOMALY_EFFECTS_CONFIG.get(type);
            if (config != null) {
                register(type, createEffectFromConfig(config));
            }
        }
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
        return new AnomalyEffect(name, damageSource, damageAmount, hitSound, isInstantKill, "");
    }

    private static AnomalyEffect createEffectFromConfig(AnomalyEffectConfig config) {
        return new AnomalyEffect("custom", DamageSource.GENERIC, config.damageAmount, null, false, "");
    }
} 