package com.olrmod.common.config;

import java.util.List;

public class ArtifactConfig {
    private String type;
    private List<EffectConfig> buffs;
    private List<EffectConfig> debuffs;

    public ArtifactConfig(String type, List<EffectConfig> buffs, List<EffectConfig> debuffs) {
        this.type = type;
        this.buffs = buffs;
        this.debuffs = debuffs;
    }

    public String getType() {
        return type;
    }

    public List<EffectConfig> getBuffs() {
        return buffs;
    }

    public List<EffectConfig> getDebuffs() {
        return debuffs;
    }
} 