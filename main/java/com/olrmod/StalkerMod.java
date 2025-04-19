package com.olrmod;

import com.olrmod.common.anomaly.AnomalyManager;
import com.olrmod.common.anomaly.AnomalyRegistry;
import com.olrmod.common.anomaly.AnomalyType;
import com.olrmod.common.artifact.ArtifactManager;
import com.olrmod.common.artifact.ArtifactType;
import com.olrmod.common.commands.CommandEmission;
import com.olrmod.common.commands.CommandRadiation;
import com.olrmod.common.commands.CommandSafeZone;
import com.olrmod.common.detector.DetectorManager;
import com.olrmod.common.detector.DetectorType;
import com.olrmod.common.effects.AnomalyEffects;
import com.olrmod.common.effects.DamageSourceRegistry;
import com.olrmod.common.emission.EmissionManager;
import com.olrmod.common.radiation.RadiationManager;
import com.olrmod.proxy.CommonProxy;
import com.olrmod.proxy.ServerProxy;
import com.olrmod.common.anomaly.AnomalyEffectConfig;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Mod(
    modid = StalkerMod.MODID,
    name = StalkerMod.NAME,
    version = StalkerMod.VERSION,
    acceptedMinecraftVersions = "[1.12.2]",
    serverSideOnly = true
)
public class StalkerMod {
    public static final String MODID = "olrmod";
    public static final String NAME = "S.T.A.L.K.E.R. Mod Lite Server";
    public static final String VERSION = "1.0";

    public static Logger logger;
    
    @Mod.Instance(MODID)
    public static StalkerMod instance;
    
    @SidedProxy(
        serverSide = "com.olrmod.proxy.ServerProxy"
    )
    public static CommonProxy proxy;
    
    // Категория звуков для мода
    public static final SoundCategory STALKER_SOUND_CATEGORY = SoundCategory.BLOCKS;
    
    private static Configuration config;
    
    // Карты для конфигурации связей ID блоков/предметов с типами аномалий/артефактов
    private static final Map<String, String> BLOCK_TO_ANOMALY_CONFIG = new HashMap<>();
    private static final Map<String, String> ITEM_TO_ARTIFACT_CONFIG = new HashMap<>();
    private static final Map<String, String> ITEM_TO_DETECTOR_CONFIG = new HashMap<>();
    private static final Map<String, Boolean> RADIATION_DETECTOR_CONFIG = new HashMap<>();
    
    // Карта для хранения конфигурации эффектов аномалий
    public static final Map<AnomalyType, AnomalyEffectConfig> ANOMALY_EFFECTS_CONFIG = new HashMap<>();
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("S.T.A.L.K.E.R. Mod Lite Server предзагрузка...");
        
        // Загружаем конфигурацию
        File configFile = new File(event.getModConfigurationDirectory(), MODID + ".cfg");
        config = new Configuration(configFile);
        loadConfig();
        
        // Вызываем прокси для инициализации
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("S.T.A.L.K.E.R. Mod Lite Server инициализация...");
        
        // Инициализируем источники урона и эффекты
        DamageSourceRegistry.init();
        AnomalyEffects.init();
        
        // Инициализируем системы
        AnomalyRegistry.init();
        AnomalyManager.init();
        ArtifactManager.init();
        DetectorManager.init();
        RadiationManager.init();
        EmissionManager.init();
        
        // Регистрируем аномалии на основе конфигурации
        for (Map.Entry<String, String> entry : BLOCK_TO_ANOMALY_CONFIG.entrySet()) {
            AnomalyType type = AnomalyType.getByName(entry.getValue());
            if (type != null) {
                AnomalyManager.registerAnomalyBlock(entry.getKey(), type);
                logger.info("Зарегистрирован блок {} как аномалия типа {}. Конфигурация загружена успешно.", entry.getKey(), type.name());
            } else {
                logger.error("Не удалось зарегистрировать блок {} - неизвестный тип аномалии: {}", 
                    entry.getKey(), entry.getValue());
            }
        }
        
        // Регистрируем артефакты на основе конфигурации
        for (Map.Entry<String, String> entry : ITEM_TO_ARTIFACT_CONFIG.entrySet()) {
            ArtifactType type = ArtifactType.getByName(entry.getValue());
            if (type != null) {
                ArtifactManager.registerArtifact(entry.getKey(), type);
            }
        }
        
        // Регистрируем детекторы на основе конфигурации
        for (Map.Entry<String, String> entry : ITEM_TO_DETECTOR_CONFIG.entrySet()) {
            String[] parts = entry.getValue().split(":");
            if (parts.length >= 2) {
                DetectorType type = DetectorType.getByName(parts[0]);
                int radius = Integer.parseInt(parts[1]);
                if (type != null) {
                    DetectorManager.registerDetector(entry.getKey(), type, radius);
                }
            }
        }
        
        // Регистрируем детекторы радиации на основе конфигурации
        for (String itemId : RADIATION_DETECTOR_CONFIG.keySet()) {
            RadiationManager.registerRadiationDetector(itemId);
        }
        
        // Вызываем прокси для инициализации
        proxy.init(event);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        logger.info("Пост-инициализация " + NAME);
        
