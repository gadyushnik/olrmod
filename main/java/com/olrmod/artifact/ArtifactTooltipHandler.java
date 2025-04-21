package com.olrmod.artifacts;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class ArtifactTooltipHandler {

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack == null || stack.isEmpty()) return;

        ArtifactType type = ArtifactData.get(stack.getItem().getRegistryName().toString());
        if (type == null) return;

        List<String> tooltip = event.getToolTip();
        tooltip.add("§7Артефакт: " + type.id);

        if (!type.effects.isEmpty()) {
            tooltip.add("§8Эффекты:");
            type.effects.forEach((effect, value) -> {
                String sign = value > 0 ? "+" : "";
                tooltip.add("§6 " + sign + value + " " + effect.toLowerCase());
            });
        }

        if (type.weightModifier != 0) {
            String prefix = type.weightModifier > 0 ? "+" : "";
            tooltip.add("§bВес: " + prefix + type.weightModifier);
        }

        if (!type.resistanceBonus.isEmpty()) {
            tooltip.add("§9Сопротивления:");
            type.resistanceBonus.forEach((typeKey, value) -> {
                tooltip.add("§a " + typeKey.toLowerCase() + ": +" + value);
            });
        }
    }
}
