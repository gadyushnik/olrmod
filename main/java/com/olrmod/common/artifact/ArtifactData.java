package com.olrmod.artifacts;

import com.olrmod.effects.EffectStageManager.EffectType;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class ArtifactData {
    private ResourceLocation itemId;
    private List<EffectType> effects;
    private int spawnChance;
    private float weightBonus;

    public ResourceLocation getItemId() {
        return itemId;
    }

    public List<EffectType> getEffects() {
        return effects;
    }

    public int getSpawnChance() {
        return spawnChance;
    }

    public float getWeightBonus() {
        return weightBonus;
    }
}
