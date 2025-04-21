package com.olrmod.weight;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.common.Loader;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class WeightManager {
    private static final Gson GSON = new Gson();

    private static float weightLow = 30;
    private static float weightNormal = 40;
    private static float weightFull = 50;

    private static Map<String, Float> itemWeights = new HashMap<>();

    public static void loadWeights(File configDir) {
        File file = new File(new File(configDir, "olrmod"), "weights.json");

        if (!file.exists()) {
            System.err.println("[WeightManager] Конфиг не найден, создаю по умолчанию...");
            createDefaultConfig(file);
        }

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<WeightConfig>(){}.getType();
            WeightConfig config = GSON.fromJson(reader, type);

            if (config != null) {
                weightLow = config.weightLow;
                weightNormal = config.weightNormal;
                weightFull = config.weightFull;
                itemWeights = config.itemWeights != null ? config.itemWeights : new HashMap<>();
                System.out.println("[WeightManager] Загружены веса предметов: " + itemWeights.size());
            } else {
                System.err.println("[WeightManager] Конфигурация пустая или повреждена.");
            }

        } catch (Exception e) {
            System.err.println("[WeightManager] Ошибка при загрузке конфигурации веса: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createDefaultConfig(File file) {
        file.getParentFile().mkdirs();
        WeightConfig defaultConfig = new WeightConfig();
        defaultConfig.weightLow = 30;
        defaultConfig.weightNormal = 40;
        defaultConfig.weightFull = 50;
        defaultConfig.itemWeights = new HashMap<>();
        defaultConfig.itemWeights.put("minecraft:flint", 0.5f);
        defaultConfig.itemWeights.put("minecraft:slime_ball", 0.1f);

        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(defaultConfig, writer);
            System.out.println("[WeightManager] Создан дефолтный конфиг веса.");
        } catch (IOException e) {
            System.err.println("[WeightManager] Ошибка при создании конфигурации по умолчанию: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static float getWeightLow() {
        return weightLow;
    }

    public static float getWeightNormal() {
        return weightNormal;
    }

    public static float getWeightFull() {
        return weightFull;
    }

    public static Map<String, Float> getItemWeights() {
        return itemWeights;
    }

    public static class WeightConfig {
        float weightLow;
        float weightNormal;
        float weightFull;
        Map<String, Float> itemWeights;
    }
}
