package com.olrmod.artifacts;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

public class ArtifactEffectRegistry {
    private static final Gson GSON = new Gson();
    private static final List<ArtifactData> artifacts = new ArrayList<>();

    public static void loadArtifacts() {
        artifacts.clear();
        try {
            InputStreamReader reader = new InputStreamReader(
                ArtifactEffectRegistry.class.getClassLoader().getResourceAsStream("assets/olrmod/data/artifacts/artifacts.json")
            );
            Type type = new TypeToken<List<ArtifactData>>() {}.getType();
            List<ArtifactData> loaded = GSON.fromJson(reader, type);
            if (loaded != null) artifacts.addAll(loaded);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки артефактов: " + e.getMessage());
        }
    }

    public static Optional<ArtifactData> getByItem(ItemStack stack) {
        ResourceLocation id = stack.getItem().getRegistryName();
        if (id == null) return Optional.empty();

        for (ArtifactData data : artifacts) {
            if (id.equals(data.getItemId())) {
                return Optional.of(data);
            }
        }
        return Optional.empty();
    }

    public static List<ArtifactData> getArtifacts() {
        return artifacts;
    }
}
