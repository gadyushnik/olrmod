package com.olrmod.anomaly;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AnomalySpawner {

    private static final List<BlockPos> activeAnomalies = new ArrayList<>();

    public static void clearAnomalies() {
        activeAnomalies.clear();
    }

    public static void spawnAnomalies(World world) {
        clearAnomalies();
        Random rand = new Random();

        int radius = 128; // примерный радиус от спавна
        int count = 20;   // примерное количество аномалий

        for (int i = 0; i < count; i++) {
            int x = rand.nextInt(radius * 2) - radius;
            int z = rand.nextInt(radius * 2) - radius;
            int y = world.getTopSolidOrLiquidBlock(new BlockPos(x, 64, z)).getY();

            BlockPos pos = new BlockPos(x, y, z);
            activeAnomalies.add(pos);

            // можно добавить реальный блок в мир или другой эффект
        }
    }

    public static double getClosestAnomalyDistance(BlockPos pos) {
        double closest = Double.MAX_VALUE;
        for (BlockPos anomaly : activeAnomalies) {
            double dist = anomaly.distanceSq(pos);
            if (dist < closest) {
                closest = dist;
            }
        }
        return Math.sqrt(closest);
    }

    public static List<BlockPos> getAllAnomalies() {
        return activeAnomalies;
    }
}
