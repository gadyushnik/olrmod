package com.olrmod.effects;

import net.minecraft.util.DamageSource;

public class ModDamageSources {
    public static DamageSource fromType(EffectStageManager.EffectType type) {
        return new DamageSource("mod_" + type.name().toLowerCase()).setDamageBypassesArmor();
    }
}
