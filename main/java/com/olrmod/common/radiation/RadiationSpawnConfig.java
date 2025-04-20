package com.olrmod.radiation;

import com.google.gson.Gson;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Loader;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class RadiationSpawnConfig {
    public static class SpawnSettings {
        public float chance;
        public int max;
    }

    public static class BiomeSettings {
        public Map<String, SpawnSettings> levels = new HashMap<>();
    }

    private static final Map<String, BiomeSettings> CONFIG = new HashMap<>();

    public static void load() {
        File file = new File(new File(Loader.instance().getConfigDir(), "olrmod"), "radiation_spawn.json");
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Map<String, BiomeSettings> loaded = gson.fromJson(reader, CONFIG.getClass());
            if (loaded != null) CONFIG.putAll(loaded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SpawnSettings getSettings(Biome biome, RadiationZone.ZoneType type) {
        BiomeSettings settings = CONFIG.get(biome.getRegistryName().getPath());
        if (settings == null) return null;
        return settings.levels.get(type.name());
    }
}
