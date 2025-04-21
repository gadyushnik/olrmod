package com.olrmod.armor;

import java.util.Map;
import net.minecraft.util.ResourceLocation;

public class ArmorData {
    private ResourceLocation itemId;
    private Map<String, Float> resistances;

    public ResourceLocation getItemId() {
        return itemId;
    }

    public Map<String, Float> getResistances() {
        return resistances;
    }
}
