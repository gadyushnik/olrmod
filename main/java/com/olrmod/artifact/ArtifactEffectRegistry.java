package com.olrmod.artifacts;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ArtifactEffectRegistry {
    private static final Gson GSON = new Gson();
    private static final List<ArtifactData> artifacts = new ArrayList<>();

    public static void loadArtifacts() {
        // Читаем конфиг из конфиг-папки: <configDir>/olrmod/artifacts.json
        File configDir = Loader.instance().getConfigDir();
        File file = new File(new File(configDir, "olrmod"), "artifacts.json");
        if (!file.exists()) {
            createDefaultArtifactConfig(file);
        }
        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<List<ArtifactData>>() {}.getType();
            List<ArtifactData> loaded = GSON.fromJson(reader, type);
            artifacts.clear();
            if (loaded != null) {
                artifacts.addAll(loaded);
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки артефактов: " + e.getMessage());
        }
    }

    private static void createDefaultArtifactConfig(File file) {
        file.getParentFile().mkdirs();
        List<ArtifactData> defaultArtifacts = new ArrayList<>();
        // Пример: артефакт "toad_eye" назначается на minecraft:flint
        ArtifactData data = new ArtifactData();
        data.setArtifactId("toad_eye");
        data.setItemId(new ResourceLocation("minecraft", "flint"));
        List<ArtifactData.EffectEntry> effects = new ArrayList<>();
        ArtifactData.EffectEntry eff1 = new ArtifactData.EffectEntry();
        eff1.setType("CHEMICAL");
        eff1.setAmount(2);
        effects.add(eff1);
        ArtifactData.EffectEntry eff2 = new ArtifactData.EffectEntry();
        eff2.setType("RADIATION");
        eff2.setAmount(-1);
        effects.add(eff2);
        data.setEffects(effects);
        data.setSpawnChance(15);
        data.setWeightBonus(0.0f);
        defaultArtifacts.add(data);
        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(defaultArtifacts, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<ArtifactData> getArtifacts() {
        return artifacts;
    }

    public static java.util.Optional<ArtifactData> getByItem(net.minecraft.item.ItemStack stack) {
        ResourceLocation id = stack.getItem().getRegistryName();
        if (id == null) return java.util.Optional.empty();

        for (ArtifactData data : artifacts) {
            if (id.equals(data.getItemId())) {
                return java.util.Optional.of(data);
            }
        }
        return java.util.Optional.empty();
    }
}
