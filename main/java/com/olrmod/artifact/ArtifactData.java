package com.olrmod.artifacts;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Loader;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class ArtifactData {
    private static final Map<String, ArtifactType> ARTIFACTS = new HashMap<>();

    public static void load() {
        File file = new File(Loader.instance().getConfigDir(), "olrmod/artifacts.json");
        if (!file.exists()) {
            System.err.println("[ArtifactData] Файл конфигурации не найден: " + file.getAbsolutePath());
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, ArtifactType>>() {}.getType();
            Map<String, ArtifactType> loaded = new Gson().fromJson(reader, type);
            if (loaded != null) {
                ARTIFACTS.clear();
                ARTIFACTS.putAll(loaded);
                System.out.println("[ArtifactData] Загружено артефактов: " + ARTIFACTS.size());
            } else {
                System.err.println("[ArtifactData] Не удалось загрузить артефакты: JSON пуст");
            }
        } catch (IOException e) {
            System.err.println("[ArtifactData] Ошибка при чтении конфигурации: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ArtifactData] Непредвиденная ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static ArtifactType get(String id) {
        return ARTIFACTS.get(id);
    }

    public static Collection<ArtifactType> getAll() {
        return ARTIFACTS.values();
    }

    public static ArtifactType getRandomArtifact(Random rand) {
        double totalWeight = 0;
        for (ArtifactType type : ARTIFACTS.values()) {
            totalWeight += type.spawnChance;
        }

        double r = rand.nextDouble() * totalWeight;
        for (ArtifactType type : ARTIFACTS.values()) {
            r -= type.spawnChance;
            if (r <= 0) return type;
        }

        return null;
    }
}
