package com.olrmod.common.emission;

import com.olrmod.StalkerMod;
import com.olrmod.common.radiation.RadiationManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.init.SoundEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Менеджер выбросов для серверной версии мода.
 * 
 * Выбросы - это периодические события, во время которых все игроки должны найти укрытие или получить урон.
 */
@Mod.EventBusSubscriber
public class EmissionManager {
    
    // Текущее состояние выброса
    private static EmissionState currentState = EmissionState.INACTIVE;
    
    // Время (в тиках) до следующего выброса
    private static int timeToNextEmission = -1;
    
    // Время (в тиках) до окончания текущего выброса
    private static int timeToEndEmission = -1;
    
    // Время (в тиках) между оповещениями
    private static final int WARNING_INTERVAL = 1200; // 1 минута (60 секунд * 20 тиков)
    
    // Время (в тиках) между проверками безопасности
    private static final int SAFETY_CHECK_INTERVAL = 20; // 1 секунда (20 тиков)
    
    // Минимальное время (в тиках) между выбросами
    private static final int MIN_TIME_BETWEEN_EMISSIONS = 12000; // 10 минут (10 * 60 * 20 тиков)
    
    // Максимальное время (в тиках) между выбросами
    private static final int MAX_TIME_BETWEEN_EMISSIONS = 36000; // 30 минут (30 * 60 * 20 тиков)
    
    // Длительность выброса (в тиках)
    private static final int EMISSION_DURATION = 3600; // 3 минуты (3 * 60 * 20 тиков)
    
    // Длительность предупреждения (в тиках)
    private static final int WARNING_DURATION = 2400; // 2 минуты (2 * 60 * 20 тиков)
    
    // Карта для хранения информации о безопасных позициях игроков
    private static final Map<String, Boolean> PLAYER_SAFETY = new HashMap<>();
    
    // Список зон безопасности
    private static final List<SafeZone> SAFE_ZONES = new ArrayList<>();
    
    // Класс для представления зоны безопасности
    public static class SafeZone {
        private final BlockPos pos1;
        private final BlockPos pos2;
        private String name;
        
        public SafeZone(BlockPos pos1, BlockPos pos2) {
            this.pos1 = pos1;
            this.pos2 = pos2;
            this.name = "Убежище " + pos1.getX() + "," + pos1.getY() + "," + pos1.getZ();
        }
        
        public SafeZone(BlockPos pos1, BlockPos pos2, String name) {
            this.pos1 = pos1;
            this.pos2 = pos2;
            this.name = name != null && !name.isEmpty() ? name : "Убежище " + pos1.getX() + "," + pos1.getY() + "," + pos1.getZ();
        }
        
        public boolean contains(BlockPos pos) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            
            return x >= Math.min(pos1.getX(), pos2.getX()) && x <= Math.max(pos1.getX(), pos2.getX())
                && y >= Math.min(pos1.getY(), pos2.getY()) && y <= Math.max(pos1.getY(), pos2.getY())
                && z >= Math.min(pos1.getZ(), pos2.getZ()) && z <= Math.max(pos1.getZ(), pos2.getZ());
        }
        
        public BlockPos getPos1() {
            return pos1;
        }
        
