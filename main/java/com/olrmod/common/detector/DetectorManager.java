package com.olrmod.common.detector;

import com.olrmod.StalkerMod;
import com.olrmod.common.anomaly.AnomalyManager;
import com.olrmod.common.radiation.RadiationManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * Менеджер детекторов для серверной версии мода.
 * Позволяет привязывать функционал детекторов к существующим предметам из других модов.
 */
@Mod.EventBusSubscriber
public class DetectorManager {
    
    // Карта для хранения связей: предмет -> тип детектора
    private static final Map<ResourceLocation, DetectorType> ITEM_TO_DETECTOR_MAP = new HashMap<>();
    
    // Карта для звуков детекторов
    private static final Map<DetectorType, SoundEvent> DETECTOR_SOUNDS = new HashMap<>();
    
    // Карта для радиусов обнаружения детекторов
    private static final Map<DetectorType, Integer> DETECTOR_RADIUSES = new HashMap<>();
    
    // Константы расстояний для разных уровней детекции
    private static final int CLOSE_DISTANCE = 3; // блоки
    private static final int MEDIUM_DISTANCE = 6; // блоки
    
    // Уровни обнаружения для частоты пищания
    public enum DetectionLevel {
        NONE(0),     // Ничего не обнаружено
        FAR(60),     // Далеко (пищит раз в 3 секунды)
        MEDIUM(40),  // Средняя дистанция (пищит раз в 2 секунды)
        CLOSE(20);   // Близко (пищит раз в секунду)
        
        private final int tickInterval;
        
        DetectionLevel(int tickInterval) {
            this.tickInterval = tickInterval;
        }
        
        public int getTickInterval() {
            return tickInterval;
        }
    }
    
    /**
     * Инициализация системы детекторов
     */
    public static void init() {
        StalkerMod.logger.info("Серверная система детекторов инициализирована");
        registerDefaultSounds();
    }
    
    /**
     * Регистрирует звуки для стандартных типов детекторов
     */
    private static void registerDefaultSounds() {
        // Регистрируем ванильные звуки для детекторов
        registerDetectorSound(DetectorType.ANOMALY, net.minecraft.init.SoundEvents.BLOCK_NOTE_PLING);
        registerDetectorSound(DetectorType.RADIATION, net.minecraft.init.SoundEvents.BLOCK_NOTE_HAT);
        registerDetectorSound(DetectorType.ARTIFACT, net.minecraft.init.SoundEvents.BLOCK_NOTE_BELL);
    }
    
    /**
     * Регистрирует предмет как детектор определенного типа
     * 
     * @param itemId ID предмета (modid:itemname)
     * @param type Тип детектора
     * @param radius Радиус действия детектора
     */
    public static void registerDetector(String itemId, DetectorType type, int radius) {
        ResourceLocation resourceLocation = new ResourceLocation(itemId);
        
        Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
        if (item != null) {
            ITEM_TO_DETECTOR_MAP.put(resourceLocation, type);
            DETECTOR_RADIUSES.put(type, radius);
            StalkerMod.logger.info("Зарегистрирован предмет {} как детектор типа {}", itemId, type.name());
        } else {
            StalkerMod.logger.warn("Не удалось найти предмет {} для регистрации детектора", itemId);
        }
    }
    
    /**
     * Регистрирует звук для типа детектора
     * 
     * @param type Тип детектора
     * @param sound Звук детектора
     */
    public static void registerDetectorSound(DetectorType type, SoundEvent sound) {
        DETECTOR_SOUNDS.put(type, sound);
    }
    
