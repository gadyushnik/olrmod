package com.olrmod.common.config;

public class EffectConfig {
    private String effect;
    private int duration;
    private int amplifier;

    public EffectConfig(String effect, int duration, int amplifier) {
        this.effect = effect;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    public String getEffect() {
        return effect;
    }

    public int getDuration() {
        return duration;
    }

    public int getAmplifier() {
        return amplifier;
    }
} 