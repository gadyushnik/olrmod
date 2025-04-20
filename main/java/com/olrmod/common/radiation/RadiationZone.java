package com.olrmod.radiation;

import net.minecraft.util.math.BlockPos;

public class RadiationZone {
    public enum ZoneType { LOW, MEDIUM, HIGH }

    private BlockPos pos1;
    private BlockPos pos2;
    private ZoneType type;
    private int stage;

    public RadiationZone(BlockPos pos1, BlockPos pos2, ZoneType type, int stage) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.type = type;
        this.stage = stage;
    }

    public BlockPos getPos1() {
        return pos1;
    }

    public BlockPos getPos2() {
        return pos2;
    }

    public ZoneType getType() {
        return type;
    }

    public int getStage() {
        return stage;
    }

    public boolean isInside(BlockPos pos) {
        return pos.getX() >= Math.min(pos1.getX(), pos2.getX()) &&
               pos.getX() <= Math.max(pos1.getX(), pos2.getX()) &&
               pos.getY() >= Math.min(pos1.getY(), pos2.getY()) &&
               pos.getY() <= Math.max(pos1.getY(), pos2.getY()) &&
               pos.getZ() >= Math.min(pos1.getZ(), pos2.getZ()) &&
               pos.getZ() <= Math.max(pos1.getZ(), pos2.getZ());
    }
}
