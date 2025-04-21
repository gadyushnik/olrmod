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
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AnomalyLogicHandler {

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        World world = player.world;

        if (player.ticksExisted % 20 != 0) return;

        BlockPos pos = player.getPosition();
        AxisAlignedBB box = new AxisAlignedBB(pos).grow(1.0D);

        for (BlockPos checkPos : BlockPos.getAllInBoxMutable(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)) {
            Block block = world.getBlockState(checkPos).getBlock();
            String id = block.getRegistryName().toString();

            AnomalyDefinition def = AnomalyConfigLoader.getByBlockId(id);
            if (def == null) continue;

            switch (def.anomalyId) {
                case "burning_fuzz":
                    EffectStageManager.addEffect(player, EffectType.CHEMICAL, 2);
                    world.spawnParticle(EnumParticleTypes.FLAME, checkPos.getX() + 0.5, checkPos.getY() + 0.5, checkPos.getZ() + 0.5, 0, 0.05, 0);
                    world.playSound(null, checkPos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.4F, 1F);
                    break;

                case "mosquito":
                    pullPlayer(player, checkPos);
                    EffectStageManager.addEffect(player, EffectType.GRAVITATIONAL, 2);
                    break;

                // Добавляй остальные по аналогии
            }
        }
    }

    private void pullPlayer(EntityPlayer player, BlockPos anomalyPos) {
        double dx = anomalyPos.getX() + 0.5 - player.posX;
        double dy = anomalyPos.getY() + 0.5 - player.posY;
        double dz = anomalyPos.getZ() + 0.5 - player.posZ;

        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist > 0.5) {
            double strength = 0.08;
            player.motionX += (dx / dist) * strength;
            player.motionY += (dy / dist) * strength;
            player.motionZ += (dz / dist) * strength;
            player.velocityChanged = true;
        }
    }
}
