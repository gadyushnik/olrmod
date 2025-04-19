package com.olrmod.common.anomaly;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Класс представляющий эффект аномалии
 */
public class AnomalyEffect {
    private final String name;
    private final DamageSource damageSource;
    private final float damageAmount;
    private final SoundEvent hitSound;
    private final boolean isInstantKill;
    private final EffectApplier effectApplier;
    private final String potionEffect;
    
    /**
     * Создает эффект аномалии с пользовательской логикой
     * 
     * @param effectApplier Функциональный интерфейс для применения эффекта
     */
    public AnomalyEffect(EffectApplier effectApplier) {
        this.name = "custom";
        this.damageSource = DamageSource.GENERIC;
        this.damageAmount = 0;
        this.hitSound = null;
        this.isInstantKill = false;
        this.effectApplier = effectApplier;
        this.potionEffect = "";
    }
    
    /**
     * Создает эффект аномалии с указанными параметрами
     * 
     * @param name Название эффекта
     * @param damageSource Источник урона
     * @param damageAmount Количество урона
     * @param hitSound Звук при попадании
     * @param isInstantKill Мгновенно убивает ли сущность
     * @param potionEffect Эффект зелья
     */
    public AnomalyEffect(String name, DamageSource damageSource, float damageAmount, 
                         SoundEvent hitSound, boolean isInstantKill, String potionEffect) {
        this.name = name;
        this.damageSource = damageSource;
        this.damageAmount = damageAmount;
        this.hitSound = hitSound;
        this.isInstantKill = isInstantKill;
        this.potionEffect = potionEffect;
        this.effectApplier = (world, pos, entity) -> {
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase living = (EntityLivingBase) entity;
                if (isInstantKill) {
                    living.setHealth(0);
                } else {
                    living.attackEntityFrom(damageSource, damageAmount);
                }
                
                if (hitSound != null) {
                    world.playSound(null, pos, hitSound, 
                                   net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
                
                if (!potionEffect.isEmpty()) {
                    String[] parts = potionEffect.split(";");
                    if (parts.length == 3) {
                        PotionEffect potion = new PotionEffect(
                            Potion.getPotionFromResourceLocation(parts[0]),
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2])
                        );
                        living.addPotionEffect(potion);
                    }
                }
            }
        };
    }
    
    /**
     * Применяет эффект аномалии к сущности
     * 
     * @param world Мир
     * @param pos Позиция аномалии
     * @param entity Сущность
     */
    public void apply(World world, BlockPos pos, Entity entity) {
        effectApplier.apply(world, pos, entity);
    }
    
    /**
     * Получить название эффекта
     * 
     * @return название эффекта
     */
    public String getName() {
        return name;
    }
    
    /**
     * Функциональный интерфейс для применения эффекта аномалии
     */
    @FunctionalInterface
    public interface EffectApplier {
        void apply(World world, BlockPos pos, Entity entity);
    }
} 