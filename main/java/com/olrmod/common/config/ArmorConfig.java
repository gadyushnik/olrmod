package com.olrmod.common.config;

import java.util.List;

public class ArmorConfig {
    private String id;
    private double radiationDebuff;
    private double electricShockModifier;
    private List<EffectConfig> buffs;
    private List<EffectConfig> debuffs;

    public ArmorConfig(String id, double radiationDebuff, double electricShockModifier, List<EffectConfig> buffs, List<EffectConfig> debuffs) {
        this.id = id;
        this.radiationDebuff = radiationDebuff;
        this.electricShockModifier = electricShockModifier;
        this.buffs = buffs;
        this.debuffs = debuffs;
    }

    public String getId() {
        return id;
    }

    public double getRadiationDebuff() {
        return radiationDebuff;
    }

    public double getElectricShockModifier() {
        return electricShockModifier;
    }

    public List<EffectConfig> getBuffs() {
        return buffs;
    }

    public List<EffectConfig> getDebuffs() {
        return debuffs;
    }
} 