package com.olrmod.radiation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.common.Loader;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RadiationSpawnConfig {

    public static class SpawnData {
        public float chance;
        public int max;
    }

    public static class BiomeSpawn {
        public Map<String, SpawnData> levels;
    }

    private static final Gson GSON = new Gson();
    private static final Map<String, BiomeSpawn> CONFIG = new HashMap<>();

    public static void load() {
        File file = new File(Loader.instance().getConfigDir(), "olrmod/radiation_spawn.json");

        if (!file.exists()) {
            System.err.println("[RadiationSpawnConfig] Файл конфигурации не найден: " + file.getAbsolutePath());
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, BiomeSpawn>>() {}.getType();
            Map<String, BiomeSpawn> loaded = GSON.fromJson(reader, type);

            if (loaded != null) {
                CONFIG.clear();
                CONFIG.putAll(loaded);
                System.out.println("[RadiationSpawnConfig] Загружены конфигурации для " + CONFIG.size() + " биомов.");
            } else {
                System.err.println("[RadiationSpawnConfig] Конфигурация пуста или недействительна.");
            }

        } catch (Exception e) {
            System.err.println("[RadiationSpawnConfig] Ошибка при загрузке конфигурации: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static SpawnData get(String biome, RadiationZone.ZoneType type) {
        BiomeSpawn spawn = CONFIG.get(biome);
        return (spawn != null && spawn.levels != null) ? spawn.levels.get(type.name()) : null;
    }
}
