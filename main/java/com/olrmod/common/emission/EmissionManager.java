package com.olrmod.emission;

import com.olrmod.effects.EffectStageManager;
import com.olrmod.effects.EffectStageManager.EffectType;
import com.olrmod.anomaly.AnomalySpawner;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EmissionManager {
    private static final int INTERVAL_TICKS = 20 * 60 * 10; // 10 мин
    private static final int DURATION_TICKS = 20 * 30; // 30 сек

    private static int tickCounter = 0;
    private static boolean emissionActive = false;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tickCounter++;

        if (!emissionActive && tickCounter >= INTERVAL_TICKS) {
            startEmission();
        }

        if (emissionActive && tickCounter >= INTERVAL_TICKS + DURATION_TICKS) {
            stopEmission();
        }
    }

    private void startEmission() {
        emissionActive = true;
        tickCounter = INTERVAL_TICKS;

        broadcast("§c[Зона] Начался выброс! Срочно найдите укрытие!");
    }

    private void stopEmission() {
        emissionActive = false;
        tickCounter = 0;

        broadcast("§a[Зона] Выброс завершён.");

        World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
        AnomalySpawner.clearOldAnomalies(world);
        AnomalySpawner.spawnNewAnomalies(world);
    }

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        if (!emissionActive) return;
        if (!(event.getEntityLiving() instanceof EntityPlayerMP)) return;

        EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();

        if (!SafeZoneManager.isInSafeZone(player)) {
            EffectStageManager.addEffect(player, EffectType.RADIATION, 4);
        }
    }

    private void broadcast(String msg) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null) {
            for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                player.sendMessage(new TextComponentString(msg));
            }
        }
    }
}
