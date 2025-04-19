package com.olrmod.common.anomaly;

import com.olrmod.StalkerMod;
import com.olrmod.common.effects.AnomalyEffects;
import com.olrmod.common.effects.DamageSourceRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.SoundEvents;
import com.olrmod.common.effects.CustomEffect;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Менеджер аномалий для серверной версии мода.
 * 
 * Эта система позволяет привязывать эффекты аномалий к блокам из других модов.
 * Вместо создания собственных блоков, мы просто "прикрепляем" нашу логику к существующим.
 */
@Mod.EventBusSubscriber
public class AnomalyManager {
    
    // Карта для хранения связей: блок -> тип аномалии
    private static final Map<ResourceLocation, AnomalyType> BLOCK_TO_ANOMALY_MAP = new HashMap<>();
    
    // Карта для отслеживания сущностей в аномалиях mosquito
    private static final Map<UUID, Pair<BlockPos, Long>> ENTITIES_IN_MOSQUITO = new HashMap<>();
    
    // Карта для хранения парных эффектов аномалий
    private static final Map<AnomalyType, Set<AnomalyEffect>> ANOMALY_EFFECTS = new HashMap<>();
    
    // Карта для хранения кастомных эффектов
    private static final Map<AnomalyType, CustomEffect> CUSTOM_EFFECTS = new HashMap<>();
    
    // Константы для настройки различных аномалий
    private static final int EFFECT_DURATION = 200; // 10 секунд (стандартная длительность)
    private static final int MOSQUITO_ATTRACTION_RADIUS = 7;
    private static final double MOSQUITO_ATTRACTION_STRENGTH = 0.4;
    private static final float BLACK_NEEDLES_DIRECT_DAMAGE = 4.0F;
    
    /**
     * Инициализация системы аномалий
     */
    public static void init() {
        MinecraftForge.EVENT_BUS.register(AnomalyManager.class);
        StalkerMod.logger.info("Серверная система аномалий инициализирована");
        initAnomalyEffects();
        initCustomEffects();
    }
    
    /**
     * Инициализация стандартных эффектов аномалий
     */
    private static void initAnomalyEffects() {
        for (Map.Entry<ResourceLocation, AnomalyType> entry : BLOCK_TO_ANOMALY_MAP.entrySet()) {
            AnomalyType type = entry.getValue();
            AnomalyEffectConfig config = StalkerMod.ANOMALY_EFFECTS_CONFIG.get(type);
            if (config != null) {
                registerAnomalyEffect(type, createEffectFromConfig(config));
            }
        }
    }
    
