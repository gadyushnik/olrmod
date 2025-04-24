package com.olrmod.effects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;
import java.util.Map;

public class EffectStageManager {
    public enum EffectType {
        RADIATION, CHEMICAL, PSY, GRAVITATIONAL
    }

    private static final Logger LOGGER = LogManager.getLogger("OLRMod");
    private static final Map<EffectType, String> NBT_KEYS = new EnumMap<>(EffectType.class);

    static {
        NBT_KEYS.put(EffectType.RADIATION, "effect_radiation");
        NBT_KEYS.put(EffectType.CHEMICAL, "effect_chemical");
        NBT_KEYS.put(EffectType.PSY, "effect_psi");
        NBT_KEYS.put(EffectType.GRAVITATIONAL, "effect_grav");
    }

    public static void addEffect(EntityPlayer player, EffectType type, int amount) {
        if (player == null || type == null) {
            LOGGER.warn("Attempted to add effect to null player or with null type");
            return;
        }
        NBTTagCompound data = getOrCreateNBT(player);
        String key = NBT_KEYS.get(type);
        int current = data.getInteger(key);
        data.setInteger(key, current + amount);
    }

    public static int getStage(EntityPlayer player, EffectType type) {
        if (player == null || type == null) return 0;
        int value = getOrCreateNBT(player).getInteger(NBT_KEYS.get(type));
        if (value >= 100) return 3;
        else if (value >= 50) return 2;
        else if (value > 0) return 1;
        else return 0;
    }

    public static void reduceEffects(EntityPlayer player) {
        if (player == null) return;
        NBTTagCompound data = getOrCreateNBT(player);
        for (String key : NBT_KEYS.values()) {
            int val = data.getInteger(key);
            if (val > 0) {
                data.setInteger(key, Math.max(0, val - 1));
            }
        }
    }

    private static NBTTagCompound getOrCreateNBT(EntityPlayer player) {
        NBTTagCompound entityData = player.getEntityData();
        if (!entityData.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            entityData.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
        }
        return entityData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
    }

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        if (player.ticksExisted % 40 == 0) {
            for (EffectType type : EffectType.values()) {
                int stage = getStage(player, type);
                if (stage > 0) {
                    LOGGER.debug("Applying {} stage {} to {}", type.name(), stage, player.getName());
                    player.attackEntityFrom(ModDamageSources.fromType(type), stage);
                }
            }
        }

        if (player.ticksExisted % 100 == 0) {
            reduceEffects(player);
        }
    }
}
