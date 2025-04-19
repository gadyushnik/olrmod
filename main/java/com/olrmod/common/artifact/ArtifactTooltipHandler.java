package com.olrmod.artifacts;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ArtifactTooltipHandler {

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        ArtifactEffectRegistry.getByItem(stack).ifPresent(data -> {
            event.getToolTip().add(TextFormatting.GOLD + "Артефакт: " + data.getArtifactId());
            for (ArtifactData.EffectEntry effect : data.getEffects()) {
                String sign = effect.getAmount() > 0 ? "+" : "";
                event.getToolTip().add(" " + sign + effect.getAmount() + " " + effect.getType());
            }
        });
    }
}
