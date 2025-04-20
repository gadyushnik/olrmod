package com.olrmod;

import com.olrmod.anomaly.AnomalyLogicHandler;
import com.olrmod.artifacts.ArtifactEffectApplier;
import com.olrmod.artifacts.ArtifactEffectRegistry;
import com.olrmod.artifacts.ArtifactTooltipHandler;
import com.olrmod.emission.EmissionManager;
import com.olrmod.weight.WeightManager;
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
        // Регистрируем обработчики
        MinecraftForge.EVENT_BUS.register(new EmissionManager());
        MinecraftForge.EVENT_BUS.register(new AnomalyLogicHandler());
        MinecraftForge.EVENT_BUS.register(new ArtifactEffectApplier());
        MinecraftForge.EVENT_BUS.register(new ArtifactTooltipHandler());

        // Загружаем JSON-конфиги
        ArtifactEffectRegistry.loadArtifacts();
        WeightManager.loadWeights(Loader.instance().getConfigDir());
    }
}
