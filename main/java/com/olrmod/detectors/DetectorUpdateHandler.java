package com.olrmod.detectors;

import com.olrmod.effects.EffectStageManager;
import com.olrmod.effects.EffectStageManager.EffectType;
import com.olrmod.anomaly.AnomalySpawner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DetectorUpdateHandler {

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        if (player.ticksExisted % 40 != 0) return;

        ItemStack main = player.getHeldItemMainhand();
        ItemStack off = player.getHeldItemOffhand();

        // Geiger
        if (GeigerDetectorItem.isHeld(main) || GeigerDetectorItem.isHeld(off)) {
            int stage = EffectStageManager.getStage(player, EffectType.RADIATION);
            if (stage > 0) {
                SoundEvents sound = SoundEvents.BLOCK_NOTE_HAT;
                float pitch = 0.5f + stage * 0.5f;
                player.world.playSound(null, player.getPosition(), sound, SoundCategory.PLAYERS, 1.0f, pitch);
            }
        }

        // Anomaly
        if (AnomalyDetectorItem.isHeld(main) || AnomalyDetectorItem.isHeld(off)) {
            double closest = AnomalySpawner.getClosestAnomalyDistance(player.getPosition());

            if (closest < 15) {
                float pitch = (float) (1.5 - (closest / 15));
                player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_NOTE_SNARE, SoundCategory.PLAYERS, 1.0f, pitch);
            }
        }
    }
}
