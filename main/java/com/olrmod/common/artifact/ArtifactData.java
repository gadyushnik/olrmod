package com.olrmod.artifacts;

import java.util.List;
import net.minecraft.util.ResourceLocation;

public class ArtifactData {
    private String artifactId;
    private ResourceLocation itemId;
    private List<EffectEntry> effects;
    private int spawnChance;
    private float weightBonus;

    public String getArtifactId() {
        return artifactId;
    }

    public ResourceLocation getItemId() {
        return itemId;
    }

    public List<EffectEntry> getEffects() {
        return effects;
    }

    public int getSpawnChance() {
        return spawnChance;
    }

    public float getWeightBonus() {
        return weightBonus;
    }

    public static class EffectEntry {
        private String type;
        private int amount;

        public String getType() {
            return type;
        }

        public int getAmount() {
            return amount;
        }
    }
}
