package com.olrmod.common.artifact;

import com.olrmod.StalkerMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Менеджер артефактов для серверной версии мода.
 * Позволяет привязывать эффекты артефактов к существующим предметам из других модов.
 */
@Mod.EventBusSubscriber
public class ArtifactManager {
    
    // Карта для хранения связей: предмет -> артефакт
    private static final Map<ResourceLocation, ArtifactType> ITEM_TO_ARTIFACT_MAP = new HashMap<>();
    
    // Карта для хранения эффектов артефактов
    private static final Map<ArtifactType, List<ArtifactEffect>> ARTIFACT_EFFECTS = new HashMap<>();
    
    // Длительность эффектов - 5 секунд (100 тиков) с постоянным обновлением
    private static final int EFFECT_DURATION = 100;
    
    /**
     * Инициализация системы артефактов
     */
    public static void init() {
        StalkerMod.logger.info("Серверная система артефактов инициализирована");
        initArtifactEffects();
    }
    
    /**
     * Регистрирует предмет как артефакт определенного типа
     * 
     * @param itemId ID предмета (modid:itemname)
     * @param type Тип артефакта
     */
    public static void registerArtifact(String itemId, ArtifactType type) {
        ResourceLocation resourceLocation = new ResourceLocation(itemId);
        
        Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
        if (item != null) {
            ITEM_TO_ARTIFACT_MAP.put(resourceLocation, type);
            StalkerMod.logger.info("Зарегистрирован предмет {} как артефакт типа {}", itemId, type.name());
        } else {
            StalkerMod.logger.warn("Не удалось найти предмет {} для регистрации артефакта", itemId);
        }
    }
    
    /**
     * Регистрирует эффект артефакта
     * 
     * @param type Тип артефакта
     * @param effect Эффект артефакта
     */
    public static void registerArtifactEffect(ArtifactType type, ArtifactEffect effect) {
        ARTIFACT_EFFECTS.computeIfAbsent(type, k -> new ArrayList<>()).add(effect);
    }
    
    /**
     * Инициализация стандартных эффектов артефактов
     */
    private static void initArtifactEffects() {
        // Медуза - поглощает радиацию, но ослабляет
        registerArtifactEffect(ArtifactType.MEDUSA, 
            ArtifactEffect.createPositive("radiation_protection", 2));
        registerArtifactEffect(ArtifactType.MEDUSA, 
            ArtifactEffect.createNegative("strength", 1));
        
        // Душа - увеличивает здоровье, но повышает восприимчивость к электричеству
        registerArtifactEffect(ArtifactType.SOUL, 
            ArtifactEffect.createPositive("health", 2));
        registerArtifactEffect(ArtifactType.SOUL, 
            ArtifactEffect.createNegative("electric_protection", 2));
        
        // Вспышка - защищает от электричества, но ускоряет радиационное отравление
        registerArtifactEffect(ArtifactType.FLASH, 
            ArtifactEffect.createPositive("electric_protection", 3));
        registerArtifactEffect(ArtifactType.FLASH, 
            ArtifactEffect.createNegative("radiation_protection", 2));
        
        // Каменный цветок - защищает от химических аномалий, но замедляет движение
        registerArtifactEffect(ArtifactType.STONE_FLOWER, 
            ArtifactEffect.createPositive("chemical_protection", 2));
        registerArtifactEffect(ArtifactType.STONE_FLOWER, 
            ArtifactEffect.createNegative("speed", 1));
        
        // Ночная звезда - способствует регенерации здоровья, но повышает излучение
        registerArtifactEffect(ArtifactType.NIGHT_STAR, 
            ArtifactEffect.createPositive("regeneration", 1));
        registerArtifactEffect(ArtifactType.NIGHT_STAR, 
            ArtifactEffect.createNegative("radiation", 1));
        
        // Пустышка - отталкивает радиацию, но делает уязвимым к пси-воздействию
        registerArtifactEffect(ArtifactType.EMPTY, 
            ArtifactEffect.createPositive("radiation_protection", 1));
        registerArtifactEffect(ArtifactType.EMPTY, 
            ArtifactEffect.createNegative("psy_protection", 2));
        
        // Огненный шар - защищает от огня, но привлекает электричество
        registerArtifactEffect(ArtifactType.FIREBALL, 
            ArtifactEffect.createPositive("fire_protection", 3));
        registerArtifactEffect(ArtifactType.FIREBALL, 
            ArtifactEffect.createNegative("electric_protection", 1));
        
        // Кристальная колючка - прочный защитный артефакт
        registerArtifactEffect(ArtifactType.CRYSTAL_THORN, 
            ArtifactEffect.createPositive("armor", 2));
        
        // Мамины бусы - усиливают защиту носителя ценой скорости
        registerArtifactEffect(ArtifactType.MOMS_BEADS, 
            ArtifactEffect.createPositive("armor", 3));
        registerArtifactEffect(ArtifactType.MOMS_BEADS, 
            ArtifactEffect.createNegative("speed", 2));
    }
    
