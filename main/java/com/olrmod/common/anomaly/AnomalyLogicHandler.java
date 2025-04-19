package com.olrmod.anomaly;

import com.olrmod.effects.EffectStageManager;
import com.olrmod.effects.EffectStageManager.EffectType;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class AnomalyLogicHandler {

    private static final int EFFECT_INTERVAL = 20;

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        World world = player.world;

        if (player.ticksExisted % EFFECT_INTERVAL != 0) return;

        BlockPos pos = player.getPosition();
        AxisAlignedBB box = new AxisAlignedBB(pos).grow(1.0D);

        for (BlockPos checkPos : BlockPos.getAllInBoxMutable(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)) {
            Block block = world.getBlockState(checkPos).getBlock();
            String id = block.getRegistryName().toString();

            switch (id) {
                case "visualmod:anomaly_burning_fuzz":
                    EffectStageManager.addEffect(player, EffectType.CHEMICAL, 2);
                    world.playSound(null, checkPos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.4F, 1F);
                    world.spawnParticle(EnumParticleTypes.FLAME, checkPos.getX() + 0.5, checkPos.getY() + 0.5, checkPos.getZ() + 0.5, 0, 0.05, 0);
                    break;

                case "visualmod:anomaly_mosquito":
                    pullPlayer(player, checkPos);
                    EffectStageManager.addEffect(player, EffectType.GRAVITATIONAL, 2);
                    break;

                // Добавь другие аномалии по аналогии
            }
        }
    }

    private void pullPlayer(EntityPlayer player, BlockPos anomalyPos) {
        double dx = anomalyPos.getX() + 0.5 - player.posX;
        double dy = anomalyPos.getY() + 0.5 - player.posY;
        double dz = anomalyPos.getZ() + 0.5 - player.posZ;

        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double strength = 0.08;

        if (dist > 0.5) {
            player.motionX += (dx / dist) * strength;
            player.motionY += (dy / dist) * strength;
            player.motionZ += (dz / dist) * strength;
            player.velocityChanged = true;
        }
    }
}
