package com.olrmod.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Базовый класс прокси для общего кода клиента и сервера
 */
public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        // Общие действия перед инициализацией
    }
    
    public void init(FMLInitializationEvent event) {
        // Общие действия при инициализации
    }
    
    public void postInit(FMLPostInitializationEvent event) {
        // Общие действия после инициализации
    }
} 