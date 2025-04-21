package com.olrmod;

import com.olrmod.anomaly.AnomalyLogicHandler;
import com.olrmod.anomaly.AnomalyConfigLoader;
import com.olrmod.artifacts.ArtifactData;
import com.olrmod.artifacts.ArtifactEffectApplier;
import com.olrmod.artifacts.ArtifactTooltipHandler;
import com.olrmod.detectors.DetectorUpdateHandler;
import com.olrmod.effects.EffectDamageHandler;
import com.olrmod.emission.EmissionManager;
import com.olrmod.radiation.RadiationZoneConfig;
import com.olrmod.radiation.RadiationZoneManager;
import com.olrmod.safezone.SafeZoneManager;
import com.olrmod.weight.WeightManager;
import com.olrmod.armor.ArmorData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = StalkerMod.MODID, name = StalkerMod.NAME, version = StalkerMod.VERSION)
public class StalkerMod {
    public static final String MODID = "olrmod";
    public static final String NAME = "Stalker Mod";
    public static final String VERSION = "1.0";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // === Регистрация обработчиков ===
        MinecraftForge.EVENT_BUS.register(new EmissionManager());
        MinecraftForge.EVENT_BUS.register(new AnomalyLogicHandler());
        MinecraftForge.EVENT_BUS.register(new EffectDamageHandler());
        MinecraftForge.EVENT_BUS.register(new DetectorUpdateHandler());
        MinecraftForge.EVENT_BUS.register(new ArtifactEffectApplier());
        MinecraftForge.EVENT_BUS.register(new ArtifactTooltipHandler());
        MinecraftForge.EVENT_BUS.register(new RadiationZoneManager());
        MinecraftForge.EVENT_BUS.register(new SafeZoneManager());

        // === Загрузка конфигов ===
        ArtifactData.load();
        WeightManager.loadWeights(Loader.instance().getConfigDir());
        AnomalyConfigLoader.load();
        RadiationZoneConfig.load();
        ArmorData.load(); // Загрузка конфигурации брони
    }
}