    private static AnomalyEffect createEffectFromConfig(AnomalyEffectConfig config) {
        return new AnomalyEffect((world, pos, entity) -> {
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase living = (EntityLivingBase) entity;
                living.attackEntityFrom(DamageSource.GENERIC, config.damageAmount);
                
                if (!config.potionEffect.isEmpty()) {
                    String[] parts = config.potionEffect.split(";");
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
        });
    }
    
    /**
     * Регистрирует блок как аномалию определенного типа
     * 
     * @param blockId Полный ID блока (modid:blockname)
     * @param type Тип аномалии
     */
    public static void registerAnomalyBlock(String blockId, AnomalyType type) {
        ResourceLocation resourceLocation = new ResourceLocation(blockId);
        
        // Проверяем, существует ли такой блок
        Block block = ForgeRegistries.BLOCKS.getValue(resourceLocation);
        if (block != null) {
            BLOCK_TO_ANOMALY_MAP.put(resourceLocation, type);
            StalkerMod.logger.info("Зарегистрирован блок {} как аномалия типа {}", blockId, type);
        } else {
            StalkerMod.logger.warn("Не удалось найти блок {} для регистрации аномалии", blockId);
        }
    }
    
    /**
     * Регистрирует эффект для определенного типа аномалии
     */
    public static void registerAnomalyEffect(AnomalyType type, AnomalyEffect effect) {
        ANOMALY_EFFECTS.computeIfAbsent(type, k -> new HashSet<>()).add(effect);
    }
    
    /**
     * Обработчик события взаимодействия сущности с блоком
     */
    @SubscribeEvent
    public static void onLivingCollideWithBlock(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        World world = entity.world;
        
        // Пропускаем обработку на клиенте
        if (world.isRemote) {
            return;
        }
        
        // Получаем позицию блока, на котором стоит сущность
        BlockPos pos = new BlockPos(
            Math.floor(entity.posX), 
            Math.floor(entity.posY - 0.1), // Небольшое смещение вниз, чтобы убедиться, что мы берем блок под ногами
            Math.floor(entity.posZ)
        );
        
        // Проверяем, является ли блок аномалией
        Block block = world.getBlockState(pos).getBlock();
        ResourceLocation resourceLocation = block.getRegistryName();
        
        if (resourceLocation != null && BLOCK_TO_ANOMALY_MAP.containsKey(resourceLocation)) {
            // Получаем тип аномалии и применяем соответствующие эффекты
            AnomalyType anomalyType = BLOCK_TO_ANOMALY_MAP.get(resourceLocation);
            Set<AnomalyEffect> effects = ANOMALY_EFFECTS.get(anomalyType);
            
            if (effects != null) {
                for (AnomalyEffect effect : effects) {
                    effect.apply(world, pos, entity);
                }
            }

            // Применяем кастомные эффекты
            CustomEffect customEffect = CUSTOM_EFFECTS.get(anomalyType);
            if (customEffect != null) {
                customEffect.updateEffect(entity);
            }
        }
        
        // Проверяем близлежащие блоки для аномалий с радиусом действия
        checkNearbyAnomalies(world, entity);
    }
    
    /**
     * Проверяет наличие аномалий вокруг сущности
     * // FIXME: Оптимизация: Эта проверка в кубе 7x7x7 каждый тик может быть ресурсоемкой.
     * // Рассмотреть: уменьшение радиуса/частоты, использование spatial hashing.
     */
    private static void checkNearbyAnomalies(World world, EntityLivingBase entity) {
        BlockPos entityPos = entity.getPosition();
        
        // Проверяем блоки в радиусе 3 (можно настроить)
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos checkPos = entityPos.add(x, y, z);
                    Block block = world.getBlockState(checkPos).getBlock();
                    
                    if (block.getRegistryName() != null) {
                        ResourceLocation resourceLocation = block.getRegistryName();
                        
                        if (BLOCK_TO_ANOMALY_MAP.containsKey(resourceLocation)) {
                            AnomalyType anomalyType = BLOCK_TO_ANOMALY_MAP.get(resourceLocation);
                            
                            // Особая обработка для комариной плеши с радиусом действия
                            if (anomalyType == AnomalyType.MOSQUITO) {
                                double distance = entity.getDistance(
                                    checkPos.getX() + 0.5, 
                                    checkPos.getY() + 0.5, 
                                    checkPos.getZ() + 0.5
                                );
                                
                                if (distance <= MOSQUITO_ATTRACTION_RADIUS) {
                                    // Помечаем сущность как находящуюся в области действия комариной плеши
                                    ENTITIES_IN_MOSQUITO.put(entity.getUniqueID(), Pair.of(checkPos, world.getTotalWorldTime()));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Обработчик тиков для аномалий, требующих постоянной обработки
     */
    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.world.isRemote) {
            processMosquitoAttraction(event.world);
            cleanupAnomalyData(event.world);
        }
    }
    
    /**
     * Обрабатывает эффект притяжения для Комариной плеши
     */
    private static void processMosquitoAttraction(World world) {
        // Обрабатываем притяжение для сущностей в зоне действия комариной плеши
        for (Map.Entry<UUID, Pair<BlockPos, Long>> entry : ENTITIES_IN_MOSQUITO.entrySet()) {
            UUID entityId = entry.getKey();
            BlockPos anomalyPos = entry.getValue().getLeft();
            
            Entity entity = findEntity(world, entityId);
            if (entity == null || entity.isDead) {
                continue;
            }
            
            processMosquitoAttraction(world, anomalyPos, entity, MOSQUITO_ATTRACTION_RADIUS, MOSQUITO_ATTRACTION_STRENGTH);
        }
    }
    
    /**
     * Находит сущность по UUID в мире
     * // FIXME: Оптимизация: Перебор всего списка loadedEntityList может быть медленным.
     * // Рассмотреть: использование карт или world.getEntityFromUuid()
     */
    private static Entity findEntity(World world, UUID entityId) {
        // Перебор списка (работает в 1.12.2)
        for (Entity loadedEntity : world.loadedEntityList) {
            if (loadedEntity.getUniqueID().equals(entityId)) {
                return loadedEntity;
            }
        }
        return null;
    }
    
    /**
     * Получает расстояние до ближайшей аномалии для игрока
     * // FIXME: Оптимизация: Проверка в кубе 21x21x21 может быть ресурсоемкой.
     * // Рассмотреть: уменьшение радиуса, более эффективные структуры данных для поиска аномалий.
     *
     * @param player Игрок
     * @return Расстояние до ближайшей аномалии или максимальное значение, если аномалий нет рядом
     */
    public static double getClosestAnomalyDistance(EntityPlayer player) {
        World world = player.world;
        BlockPos playerPos = player.getPosition();
        double closestDistance = Double.MAX_VALUE;
        
        // Проверяем блоки в радиусе поиска (максимальный радиус детектора)
        int searchRadius = 10;
        
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = playerPos.add(x, y, z);
                    Block block = world.getBlockState(checkPos).getBlock();
                    
                    if (block.getRegistryName() != null && BLOCK_TO_ANOMALY_MAP.containsKey(block.getRegistryName())) {
                        // Вычисляем точное расстояние от игрока до аномалии
                        double distance = Math.sqrt(
                            Math.pow(playerPos.getX() - checkPos.getX(), 2) +
                            Math.pow(playerPos.getY() - checkPos.getY(), 2) +
                            Math.pow(playerPos.getZ() - checkPos.getZ(), 2)
                        );
                        
                        if (distance < closestDistance) {
                            closestDistance = distance;
                        }
                    }
                }
            }
        }
        
        return closestDistance;
    }
    
    /**
     * Удаляет устаревшие данные об аномалиях
     */
    private static void cleanupAnomalyData(World world) {
        long currentTime = world.getTotalWorldTime();
        
        // Удаляем устаревшие записи о сущностях в зоне комариной плеши (5 секунд неактивности)
        ENTITIES_IN_MOSQUITO.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().getRight() > 100 || // Более 5 секунд неактивности
            findEntity(world, entry.getKey()) == null // Сущность больше не существует
        );
    }
    
