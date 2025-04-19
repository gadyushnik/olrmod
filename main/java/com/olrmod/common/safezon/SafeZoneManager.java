package com.olrmod.common.safezon;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер безопасных зон
 */
public class SafeZoneManager {
    
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<UUID, List<SafeZone>> PLAYER_SAFE_ZONES = new HashMap<>();
    private static final Map<Integer, List<SafeZone>> DIMENSION_SAFE_ZONES = new HashMap<>();
    
    private SafeZoneManager() {
        // Приватный конструктор для статического класса
    }
    
    /**
     * Инициализация менеджера безопасных зон
     */
    public static void init() {
        LOGGER.info("Инициализация менеджера безопасных зон");
    }
    
    /**
     * Добавляет новую безопасную зону
     * 
     * @param world Мир
     * @param center Центр зоны
     * @param radius Радиус зоны
     * @param owner Владелец зоны (опционально)
     * @return Созданная безопасная зона
     */
    public static SafeZone addSafeZone(World world, BlockPos center, int radius, EntityPlayer owner) {
        SafeZone safeZone = new SafeZone(world.provider.getDimension(), center, radius, owner != null ? owner.getUniqueID() : null);
        
        // Добавляем зону в соответствующие коллекции
        DIMENSION_SAFE_ZONES.computeIfAbsent(world.provider.getDimension(), k -> new ArrayList<>()).add(safeZone);
        
        if (owner != null) {
            PLAYER_SAFE_ZONES.computeIfAbsent(owner.getUniqueID(), k -> new ArrayList<>()).add(safeZone);
        }
        
        LOGGER.info("Создана безопасная зона в измерении {} с центром {} и радиусом {}", 
                world.provider.getDimension(), center, radius);
                
        return safeZone;
    }
    
    /**
     * Проверяет, находится ли позиция в какой-либо безопасной зоне
     * 
     * @param world Мир
     * @param pos Позиция для проверки
     * @return true, если позиция в безопасной зоне
     */
    public static boolean isInSafeZone(World world, BlockPos pos) {
        int dimension = world.provider.getDimension();
        List<SafeZone> safeZones = DIMENSION_SAFE_ZONES.get(dimension);
        
        if (safeZones == null || safeZones.isEmpty()) {
            return false;
        }
        
        for (SafeZone zone : safeZones) {
            if (zone.contains(pos)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Класс, представляющий безопасную зону
     */
    public static class SafeZone {
        private final int dimension;
        private final BlockPos center;
        private final int radius;
        private final UUID ownerUUID;
        
        public SafeZone(int dimension, BlockPos center, int radius, UUID ownerUUID) {
            this.dimension = dimension;
            this.center = center;
            this.radius = radius;
            this.ownerUUID = ownerUUID;
        }
        
        /**
         * Проверяет, находится ли позиция в этой безопасной зоне
         * 
         * @param pos Позиция для проверки
         * @return true, если позиция в зоне
         */
        public boolean contains(BlockPos pos) {
            double distanceSq = center.distanceSq(pos);
            return distanceSq <= (radius * radius);
        }
        
        // Геттеры
        public BlockPos getCenter() {
            return center;
        }
        
        public int getRadius() {
            return radius;
        }
        
        public UUID getOwnerUUID() {
            return ownerUUID;
        }
        
        public int getDimension() {
            return dimension;
        }
    }
} 