package com.olrmod.effects;

import com.olrmod.effects.EffectStageManager.EffectType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EffectDamageHandler {

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayerMP)) return;

        EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();

        if (player.ticksExisted % 40 != 0) return;

        for (EffectType type : EffectType.values()) {
            int stage = EffectStageManager.getStage(player, type);
            if (stage > 0) {
                float damage = stage;
                switch (type) {
                    case RADIATION:
                        player.attackEntityFrom(ModDamageSources.RADIATION, damage);
                        break;
                    case CHEMICAL:
                        player.attackEntityFrom(ModDamageSources.CHEMICAL, damage);
                        break;
                    case GRAVITATIONAL:
                        player.attackEntityFrom(ModDamageSources.GRAVITATIONAL, damage);
                        break;
                    case PSY:
                        player.attackEntityFrom(ModDamageSources.PSY, damage);
                        break;
                }
            }
        }
    }
}