    /**
     * Возвращает звук "удара" для комариной плеши (когда сущность в центре)
     */
    private static SoundEvent getMosquitoHitSound() {
        // Используем ванильный звук вместо своего
        return SoundEvents.ENTITY_ENDERMEN_AMBIENT;
    }
    
    /**
     * Обрабатывает притяжение сущностей к центру аномалии Комариная плешь.
     * Эта функция теперь также отвечает за нанесение смертельного урона при приближении.
     * 
     * @param world Мир
     * @param anomalyPos Позиция блока аномалии
     * @param entity Сущность
     * @param attractionRadius Радиус притяжения
     * @param attractionStrength Базовая сила притяжения
     */
    public static void processMosquitoAttraction(World world, BlockPos anomalyPos, Entity entity, double attractionRadius, double attractionStrength) {
        if (entity == null || !entity.isEntityAlive() || entity instanceof EntityItem) {
            return;
        }

        // Вычисляем центр аномалии
        Vec3d anomalyCenter = new Vec3d(anomalyPos).addVector(0.5, 0.5, 0.5);
        Vec3d entityPos = entity.getPositionVector();
        
        // Вектор от сущности к аномалии
        Vec3d attractionVec = anomalyCenter.subtract(entityPos);
        double distance = attractionVec.lengthVector();
        
        // Если сущность слишком далеко, прекращаем эффект
        if (distance > attractionRadius) {
            return;
        }
        
        // Если сущность очень близко к аномалии, убиваем её
        if (distance < 1.5) {
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                if (player.isCreative()) {
                    // В креативе игроки не получают урон
                    return;
                }
                
                // Применяем смертельный эффект
                entity.attackEntityFrom(DamageSourceRegistry.GRAVITY_DAMAGE, 1000.0F);
                StalkerMod.logger.info("Игрок {} уничтожен комариной плешью", player.getName());
            } else if (entity instanceof EntityLivingBase) {
                // Для других живых сущностей - мгновенная смерть
                entity.attackEntityFrom(DamageSourceRegistry.GRAVITY_DAMAGE, 1000.0F);
            } else {
                // Для неживых сущностей (если такие попадут) - просто удаляем
                entity.setDead(); 
            }
            // Выходим, так как сущность мертва
            return;
        }
        
