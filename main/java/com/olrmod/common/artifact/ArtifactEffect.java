package com.olrmod.common.artifact;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Класс для представления эффекта артефакта
 */
public class ArtifactEffect {
    
    private final String effectName;
    private final int value;
    private final boolean isPositive;
    
    /**
     * Конструктор эффекта артефакта
     * 
     * @param effectName Название эффекта
     * @param value Значение эффекта
     * @param isPositive Является ли эффект положительным
     */
    public ArtifactEffect(String effectName, int value, boolean isPositive) {
        this.effectName = effectName;
        this.value = value;
        this.isPositive = isPositive;
    }
    
    /**
     * Создает положительный эффект артефакта
     * 
     * @param effectName Название эффекта
     * @param value Значение эффекта
     * @return Созданный положительный эффект
     */
    public static ArtifactEffect createPositive(String effectName, int value) {
        return new ArtifactEffect(effectName, value, true);
    }
    
    /**
     * Создает отрицательный эффект артефакта
     * 
     * @param effectName Название эффекта
     * @param value Значение эффекта
     * @return Созданный отрицательный эффект
     */
    public static ArtifactEffect createNegative(String effectName, int value) {
        return new ArtifactEffect(effectName, value, false);
    }
    
    /**
     * Применяет эффект к игроку
     * 
     * @param player Игрок
     * @param count Количество артефактов данного типа
     */
    public void apply(EntityPlayer player, int count) {
        // В серверной версии эффекты применяются не напрямую,
        // а через систему ArtifactManager
        if (count > 0) {
            // Применяем эффект с учетом количества артефактов
            // Можно ограничить стакание эффектов, например, до 3
            int scaledValue = Math.min(value * count, value * 3);
            
            // Вызываем метод применения эффекта из ArtifactManager
            ArtifactManager.applyPotionEffect(player, effectName, scaledValue);
        }
    }
    
    /**
     * Получить название эффекта
     * 
     * @return название эффекта
     */
    public String getEffectName() {
        return effectName;
    }
    
    /**
     * Получить значение эффекта
     * 
     * @return значение эффекта
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Является ли эффект положительным
     * 
     * @return true если эффект положительный
     */
    public boolean isPositive() {
        return isPositive;
    }
} 