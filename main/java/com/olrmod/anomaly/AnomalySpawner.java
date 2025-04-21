package com.olrmod.anomaly;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.*;

public class AnomalySpawner {

    private static final List<BlockPos> activeAnomalies = new ArrayList<>();
    private static final Map<String, Integer> biomeSpawnCount = new HashMap<>();

    public static void clearAnomalies() {
        activeAnomalies.clear();
        biomeSpawnCount.clear();
    }

    public static void spawnAnomalies(World world) {
        clearAnomalies();
        Random rand = new Random();

        int radius = 128;
        int attempts = 500;

        for (int i = 0; i < attempts; i++) {
            int x = rand.nextInt(radius * 2) - radius;
            int z = rand.nextInt(radius * 2) - radius;
            int y = world.getTopSolidOrLiquidBlock(new BlockPos(x, 64, z)).getY();
            BlockPos pos = new BlockPos(x, y, z);

            Biome biome = world.getBiome(pos);
            String biomeName = biome.getRegistryName().getPath();

            for (AnomalyDefinition def : AnomalyConfigLoader.getDefinitions()) {
                if (!def.biomes.contains(biomeName)) continue;

                biomeSpawnCount.putIfAbsent(def.anomalyId, 0);
                if (biomeSpawnCount.get(def.anomalyId) >= 3) continue; // лимит 3 на тип в биоме

                if (rand.nextDouble() <= def.spawnChance) {
                    // Здесь ты можешь разместить нужный блок через world.setBlockState(pos, ...);
                    activeAnomalies.add(pos);
                    biomeSpawnCount.put(def.anomalyId, biomeSpawnCount.get(def.anomalyId) + 1);
                }
            }
        }
    }

    public static double getClosestAnomalyDistance(BlockPos pos) {
        return activeAnomalies.stream()
                .mapToDouble(p -> p.distanceSq(pos))
                .min()
                .map(Math::sqrt)
                .orElse(Double.POSITIVE_INFINITY);
    }

    public static List<BlockPos> getAllAnomalies() {
        return activeAnomalies;
    }
}
