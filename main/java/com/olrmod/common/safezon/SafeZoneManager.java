package com.olrmod.emission;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class SafeZoneManager {
    private static final Map<String, AxisAlignedBB> safeZones = new HashMap<>();

    public static void addZone(String id, BlockPos pos1, BlockPos pos2) {
        safeZones.put(id, new AxisAlignedBB(pos1, pos2).expand(1, 1, 1));
    }

    public static void removeZone(String id) {
        safeZones.remove(id);
    }

    public static boolean isInSafeZone(EntityPlayer player) {
        for (AxisAlignedBB box : safeZones.values()) {
            if (box.contains(player.getPositionVector())) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, AxisAlignedBB> getAllZones() {
        return safeZones;
    }

    public static void clearAll() {
        safeZones.clear();
    }
}
