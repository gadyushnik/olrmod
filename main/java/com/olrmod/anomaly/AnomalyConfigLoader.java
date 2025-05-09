package com.olrmod.anomaly;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.common.Loader;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AnomalyConfigLoader {

    private static final List<AnomalyDefinition> definitions = new ArrayList<>();

    public static void load() {
        File file = new File(Loader.instance().getConfigDir(), "olrmod/anomalies.json");

        if (!file.exists()) {
            System.err.println("[AnomalyConfigLoader] Файл конфигурации не найден: " + file.getAbsolutePath());
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<List<AnomalyDefinition>>() {}.getType();
            List<AnomalyDefinition> loaded = new Gson().fromJson(reader, type);

            if (loaded != null) {
                definitions.clear();
                definitions.addAll(loaded);
                System.out.println("[AnomalyConfigLoader] Загружено аномалий: " + definitions.size());
            } else {
                System.err.println("[AnomalyConfigLoader] Конфигурация пуста или повреждена.");
            }

        } catch (Exception e) {
            System.err.println("[AnomalyConfigLoader] Ошибка при чтении конфигурации: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<AnomalyDefinition> getDefinitions() {
        return definitions;
    }

    public static AnomalyDefinition getByBlockId(String id) {
        for (AnomalyDefinition def : definitions) {
            if (def.blockIds.contains(id)) return def;
        }
        return null;
    }
}
