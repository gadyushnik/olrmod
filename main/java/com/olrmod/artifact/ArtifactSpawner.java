package com.olrmod.artifacts;

import com.olrmod.anomaly.AnomalySpawner;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class ArtifactSpawner {

    public static void spawnInAnomalies(World world) {
        Random rand = new Random();

        for (BlockPos pos : AnomalySpawner.getAllAnomalies()) {
            if (rand.nextDouble() < 0.3) { // шанс на артефакт в аномалии
                ItemStack artifact = new ItemStack(Items.DIAMOND); // временно — замени на свой предмет
                artifact.setStackDisplayName("§bАртефакт");
                world.spawnEntity(new net.minecraft.entity.item.EntityItem(world, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, artifact));
            }
        }
    }
}
