package com.olrmod.artifacts;

import java.util.List;
import net.minecraft.util.ResourceLocation;

public class ArtifactData {
    private String artifactId;
    private ResourceLocation itemId;
    private List<EffectEntry> effects;
    private int spawnChance;
    private float weightBonus;

    // Getters
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

    // Setters
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public void setItemId(ResourceLocation itemId) {
        this.itemId = itemId;
    }

    public void setEffects(List<EffectEntry> effects) {
        this.effects = effects;
    }

    public void setSpawnChance(int spawnChance) {
        this.spawnChance = spawnChance;
    }

    public void setWeightBonus(float weightBonus) {
        this.weightBonus = weightBonus;
    }

    public static class EffectEntry {
        private String type;
        private int amount;

        // Getters
        public String getType() {
            return type;
        }

        public int getAmount() {
            return amount;
        }

        // Setters
        public void setType(String type) {
            this.type = type;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }
}
