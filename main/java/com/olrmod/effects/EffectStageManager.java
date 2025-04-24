package com.olrmod.effects;

import net.minecraft.entity.Entity;
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
        RADIATION(true), CHEMICAL(true), PSY(true), GRAVITATIONAL(true),
        ELECTRIC(false), FIRE(false); // новые эффекты без стадий

        private final boolean staged;
        EffectType(boolean staged) {
            this.staged = staged;
        }
        public boolean isStaged() {
            return staged;
        }
    }

    private static final Logger LOGGER = LogManager.getLogger("OLRMod");
    private static final Map<EffectType, String> NBT_KEYS = new EnumMap<>(EffectType.class);

    static {
        NBT_KEYS.put(EffectType.RADIATION, "effect_radiation");
        NBT_KEYS.put(EffectType.CHEMICAL, "effect_chemical");
        NBT_KEYS.put(EffectType.PSY, "effect_psi");
        NBT_KEYS.put(EffectType.GRAVITATIONAL, "effect_grav");
    }

    public static void addEffect(Entity entity, EffectType type, int amount) {
        if (!(entity instanceof EntityPlayer) || type == null) return;
        EntityPlayer player = (EntityPlayer) entity;

        if (!type.isStaged()) {
            player.attackEntityFrom(ModDamageSources.fromType(type), amount);
            LOGGER.debug("Applied instant {} damage: {}", type.name(), amount);
            return;
        }

        NBTTagCompound data = getOrCreateNBT(player);
        String key = NBT_KEYS.get(type);
        int current = data.getInteger(key);
        data.setInteger(key, current + amount);
    }

    public static int getStage(EntityPlayer player, EffectType type) {
        if (player == null || type == null || !type.isStaged()) return 0;
        int value = getOrCreateNBT(player).getInteger(NBT_KEYS.get(type));
        return value >= 100 ? 3 : value >= 50 ? 2 : value > 0 ? 1 : 0;
    }

    public static void reduceEffects(EntityPlayer player) {
        if (player == null) return;
        NBTTagCompound data = getOrCreateNBT(player);
        for (Map.Entry<EffectType, String> entry : NBT_KEYS.entrySet()) {
            int val = data.getInteger(entry.getValue());
            if (val > 0) data.setInteger(entry.getValue(), Math.max(0, val - 1));
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
                if (!type.isStaged()) continue;
                int stage = getStage(player, type);
                if (stage > 0) {
                    player.attackEntityFrom(ModDamageSources.fromType(type), stage);
                    LOGGER.debug("Stage {} damage from {} to {}", stage, type.name(), player.getName());
                }
            }
        }

        if (player.ticksExisted % 100 == 0) {
            reduceEffects(player);
        }
    }
}
