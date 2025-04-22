package com.olrmod.anomaly;

import com.olrmod.effects.EffectStageManager;
import com.olrmod.effects.EffectStageManager.EffectType;
import net.minecraft.block.Block;
import net.minecraft.entity.player.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

public class AnomalyLogicHandler {

    private static final int EFFECT_INTERVAL = 20;
    private static final Map<UUID, Integer> deathTimers = new HashMap<>();

    @SubscribeEvent
    public void onEntityTick(LivingEvent.LivingUpdateEvent event) {
        Entity entity = event.getEntity();
        World world = entity.world;

        if (world.isRemote) return;

        BlockPos pos = entity.getPosition();
        AxisAlignedBB box = new AxisAlignedBB(pos).grow(0.5);

        for (BlockPos checkPos : BlockPos.getAllInBoxMutable(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)) {
            Block block = world.getBlockState(checkPos).getBlock();
            String id = block.getRegistryName().toString();
            AnomalyDefinition def = AnomalyConfigLoader.getByBlockId(id);
            if (def == null) continue;

            switch (def.type.toLowerCase()) {
                case "mincer":
                    if (entity.ticksExisted % 40 == 0) {
                        EffectStageManager.addEffect(entity, EffectType.ELECTRIC, 1);
                        spawnParticles(world, checkPos, EnumParticleTypes.CRIT);
                    } else {
                        spawnParticles(world, checkPos, EnumParticleTypes.SPELL);
                    }
                    break;

                case "galantine":
                    EffectStageManager.addEffect(entity, EffectType.CHEMICAL, 1);
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, entity.posX, entity.posY + 0.5, entity.posZ, 0, 0.01, 0);
                    break;

                case "mosquito":
                    pullEntity(entity, checkPos);
                    if (entity.ticksExisted % 40 == 0) {
                        entity.attackEntityFrom(EffectStageManager.GENERIC, 1000);
                        EffectStageManager.addEffect(entity, EffectType.GRAVITY, 3);
                    }
                    break;

                case "burning_fuzz":
                    EffectStageManager.addEffect(entity, EffectType.CHEMICAL, 1);
                    slowEntity(entity, 0.8);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, entity.posX, entity.posY, entity.posZ, 0, 0, 0);
                    break;

                case "rusty_hair":
                    EffectStageManager.addEffect(entity, EffectType.CHEMICAL, 1);
                    slowEntity(entity, 0.3);
                    break;

                case "black_needles":
                    if (entity.ticksExisted % 40 == 0) {
                        EffectStageManager.addEffect(entity, EffectType.CHEMICAL, 1);
                        entity.attackEntityFrom(EffectStageManager.GENERIC, 2);
                    }
                    slowEntity(entity, 0.6);
                    break;

                case "stomper":
                    if (entity.ticksExisted % 40 == 0) {
                        world.playSound(null, checkPos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 1f, 1f);
                    }
                    break;

                case "puke":
                    if (entity instanceof EntityPlayer) {
                        ((EntityPlayer) entity).addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 60));
                    }
                    break;

                case "dead_zone":
                    UUID uuid = entity.getUniqueID();
                    deathTimers.putIfAbsent(uuid, 60 + new Random().nextInt(100)); // от 3 до 8 секунд
                    break;
            }
        }

        handleDeadZoneTimers(world);
    }

    private void handleDeadZoneTimers(World world) {
        Iterator<Map.Entry<UUID, Integer>> it = deathTimers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            UUID uuid = entry.getKey();
            int ticks = entry.getValue() - 1;

            if (ticks <= 0) {
                EntityPlayer player = world.getPlayerEntityByUUID(uuid);
                if (player != null) {
                    player.attackEntityFrom(EffectStageManager.GENERIC, 9999);
                }
                it.remove();
            } else {
                entry.setValue(ticks);
            }
        }
    }

    private void pullEntity(Entity entity, BlockPos center) {
        double dx = center.getX() + 0.5 - entity.posX;
        double dy = center.getY() + 0.5 - entity.posY;
        double dz = center.getZ() + 0.5 - entity.posZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist < 2 && dist > 0.1) {
            double strength = 0.1;
            entity.motionX += (dx / dist) * strength;
            entity.motionY += (dy / dist) * strength;
            entity.motionZ += (dz / dist) * strength;
            entity.velocityChanged = true;
        }
    }

    private void slowEntity(Entity entity, double factor) {
        entity.motionX *= factor;
        entity.motionZ *= factor;
    }

    private void spawnParticles(World world, BlockPos pos, EnumParticleTypes type) {
        for (int i = 0; i < 5; i++) {
            world.spawnParticle(type, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0, 0, 0);
        }
    }
}
