package com.olrmod.effects;

import net.minecraft.util.DamageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModDamageSources {
    private static final Logger LOGGER = LogManager.getLogger("OLRMod");

    public static final DamageSource RADIATION = new DamageSource("mod_radiation").setDamageBypassesArmor();
    public static final DamageSource CHEMICAL = new DamageSource("mod_chemical").setDamageBypassesArmor();
    public static final DamageSource GRAVITATIONAL = new DamageSource("mod_gravitational").setDamageBypassesArmor();
    public static final DamageSource PSY = new DamageSource("mod_psy").setDamageBypassesArmor();

    public static DamageSource fromType(EffectStageManager.EffectType type) {
        if (type == null) {
            LOGGER.warn("EffectType is null when requesting DamageSource!");
            return DamageSource.GENERIC;
        }
        switch (type) {
            case RADIATION: return RADIATION;
            case CHEMICAL: return CHEMICAL;
            case GRAVITATIONAL: return GRAVITATIONAL;
            case PSY: return PSY;
            default:
                LOGGER.error("Unhandled EffectType: {}", type);
                return DamageSource.GENERIC;
        }
    }
}