    /**
     * Обработчик обновления живых сущностей
     */
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }
        
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        World world = player.world;
        
        if (world.isRemote) {
            return;
        }
        
        // Проверяем детекторы у игрока
        checkPlayerDetectors(player);
    }
    
    /**
     * Проверяет наличие детекторов у игрока и их работу
     * 
     * @param player Игрок
     */
    private static void checkPlayerDetectors(EntityPlayer player) {
        // Проверяем только основной инвентарь и оффхенд
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack.isEmpty()) continue;
            
            ResourceLocation itemId = stack.getItem().getRegistryName();
            if (itemId != null && ITEM_TO_DETECTOR_MAP.containsKey(itemId)) {
                DetectorType type = ITEM_TO_DETECTOR_MAP.get(itemId);
                processDetector(player, type);
            }
        }
        
        ItemStack offhandStack = player.getHeldItemOffhand();
        if (!offhandStack.isEmpty()) {
            ResourceLocation itemId = offhandStack.getItem().getRegistryName();
            if (itemId != null && ITEM_TO_DETECTOR_MAP.containsKey(itemId)) {
                DetectorType type = ITEM_TO_DETECTOR_MAP.get(itemId);
                processDetector(player, type);
            }
        }
    }
    
    /**
     * Обрабатывает работу детектора определенного типа
     * 
     * @param player Игрок
     * @param type Тип детектора
     */
    private static void processDetector(EntityPlayer player, DetectorType type) {
        World world = player.world;
        DetectionLevel detectionLevel = DetectionLevel.NONE;
        
        // Проверяем в зависимости от типа детектора
        switch (type) {
            case ANOMALY:
                detectionLevel = checkForAnomalies(player);
                break;
            case RADIATION:
                detectionLevel = checkForRadiation(player);
                break;
            case ARTIFACT:
                detectionLevel = checkForArtifacts(player);
                break;
        }
        
        // Если что-то обнаружено, проигрываем звук в зависимости от уровня
        if (detectionLevel != DetectionLevel.NONE) {
            SoundEvent sound = DETECTOR_SOUNDS.get(type);
            if (sound != null && world.getTotalWorldTime() % detectionLevel.getTickInterval() == 0) {
                float pitch = 1.0f;
                
                // Для близкого расстояния увеличиваем высоту звука
                if (detectionLevel == DetectionLevel.CLOSE) {
                    pitch = 1.2f;
                } else if (detectionLevel == DetectionLevel.MEDIUM) {
                    pitch = 1.1f;
                }
                
                world.playSound(null, player.getPosition(), 
                    sound, SoundCategory.PLAYERS, 0.7f, pitch);
            }
        }
    }
    
    /**
     * Проверка наличия аномалий вокруг игрока с определением уровня близости
     */
    private static DetectionLevel checkForAnomalies(EntityPlayer player) {
        // Получаем ближайшую аномалию от AnomalyManager
        double closestDistance = AnomalyManager.getClosestAnomalyDistance(player);
        int detectionRadius = DETECTOR_RADIUSES.getOrDefault(DetectorType.ANOMALY, 10);
        
        // Определяем уровень детекции в зависимости от ближайшей найденной аномалии
        if (closestDistance <= detectionRadius) {
            if (closestDistance <= CLOSE_DISTANCE) {
                return DetectionLevel.CLOSE;
            } else if (closestDistance <= MEDIUM_DISTANCE) {
                return DetectionLevel.MEDIUM;
            } else {
                return DetectionLevel.FAR;
            }
        }
        
        return DetectionLevel.NONE;
    }
    
    /**
     * Проверка уровня радиации вокруг игрока с определением уровня близости
     */
    private static DetectionLevel checkForRadiation(EntityPlayer player) {
        // Получаем уровень радиации в текущей позиции игрока
        int radiationLevel = RadiationManager.getRadiationLevel(player.world, player.getPosition());
        
        // Возвращаем соответствующий уровень обнаружения в зависимости от уровня радиации
        switch (radiationLevel) {
            case 1:  // Слабый уровень радиации
                return DetectionLevel.FAR;
            case 2:  // Средний уровень радиации
                return DetectionLevel.MEDIUM;
            case 3:  // Высокий уровень радиации
                return DetectionLevel.CLOSE;
            default: // Радиации нет
                return DetectionLevel.NONE;
        }
    }
    
    /**
     * Проверка наличия артефактов вокруг игрока
     */
    private static DetectionLevel checkForArtifacts(EntityPlayer player) {
        // Функционал для проверки артефактов будет добавлен позже
        // Сейчас просто возвращаем NONE
        return DetectionLevel.NONE;
    }
    
    /**
     * Проверяет наличие детектора определенного типа у игрока
     * 
     * @param player Игрок
     * @param type Тип детектора
     * @return true если игрок имеет детектор указанного типа
     */
    public static boolean hasDetector(EntityPlayer player, DetectorType type) {
        // Проверяем основной инвентарь
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack.isEmpty()) continue;
            
            ResourceLocation itemId = stack.getItem().getRegistryName();
            if (itemId != null && ITEM_TO_DETECTOR_MAP.containsKey(itemId)) {
                if (ITEM_TO_DETECTOR_MAP.get(itemId) == type) {
                    return true;
                }
            }
        }
        
        // Проверяем оффхенд
        ItemStack offhandStack = player.getHeldItemOffhand();
        if (!offhandStack.isEmpty()) {
            ResourceLocation itemId = offhandStack.getItem().getRegistryName();
            if (itemId != null && ITEM_TO_DETECTOR_MAP.containsKey(itemId)) {
                return ITEM_TO_DETECTOR_MAP.get(itemId) == type;
            }
        }
        
        return false;
    }
} 