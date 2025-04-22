package com.olrmod.armor;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class ArmorTooltipHandler {

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack == null || stack.isEmpty()) return;

        ArmorResistanceData data = ArmorData.get(stack.getItem().getRegistryName().toString());
        if (data == null || data.effects.isEmpty()) return;

        List<String> tooltip = event.getToolTip();
        tooltip.add("§9Эффекты брони:");
        for (String effect : data.effects.keySet()) {
            tooltip.add("§7• " + effect.toLowerCase());
        }
    }
}
