package com.olrmod.artifacts;

import com.olrmod.effects.EffectStageManager;
import com.olrmod.effects.EffectStageManager.EffectType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ArtifactEffectApplier {

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        if (player.ticksExisted % 40 != 0) return; // Каждые 2 секунды

        for (ItemStack stack : player.inventory.mainInventory) {
            ArtifactEffectRegistry.getByItem(stack).ifPresent(data -> {
                for (ArtifactData.EffectEntry effect : data.getEffects()) {
                    try {
                        EffectType type = EffectType.valueOf(effect.getType().toUpperCase());
                        EffectStageManager.addEffect(player, type, effect.getAmount());
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            });
        }
    }
}
