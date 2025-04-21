package com.olrmod.emission;

import com.olrmod.anomaly.AnomalySpawner;
import com.olrmod.artifacts.ArtifactSpawner;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EmissionManager {

    private static boolean active = false;
    private static int timer = 0;

    public static void startEmission(MinecraftServer server) {
        active = true;
        timer = 200; // Длительность выброса в тиках
        server.getPlayerList().sendMessage(new TextComponentString("Начался выброс! Ищите укрытие."));
    }

    @SubscribeEvent
    public void onPlayerUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!active || event.getEntityLiving().world.isRemote) return;

        if (--timer <= 0) {
            active = false;

            // Генерация аномалий
            AnomalySpawner.spawnAnomalies(event.getEntityLiving().world);

            // Генерация артефактов
            ArtifactSpawner.spawnInAnomalies(event.getEntityLiving().world);

            event.getEntityLiving().world.getMinecraftServer()
                .getPlayerList()
                .sendMessage(new TextComponentString("Выброс завершён. Аномалии переродились."));
        }
    }
}
