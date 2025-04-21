package com.olrmod.emission;

import com.olrmod.anomaly.AnomalySpawner;
import com.olrmod.artifacts.ArtifactData;
import com.olrmod.artifacts.ArtifactType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Random;

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

            World world = event.getEntityLiving().world;

            AnomalySpawner.spawnAnomalies(world);
            spawnArtifactsInAnomalies(world);

            world.getMinecraftServer()
                 .getPlayerList()
                 .sendMessage(new TextComponentString("Выброс завершён. Аномалии переродились."));
        }
    }

    private void spawnArtifactsInAnomalies(World world) {
        Random rand = new Random();

        for (BlockPos pos : AnomalySpawner.getAllAnomalies()) {
            if (rand.nextDouble() < 0.3) {
                ArtifactType type = ArtifactData.getRandomArtifact(rand);
                if (type == null) continue;

                Item item = ForgeRegistries.ITEMS.getValue(type.itemId);
                if (item == null) continue;

                ItemStack stack = new ItemStack(item);
                EntityItem entity = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, stack);
                world.spawnEntity(entity);
            }
        }
    }
}