    /**
     * Обработчик обновления живых сущностей
     */
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        
        if (entity.world.isRemote || !(entity instanceof EntityPlayer)) {
            return;
        }
        
        EntityPlayer player = (EntityPlayer) entity;
        
        // Проверяем каждую секунду (20 тиков)
        if (player.ticksExisted % 20 != 0) {
            return;
        }
        
        // Проверяем, есть ли артефакты в инвентаре игрока
        checkPlayerArtifacts(player);
    }
    
    /**
     * Проверяет наличие артефактов в инвентаре игрока и применяет их эффекты
     * 
     * @param player Игрок
     */
    private static void checkPlayerArtifacts(EntityPlayer player) {
        // Карта для подсчета количества артефактов каждого типа
        Map<ArtifactType, Integer> artifactCounts = new HashMap<>();
        
        // Проверяем основной инвентарь
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack.isEmpty()) continue;
            
            ResourceLocation itemId = stack.getItem().getRegistryName();
            if (itemId != null && ITEM_TO_ARTIFACT_MAP.containsKey(itemId)) {
                ArtifactType type = ITEM_TO_ARTIFACT_MAP.get(itemId);
                artifactCounts.put(type, artifactCounts.getOrDefault(type, 0) + stack.getCount());
            }
        }
        
        // Проверяем оффхэнд
        ItemStack offhandStack = player.getHeldItemOffhand();
        if (!offhandStack.isEmpty()) {
            ResourceLocation itemId = offhandStack.getItem().getRegistryName();
            if (itemId != null && ITEM_TO_ARTIFACT_MAP.containsKey(itemId)) {
                ArtifactType type = ITEM_TO_ARTIFACT_MAP.get(itemId);
                artifactCounts.put(type, artifactCounts.getOrDefault(type, 0) + offhandStack.getCount());
            }
        }
        
        // Применяем эффекты для каждого типа артефактов
        for (Map.Entry<ArtifactType, Integer> entry : artifactCounts.entrySet()) {
            ArtifactType type = entry.getKey();
            int count = entry.getValue();
            
            List<ArtifactEffect> effects = ARTIFACT_EFFECTS.get(type);
            if (effects != null) {
                for (ArtifactEffect effect : effects) {
                    effect.apply(player, count);
                }
            }
        }
    }
    
    /**
     * Применяет эффект potion к игроку на основе типа эффекта артефакта
     * 
     * @param player Игрок
     * @param effectName Название эффекта
     * @param level Уровень эффекта
     */
    public static void applyPotionEffect(EntityPlayer player, String effectName, int level) {
        Potion potion = null;
        
        // Сопоставляем строковые названия эффектов с объектами Potion
        switch (effectName) {
            case "regeneration":
                potion = net.minecraft.init.MobEffects.REGENERATION;
                break;
            case "strength":
                potion = net.minecraft.init.MobEffects.STRENGTH;
                break;
            case "speed":
                potion = net.minecraft.init.MobEffects.SPEED;
                break;
            case "health":
                potion = net.minecraft.init.MobEffects.HEALTH_BOOST;
                break;
            // Добавьте другие эффекты здесь
        }
        
        if (potion != null) {
            player.addPotionEffect(new PotionEffect(potion, EFFECT_DURATION, level - 1, false, false));
        }
    }
} 