        public BlockPos getPos2() {
            return pos2;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name + " [" + pos1.getX() + "," + pos1.getY() + "," + pos1.getZ() + " до " + 
                   pos2.getX() + "," + pos2.getY() + "," + pos2.getZ() + "]";
        }
    }
    
    // Состояния выброса
    public enum EmissionState {
        INACTIVE,   // Выброс неактивен
        WARNING,    // Выброс скоро начнется (предупреждение)
        ACTIVE      // Выброс активен
    }
    
    /**
     * Инициализация системы выбросов
     */
    public static void init() {
        StalkerMod.logger.info("Серверная система выбросов инициализирована");
        
        // Устанавливаем случайное время до первого выброса
        resetEmissionTimer();
    }
    
    /**
     * Обработчик тиков мира для управления выбросами
     */
    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        // Обрабатываем только в основном мире и только на сервере
        if (event.phase != TickEvent.Phase.END || event.world.isRemote || event.world.provider.getDimension() != 0) {
            return;
        }
        
        // Обновляем состояние выброса
        updateEmissionState(event.world);
    }
    
    /**
     * Обработчик обновления живых сущностей для применения эффектов выброса
     */
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        
        if (entity.world.isRemote || !(entity instanceof EntityPlayer)) {
            return;
        }
        
        // Проверяем только раз в секунду (20 тиков)
        if (entity.ticksExisted % SAFETY_CHECK_INTERVAL != 0) {
            return;
        }
        
        // Применяем эффекты выброса, если выброс активен
        if (currentState == EmissionState.ACTIVE) {
            applyEmissionEffects((EntityPlayer) entity);
        }
    }
    
    /**
     * Обновляет состояние выброса
     * 
     * @param world Мир
     */
    private static void updateEmissionState(World world) {
        switch (currentState) {
            case INACTIVE:
                // Проверяем, не пора ли начать предупреждение о выбросе
                if (timeToNextEmission <= WARNING_DURATION) {
                    // Переходим в состояние предупреждения
                    currentState = EmissionState.WARNING;
                    broadcastEmissionWarning(world);
                }
                break;
                
            case WARNING:
                // Проверяем, не пора ли начать выброс
                if (timeToNextEmission <= 0) {
                    // Переходим в состояние активного выброса
                    currentState = EmissionState.ACTIVE;
                    timeToEndEmission = EMISSION_DURATION;
                    broadcastEmissionStart(world);
                } else if (timeToNextEmission % WARNING_INTERVAL == 0) {
                    // Периодически отправляем предупреждения
                    broadcastEmissionWarning(world);
                }
                break;
                
            case ACTIVE:
                // Проверяем, не пора ли закончить выброс
                if (timeToEndEmission <= 0) {
                    // Переходим в неактивное состояние
                    currentState = EmissionState.INACTIVE;
                    resetEmissionTimer();
                    broadcastEmissionEnd(world);
                    // Очищаем карту безопасности
                    PLAYER_SAFETY.clear();
                }
                break;
        }
        
        // Уменьшаем таймеры
        if (timeToNextEmission > 0) {
            timeToNextEmission--;
        }
        
        if (timeToEndEmission > 0) {
            timeToEndEmission--;
        }
    }
    
    /**
     * Применяет эффекты выброса к игроку, если он не в безопасном месте
     * 
     * @param player Игрок
     */
    private static void applyEmissionEffects(EntityPlayer player) {
        String playerName = player.getName();
        
        // Проверяем безопасность игрока только раз в секунду
        if (!PLAYER_SAFETY.containsKey(playerName) || player.ticksExisted % 20 == 0) {
            boolean isSafe = isPlayerSafe(player);
            PLAYER_SAFETY.put(playerName, isSafe);
        }
        
        // Если игрок не в безопасности, применяем негативные эффекты
        if (!PLAYER_SAFETY.getOrDefault(playerName, false)) {
            // Наносим урон и применяем дебаффы
            player.attackEntityFrom(net.minecraft.util.DamageSource.OUT_OF_WORLD, 1.0F);
            player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 200, 0));
            player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 100, 0));
            
            // Отправляем предупреждающее сообщение
            if (player.ticksExisted % 100 == 0) { // каждые 5 секунд
                player.sendMessage(new TextComponentString(
                    TextFormatting.RED + "Вы подвергаетесь воздействию выброса! Найдите укрытие!"
                ));
            }
            
            // Воспроизводим звук опасности
            if (player.ticksExisted % 40 == 0) { // каждые 2 секунды
                player.world.playSound(null, player.getPosition(), 
                    net.minecraft.init.SoundEvents.ENTITY_GHAST_HURT, 
                    SoundCategory.AMBIENT, 1.0F, 0.8F);
            }
        }
    }
    
    /**
     * Проверяет, находится ли игрок в безопасном месте
     * 
     * @param player Игрок
     * @return true если игрок в безопасности
     */
    private static boolean isPlayerSafe(EntityPlayer player) {
        // Проверка подземелья - если игрок ниже уровня моря, он в безопасности
        if (player.getPosition().getY() < 64) {
            return true;
        }
        
        // Проверка наличия потолка над головой
        BlockPos pos = player.getPosition();
        for (int y = 1; y <= 10; y++) {
            BlockPos checkPos = pos.add(0, y, 0);
            if (!player.world.isAirBlock(checkPos)) {
                return true;
            }
        }
        
        // Проверка на безопасную зону
        if (isInSafeZone(player)) {
            return true;
        }
        
        // Игрок на открытом пространстве, не безопасно
        return false;
    }
    
    /**
     * Сбрасывает таймер выброса, устанавливая случайное время до следующего выброса
     */
    private static void resetEmissionTimer() {
        Random random = new Random();
        timeToNextEmission = MIN_TIME_BETWEEN_EMISSIONS + random.nextInt(MAX_TIME_BETWEEN_EMISSIONS - MIN_TIME_BETWEEN_EMISSIONS);
    }
    
    /**
     * Отправляет предупреждение о выбросе всем игрокам
     * 
     * @param world Мир
     */
    private static void broadcastEmissionWarning(World world) {
        int minutesLeft = (timeToNextEmission / 20) / 60;
        String message;
        
        if (minutesLeft > 0) {
            message = TextFormatting.YELLOW + "ВНИМАНИЕ! Выброс через " + minutesLeft + " минут! Найдите укрытие!";
        } else {
            message = TextFormatting.RED + "ВНИМАНИЕ! Выброс через " + (timeToNextEmission / 20) + " секунд! СРОЧНО найдите укрытие!";
        }
        
        // Отправляем сообщение всем игрокам
        for (EntityPlayer player : world.playerEntities) {
            player.sendMessage(new TextComponentString(message));
            
            // Воспроизводим звук предупреждения (ванильный звук)
            world.playSound(null, player.getPosition(), 
                net.minecraft.init.SoundEvents.ENTITY_ENDERDRAGON_GROWL, 
                SoundCategory.AMBIENT, 1.0F, 0.5F);
        }
    }
    
    /**
     * Уведомляет всех игроков о начале выброса
     * 
     * @param world Мир
     */
    private static void broadcastEmissionStart(World world) {
        String message = TextFormatting.DARK_RED + "ВЫБРОС НАЧАЛСЯ! Укройтесь немедленно!";
        
        // Отправляем сообщение всем игрокам
        for (EntityPlayer player : world.playerEntities) {
            player.sendMessage(new TextComponentString(message));
            
            // Воспроизводим звук начала выброса (ванильный звук)
            world.playSound(null, player.getPosition(), 
                net.minecraft.init.SoundEvents.ENTITY_ENDERDRAGON_DEATH, 
                SoundCategory.AMBIENT, 1.0F, 0.3F);
        }
    }
    
    /**
     * Уведомляет всех игроков об окончании выброса
     * 
     * @param world Мир
     */
    private static void broadcastEmissionEnd(World world) {
        String message = TextFormatting.GREEN + "Выброс закончился. Теперь можно выходить из укрытия.";
        
        // Отправляем сообщение всем игрокам
        for (EntityPlayer player : world.playerEntities) {
            player.sendMessage(new TextComponentString(message));
            
            // Воспроизводим звук окончания выброса (ванильный звук)
            world.playSound(null, player.getPosition(), 
                net.minecraft.init.SoundEvents.ENTITY_PLAYER_LEVELUP, 
                SoundCategory.AMBIENT, 1.0F, 1.0F);
        }
    }
    
    /**
     * Возвращает текущее состояние выброса
     * 
     * @return Текущее состояние выброса
     */
    public static EmissionState getCurrentState() {
        return currentState;
    }
    
    /**
     * Возвращает время (в тиках) до следующего выброса
     * 
     * @return Время до следующего выброса
     */
    public static int getTimeToNextEmission() {
        return timeToNextEmission;
    }
    
    /**
     * Принудительно запускает выброс
     * 
     * @param world Мир
     */
    public static void forceStartEmission(World world) {
        // Устанавливаем время до выброса на значение предупреждения
        timeToNextEmission = WARNING_DURATION;
        currentState = EmissionState.WARNING;
        broadcastEmissionWarning(world);
    }
    
    // Метод для проверки активности выброса
    public static boolean isEmissionActive() {
        return currentState == EmissionState.ACTIVE;
    }
    
    // Метод для принудительного старта выброса
    public static void startEmission(World world) {
        // Устанавливаем состояние выброса на ACTIVE
        currentState = EmissionState.ACTIVE;
        timeToEndEmission = EMISSION_DURATION;
        broadcastEmissionStart(world);
    }
    
    // Метод для остановки выброса
    public static void stopEmission() {
        // Устанавливаем состояние выброса на INACTIVE
        currentState = EmissionState.INACTIVE;
        resetEmissionTimer();
        broadcastEmissionEnd(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0));
        // Очищаем карту безопасности
        PLAYER_SAFETY.clear();
    }
    
    /**
     * Создаёт безопасную зону между двумя координатами
     * 
     * @param world Мир, в котором создаётся зона
     * @param pos1 Первая координата
     * @param pos2 Вторая координата
     */
    public static void createSafeZone(World world, BlockPos pos1, BlockPos pos2) {
        SafeZone safeZone = new SafeZone(pos1, pos2);
        SAFE_ZONES.add(safeZone);
        
        StalkerMod.logger.info("Создана безопасная зона: " + safeZone);
    }
    
    /**
     * Создаёт безопасную зону между двумя координатами с указанным названием
     * 
     * @param world Мир, в котором создаётся зона
     * @param pos1 Первая координата
     * @param pos2 Вторая координата
     * @param name Название зоны
     */
    public static void createSafeZone(World world, BlockPos pos1, BlockPos pos2, String name) {
        SafeZone safeZone = new SafeZone(pos1, pos2, name);
        SAFE_ZONES.add(safeZone);
        
        StalkerMod.logger.info("Создана безопасная зона: " + safeZone);
    }
    
    /**
     * Удаляет безопасную зону, содержащую указанную позицию
     * 
     * @param world Мир, в котором находится зона
     * @param pos Позиция для поиска зоны
     * @return true, если зона найдена и удалена
     */
    public static boolean removeSafeZone(World world, BlockPos pos) {
        int dimension = world.provider.getDimension();
        
        for (int i = 0; i < SAFE_ZONES.size(); i++) {
            SafeZone zone = SAFE_ZONES.get(i);
            
            if (zone.contains(pos)) {
                SAFE_ZONES.remove(i);
                
                StalkerMod.logger.info("Удалена безопасная зона: " + zone);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Получает список всех безопасных зон
     * 
     * @return Список безопасных зон
     */
    public static List<SafeZone> getSafeZones() {
        return new ArrayList<>(SAFE_ZONES);
    }
    
    /**
     * Удаляет безопасную зону по её названию
     * 
     * @param name Название зоны
     * @return true, если зона найдена и удалена
     */
    public static boolean removeSafeZoneByName(String name) {
        for (int i = 0; i < SAFE_ZONES.size(); i++) {
            SafeZone zone = SAFE_ZONES.get(i);
            
            if (zone.getName().equalsIgnoreCase(name)) {
                SAFE_ZONES.remove(i);
                
                StalkerMod.logger.info("Удалена безопасная зона: " + zone);
                return true;
            }
        }
        
        return false;
    }
    
    // Метод для проверки, находится ли игрок в безопасной зоне
    public static boolean isInSafeZone(EntityPlayer player) {
        BlockPos pos = player.getPosition();
        for (SafeZone zone : SAFE_ZONES) {
            if (zone.contains(pos)) {
                return true;
            }
        }
        return false;
    }

    public static void triggerEmission(World world) {
        // Пример изменения механики выбросов
        for (EntityPlayer player : world.playerEntities) {
            if (!player.isCreative()) {
                // Применяем сильный эффект пси-урона вместо мгновенной смерти
                player.addPotionEffect(new PotionEffect(
                    MobEffects.WITHER, // Используем эффект Wither как пример
                    600, // Длительность эффекта (30 секунд)
                    2 // Уровень эффекта
                ));
            }
        }

        // Проигрываем звук выброса для первого игрока в списке
        if (!world.playerEntities.isEmpty()) {
            EntityPlayer firstPlayer = world.playerEntities.get(0);
            world.playSound(null, firstPlayer.posX, firstPlayer.posY, firstPlayer.posZ,
                SoundEvents.ENTITY_ENDERDRAGON_GROWL, // Пример звука
                SoundCategory.AMBIENT,
                1.0F,
                1.0F
            );
        }
    }
}