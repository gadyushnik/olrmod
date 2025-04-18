package com.olrmod.common.radiation;

import com.olrmod.StalkerMod;
import com.olrmod.common.detector.DetectorManager;
import com.olrmod.common.detector.DetectorType;
import com.olrmod.common.effects.AnomalyEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class RadiationManager {
    
    // Список всех радиационных зон в мире (хранятся как пары точек)
    private static final Map<Integer, List<RadiationZone>> RADIATION_ZONES = new HashMap<>();
    
    // Карта для хранения связей: предмет -> флаг распознавания радиации
    private static final Map<ResourceLocation, Boolean> RADIATION_DETECTORS = new HashMap<>();
    
    /**
     * Инициализация системы радиации
     */
    public static void init() {
        StalkerMod.logger.info("Инициализация системы радиации");
        // Базовая инициализация, при необходимости здесь можно добавить дополнительные действия
    }
    
    /**
     * Регистрирует предмет как детектор радиации
     */
    public static void registerRadiationDetector(String itemId) {
        ResourceLocation resourceLocation = new ResourceLocation(itemId);
        Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
        
        if (item != null) {
            RADIATION_DETECTORS.put(resourceLocation, true);
            StalkerMod.logger.info("Зарегистрирован предмет {} как детектор радиации", itemId);
        } else {
            StalkerMod.logger.warn("Не удалось найти предмет {} для регистрации детектора радиации", itemId);
        }
    }
    
    // Оптимизированный способ хранения и доступа к радиационным зонам через измерение
    private static class RadiationZone {
        private final BlockPos pos1;
        private final BlockPos pos2;
        private final int level;
        
        public RadiationZone(BlockPos pos1, BlockPos pos2, int level) {
            // Нормализуем координаты, чтобы pos1 всегда был минимальной точкой, а pos2 - максимальной
            this.pos1 = new BlockPos(
                Math.min(pos1.getX(), pos2.getX()),
                Math.min(pos1.getY(), pos2.getY()),
                Math.min(pos1.getZ(), pos2.getZ())
            );
            this.pos2 = new BlockPos(
                Math.max(pos1.getX(), pos2.getX()),
                Math.max(pos1.getY(), pos2.getY()),
                Math.max(pos1.getZ(), pos2.getZ())
            );
            this.level = level;
        }
        
        public boolean isInside(BlockPos pos) {
            return pos.getX() >= pos1.getX() && pos.getX() <= pos2.getX() &&
                   pos.getY() >= pos1.getY() && pos.getY() <= pos2.getY() &&
                   pos.getZ() >= pos1.getZ() && pos.getZ() <= pos2.getZ();
        }
        
        public boolean isInside(Entity entity) {
            // Проверяем, находится ли entity в пределах зоны
            AxisAlignedBB entityBox = entity.getEntityBoundingBox();
            AxisAlignedBB zoneBox = new AxisAlignedBB(
                pos1.getX(), pos1.getY(), pos1.getZ(),
                pos2.getX() + 1, pos2.getY() + 1, pos2.getZ() + 1
            );
            
            return zoneBox.intersects(entityBox);
        }
        
        public int getLevel() {
            return level;
        }
    }
    
    /**
     * Создать новую радиационную зону
     * @param world Мир
     * @param pos1 Первая точка зоны
     * @param pos2 Вторая точка зоны
     * @param level Уровень радиации (1-3)
     */
    public static void createRadiationZone(World world, BlockPos pos1, BlockPos pos2, int level) {
        int dimensionId = world.provider.getDimension();
        
        // Получаем или создаем список зон для текущего измерения
        List<RadiationZone> zones = RADIATION_ZONES.computeIfAbsent(dimensionId, k -> new ArrayList<>());
        
        // Создаем новую зону и добавляем в список
        RadiationZone zone = new RadiationZone(pos1, pos2, level);
        zones.add(zone);
        
        StalkerMod.logger.info("Создана радиационная зона уровня {} в измерении {}", level, dimensionId);
    }
    
    /**
     * Удалить радиационную зону, содержащую указанную точку
     * @param world Мир
     * @param pos Точка внутри зоны
     * @return true если зона была найдена и удалена
     */
    public static boolean removeRadiationZone(World world, BlockPos pos) {
        int dimensionId = world.provider.getDimension();
        
        List<RadiationZone> zones = RADIATION_ZONES.get(dimensionId);
        if (zones == null) {
            return false;
        }
        
        for (int i = 0; i < zones.size(); i++) {
            if (zones.get(i).isInside(pos)) {
                zones.remove(i);
                StalkerMod.logger.info("Удалена радиационная зона в измерении {}", dimensionId);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Проверить, находится ли точка в радиационной зоне и получить уровень радиации
     * @param world Мир
     * @param pos Позиция
     * @return Уровень радиации (0 если точка не в радиационной зоне)
     */
    public static int getRadiationLevel(World world, BlockPos pos) {
        int dimensionId = world.provider.getDimension();
        
        List<RadiationZone> zones = RADIATION_ZONES.get(dimensionId);
        if (zones == null) {
            return 0;
        }
        
        for (RadiationZone zone : zones) {
            if (zone.isInside(pos)) {
                return zone.getLevel();
            }
        }
        
        return 0;
    }
    
    /**
     * Проверить находится ли сущность в радиационной зоне
     * @param entity Сущность
     * @return Уровень радиации (0 если сущность не в радиационной зоне)
     */
    public static int getRadiationLevel(Entity entity) {
        if (entity.world.isRemote) {
            return 0;
        }
        
        int dimensionId = entity.world.provider.getDimension();
        
        List<RadiationZone> zones = RADIATION_ZONES.get(dimensionId);
        if (zones == null) {
            return 0;
        }
        
        for (RadiationZone zone : zones) {
            if (zone.isInside(entity)) {
                return zone.getLevel();
            }
        }
        
        return 0;
    }
    
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        
        if (entity.world.isRemote || entity.isDead) {
            return;
        }
        
        // Проверяем раз в секунду (20 тиков)
        if (entity.ticksExisted % 20 != 0) {
            return;
        }
        
        int radiationLevel = getRadiationLevel(entity);
        
        if (radiationLevel > 0) {
            // Проигрываем звук гейгера, если у игрока есть детектор
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                
                if (hasRadiationDetector(player)) {
                    // Воспроизводим звук детектора - используем ванильный звук
                    entity.world.playSound(null, entity.getPosition(),
                        net.minecraft.init.SoundEvents.BLOCK_NOTE_HAT,
                        SoundCategory.PLAYERS, 0.8f, 1.0f + (radiationLevel * 0.1f));
                }
            }
            
            // Применяем эффект радиации в зависимости от уровня зоны
            switch (radiationLevel) {
                case 1:
                    entity.addPotionEffect(new PotionEffect(AnomalyEffects.RADIATION_DAMAGE_WEAK, 100, 0));
                    break;
                case 2:
                    entity.addPotionEffect(new PotionEffect(AnomalyEffects.RADIATION_DAMAGE_NORMAL, 100, 0));
                    break;
                case 3:
                    entity.addPotionEffect(new PotionEffect(AnomalyEffects.RADIATION_DAMAGE_STRONG, 100, 0));
                    break;
            }
        }
    }
    
    /**
     * Проверка наличия детектора радиации в инвентаре игрока
     */
    private static boolean hasRadiationDetector(EntityPlayer player) {
        // Проверяем основной инвентарь
        for (ItemStack stack : player.inventory.mainInventory) {
            if (!stack.isEmpty() && stack.getItem().getRegistryName() != null) {
                ResourceLocation itemId = stack.getItem().getRegistryName();
                if (RADIATION_DETECTORS.containsKey(itemId)) {
                    return true;
                }
            }
        }
        
        // Проверяем оффхенд
        ItemStack offhandStack = player.getHeldItemOffhand();
        if (!offhandStack.isEmpty() && offhandStack.getItem().getRegistryName() != null) {
            ResourceLocation itemId = offhandStack.getItem().getRegistryName();
            return RADIATION_DETECTORS.containsKey(itemId);
        }
        
        // Также проверяем детекторы, зарегистрированные в DetectorManager
        return DetectorManager.hasDetector(player, DetectorType.RADIATION);
    }
} 