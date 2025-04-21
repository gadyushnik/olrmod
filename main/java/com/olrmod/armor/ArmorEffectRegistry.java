package com.olrmod.armor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class ArmorEffectRegistry {
    private static final Gson GSON = new Gson();
    private static final List<ArmorData> armors = new ArrayList<>();

    public static void load() {
        File file = new File(new File(Loader.instance().getConfigDir(), "olrmod"), "armor.json");
        if (!file.exists()) {
            generateDefault(file);
        }

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<List<ArmorData>>() {}.getType();
            List<ArmorData> loaded = GSON.fromJson(reader, type);
            armors.clear();
            if (loaded != null) armors.addAll(loaded);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки резистов брони: " + e.getMessage());
        }
    }

    private static void generateDefault(File file) {
        file.getParentFile().mkdirs();
        ArmorData armor = new ArmorData();
        armor.itemId = new ResourceLocation("visualmod", "stalker_suit");
        Map<String, Float> resist = new HashMap<>();
        resist.put("RADIATION", 0.8f);
        resist.put("CHEMICAL", 0.6f);
        resist.put("GRAVITATIONAL", 0.4f);
        armor.resistances = resist;

        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(Collections.singletonList(armor), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static float getResistance(ResourceLocation itemId, String type, float durabilityFactor) {
        for (ArmorData data : armors) {
            if (itemId.equals(data.getItemId())) {
                Float base = data.getResistances().get(type.toUpperCase());
                if (base != null) {
                    return base * durabilityFactor;
                }
            }
        }
        return 0f;
    }
}
