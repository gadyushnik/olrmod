package com.olrmod;

import com.olrmod.artifacts.ArtifactEffectRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = StalkerMod.MODID, name = StalkerMod.NAME, version = StalkerMod.VERSION)
public class StalkerMod {
    public static final String MODID = "olrmod";
    public static final String NAME = "Stalker Mod";
    public static final String VERSION = "1.0";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModEventRegistrar.register();
        ArtifactEffectRegistry.loadArtifacts();
    }
}