        if (config.hasChanged()) {
            config.save();
        }
        
        // Вызываем прокси для пост-инициализации
        proxy.postInit(event);
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        // Регистрация команд
        event.registerServerCommand(new CommandRadiation());
        event.registerServerCommand(new CommandSafeZone());
        event.registerServerCommand(new CommandEmission());
        
        logger.info("S.T.A.L.K.E.R. Mod Lite Server зарегистрировал серверные команды");
    }
    
    /**
     * Загрузка конфигурации мода
     */
    private void loadConfig() {
        // Загружаем конфигурацию связей блоков и аномалий
        String[] anomalyMappings = config.getStringList(
            "anomaly_mappings",
            "anomalies",
            new String[] {
                "minecraft:lava:electro",
                "minecraft:fire:thermal",
                "minecraft:cobweb:gravitational"
            },
            "Блоки, которые будут иметь эффекты аномалий (формат: modid:blockname:anomaly_type)"
        );
        
        for (String mapping : anomalyMappings) {
            String[] parts = mapping.split(":");
            if (parts.length >= 3) { 
                String blockId = parts[0] + ":" + parts[1]; 
                String anomalyType = parts[2];
                BLOCK_TO_ANOMALY_CONFIG.put(blockId, anomalyType);
            } else {
                logger.warn("Некорректная строка в anomaly_mappings: '{}'. Ожидаемый формат: modid:blockname:anomaly_type", mapping);
            }
        }
        
        // Загружаем конфигурацию эффектов аномалий
        for (AnomalyType type : AnomalyType.values()) {
            String section = "anomalies." + type.name().toLowerCase();
            float damageAmount = config.getFloat("damageAmount", section, 10.0f, 0.0f, 100.0f, "Количество урона для аномалии " + type.name());
            String potionEffect = config.getString("potionEffect", section, "", "Эффект зелья для аномалии " + type.name() + " (формат: effect;duration;level)");
            ANOMALY_EFFECTS_CONFIG.put(type, new AnomalyEffectConfig(damageAmount, potionEffect));
        }
        
        // Загружаем конфигурацию связей предметов и артефактов
        String[] artifactMappings = config.getStringList(
            "artifact_mappings",
            "artifacts",
            new String[] {
                "minecraft:nether_star:medusa",
                "minecraft:ghast_tear:soul",
                "minecraft:blaze_powder:fireball"
            },
            "Предметы, которые будут иметь эффекты артефактов (формат: modid:itemname:artifact_type)"
        );
        
        for (String mapping : artifactMappings) {
            String[] parts = mapping.split(":");
            if (parts.length >= 3) {
                String itemId = parts[0] + ":" + parts[1];
                String artifactType = parts[2];
                ITEM_TO_ARTIFACT_CONFIG.put(itemId, artifactType);
            } else {
                logger.warn("Некорректная строка в artifact_mappings: '{}'. Ожидаемый формат: modid:itemname:artifact_type", mapping);
            }
        }
        
        // Загружаем конфигурацию связей предметов и детекторов
        String[] detectorMappings = config.getStringList(
            "detector_mappings",
            "detectors",
            new String[] {
                "minecraft:compass:anomaly:10",
                "minecraft:clock:radiation:0",
                "minecraft:name_tag:artifact:5"
            },
            "Предметы, которые будут работать как детекторы (формат: modid:itemname:detector_type:radius)"
        );
        
        for (String mapping : detectorMappings) {
            String[] parts = mapping.split(":");
            if (parts.length >= 4) {
                String itemId = parts[0] + ":" + parts[1];
                String detectorType = parts[2];
                String radiusStr = parts[3];
                
                try {
                    ITEM_TO_DETECTOR_CONFIG.put(itemId, detectorType + ":" + radiusStr);
                    Integer.parseInt(radiusStr);
                } catch (NumberFormatException e) {
                    logger.warn("Некорректный радиус в detector_mappings: '{}'. Радиус должен быть числом.", mapping);
                }
            } else {
                logger.warn("Некорректная строка в detector_mappings: '{}'. Ожидаемый формат: modid:itemname:detector_type:radius", mapping);
            }
        }
        
        // Загружаем конфигурацию детекторов радиации
        String[] radiationDetectorMappings = config.getStringList(
            "radiation_detector_mappings",
            "detectors",
            new String[] {
                "minecraft:compass",
                "minecraft:clock"
            },
            "Предметы, которые будут работать как детекторы радиации (формат: modid:itemname)"
        );
        
        for (String itemIdMapping : radiationDetectorMappings) {
            if (itemIdMapping != null && !itemIdMapping.trim().isEmpty() && itemIdMapping.contains(":")) {
                String[] parts = itemIdMapping.split(":");
                if (parts.length >= 2) {
                    RADIATION_DETECTOR_CONFIG.put(itemIdMapping.trim(), true);
                } else {
                    logger.warn("Некорректная строка в radiation_detector_mappings: '{}'. Ожидаемый формат: modid:itemname", itemIdMapping);
                }
            } else {
                logger.warn("Некорректная или пустая строка в radiation_detector_mappings: '{}'", itemIdMapping);
            }
        }
        
        if (config.hasChanged()) {
            config.save();
        }
    }
} 