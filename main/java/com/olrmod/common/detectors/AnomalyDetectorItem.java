package com.olrmod.detectors;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class AnomalyDetectorItem extends Item {
    public AnomalyDetectorItem() {
        setMaxStackSize(1);
        setUnlocalizedName("anomaly_detector");
        setRegistryName("anomaly_detector");
    }

    public static boolean isHeld(ItemStack stack) {
        return stack != null && stack.getItem() instanceof AnomalyDetectorItem;
    }
}
