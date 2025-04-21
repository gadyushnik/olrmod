package com.olrmod.effects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class EffectStageManager {
    public enum EffectType {
        RADIATION, CHEMICAL, PSI, GRAVITATIONAL
    }

    private static final Map<EffectType, String> NBT_KEYS = new HashMap<>();

    static {
        NBT_KEYS.put(EffectType.RADIATION, "effect_radiation");
        NBT_KEYS.put(EffectType.CHEMICAL, "effect_chemical");
        NBT_KEYS.put(EffectType.PSI, "effect_psi");
        NBT_KEYS.put(EffectType.GRAVITATIONAL, "effect_grav");
    }

    public static void addEffect(EntityPlayer player, EffectType type, int amount) {
        NBTTagCompound data = getOrCreateNBT(player);
        String key = NBT_KEYS.get(type);
        int current = data.getInteger(key);
        data.setInteger(key, current + amount);
    }

    public static int getEffectStage(EntityPlayer player, EffectType type) {
        int value = getOrCreateNBT(player).getInteger(NBT_KEYS.get(type));
        if (value >= 100) return 3;
        else if (value >= 50) return 2;
        else if (value > 0) return 1;
        else return 0;
    }

    public static void reduceEffects(EntityPlayer player) {
        NBTTagCompound data = getOrCreateNBT(player);
        for (String key : NBT_KEYS.values()) {
            int val = data.getInteger(key);
            if (val > 0) data.setInteger(key, Math.max(0, val - 1));
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

        for (EffectType type : EffectType.values()) {
            int stage = getEffectStage(player, type);
            if (stage > 0 && player.ticksExisted % 40 == 0) {
                player.attackEntityFrom(ModDamageSources.fromType(type), stage); // кастомный урон
            }
        }

        if (player.ticksExisted % 100 == 0) {
            reduceEffects(player);
        }
    }
}
