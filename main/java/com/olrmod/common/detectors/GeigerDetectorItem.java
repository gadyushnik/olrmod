package com.olrmod.detectors;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GeigerDetectorItem extends Item {
    public GeigerDetectorItem() {
        setMaxStackSize(1);
        setUnlocalizedName("geiger_detector");
        setRegistryName("geiger_detector");
    }

    public static boolean isHeld(ItemStack stack) {
        return stack != null && stack.getItem() instanceof GeigerDetectorItem;
    }
}
