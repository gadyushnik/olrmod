package com.olrmod.proxy;

import com.olrmod.StalkerMod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Серверная реализация прокси
 */
public class ServerProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        StalkerMod.logger.info("Инициализация серверной части мода");
    }
    
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }
    
    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }
} 