        // Нормализуем вектор притяжения
        Vec3d normalized = attractionVec.normalize();
        
        // Увеличиваем силу притяжения с уменьшением расстояния (квадратичная зависимость)
        double strengthFactor = attractionStrength * (1.0 - (distance / attractionRadius)) * (1.0 - (distance / attractionRadius));
        
        // Для игроков
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            
            // Игроки в креативном режиме не притягиваются
            if (player.isCreative()) {
                return;
            }
            
            // Логируем притяжение игрока каждые 40 тиков
            if (world.getTotalWorldTime() % 40 == 0) {
                StalkerMod.logger.info("Игрок {} притягивается комариной плешью. Расстояние: {}, Сила: {}", 
                    player.getName(), String.format("%.2f", distance), String.format("%.4f", strengthFactor));
            }
            
            // Применяем эффект притяжения к игроку
            player.motionX += normalized.x * strengthFactor;
            player.motionZ += normalized.z * strengthFactor;
            
            // Не даем игроку "улететь" вверх при сильном притяжении
            if (distance < 2.5) {
                player.motionY = Math.min(player.motionY, 0.0);
            }
            
            // Воспроизводим звук
            if (world.getTotalWorldTime() % 20 == 0 && world.rand.nextInt(3) == 0) {
                world.playSound(null, player.posX, player.posY, player.posZ, 
                    getMosquitoIdleSound(), SoundCategory.AMBIENT, 
                    0.7F + world.rand.nextFloat() * 0.3F, 0.8F + world.rand.nextFloat() * 0.4F);
            }
        } 
        // Для других живых сущностей
        else if (entity instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) entity;
            
            // Отключаем ИИ, пока сущность притягивается
            living.setNoAI(true);
            
            // Применяем притяжение
            living.motionX += normalized.x * strengthFactor;
            living.motionZ += normalized.z * strengthFactor;
            
            // Не даем сущности "улететь" вверх
            if (distance < 2.5) {
                living.motionY = Math.min(living.motionY, 0.0);
            }
            
            // Воспроизводим звук реже для других сущностей
            if (world.getTotalWorldTime() % 40 == 0 && world.rand.nextInt(5) == 0) {
                world.playSound(null, living.posX, living.posY, living.posZ, 
                    getMosquitoIdleSound(), SoundCategory.AMBIENT, 
                    0.5F, 0.8F + world.rand.nextFloat() * 0.4F);
            }
        }
    }
    
    /**
     * Получает звук для комариной плеши
     */
    private static SoundEvent getMosquitoIdleSound() {
        return SoundEvents.ENTITY_ENDERMEN_AMBIENT;
    }

    private static void initCustomEffects() {
        // Добавить другие эффекты по аналогии
    }

    private static void applyBuffsAndDebuffs(EntityPlayer player) {
        // Пример применения баффов и дебаффов из конфигурации
        // Здесь мы будем использовать конфигурацию для получения информации о баффах и дебаффах
        // и применять их к игроку

        // Пример: применение эффекта устойчивости
        PotionEffect resistance = new PotionEffect(MobEffects.RESISTANCE, 200, 1);
        player.addPotionEffect(resistance);

        // Пример: применение эффекта слабости
        PotionEffect weakness = new PotionEffect(MobEffects.WEAKNESS, 200, 1);
        player.addPotionEffect(weakness);
    }
} 