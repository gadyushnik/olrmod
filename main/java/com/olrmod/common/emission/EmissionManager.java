package com.olrmod.emission;

import com.olrmod.effects.EffectStageManager;
import com.olrmod.effects.EffectStageManager.EffectType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EmissionManager {
    private static int timer = 0;
    private static boolean active = false;
    private static final int INTERVAL = 20 * 60 * 10;
    private static final int DURATION = 20 * 30;

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        timer++;

        if (!active && timer >= INTERVAL) {
            startEmission();
        }

        if (active && timer >= INTERVAL + DURATION) {
            stopEmission();
        }
    }

    private void startEmission() {
        active = true;
        timer = INTERVAL;
        broadcast("§c[Зона] Начался выброс! Срочно найдите укрытие!");
    }

    private void stopEmission() {
        active = false;
        timer = 0;
        broadcast("§a[Зона] Выброс завершён.");
    }

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        if (!active) return;
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
