package com.olrmod.radiation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Loader;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RadiationZoneManager {
    private static final List<RadiationZone> zones = new ArrayList<>();
    private static final Gson GSON = new Gson();

    public static void loadZones() {
        File file = new File(new File(Loader.instance().getConfigDir(), "olrmod"), "radiation_zones.json");
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<List<RadiationZone>>() {}.getType();
            List<RadiationZone> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                zones.clear();
                zones.addAll(loaded);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveZones() {
        File file = new File(new File(Loader.instance().getConfigDir(), "olrmod"), "radiation_zones.json");
        file.getParentFile().mkdirs();
        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(zones, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addZone(RadiationZone zone) {
        zones.add(zone);
    }

    public static void removeZonesInChunk(int chunkX, int chunkZ) {
        zones.removeIf(zone -> {
            BlockPos p1 = zone.getPos1();
            return (p1.getX() >> 4) == chunkX && (p1.getZ() >> 4) == chunkZ;
        });
    }

    public static int getRadiationStage(EntityPlayer player) {
        BlockPos pos = player.getPosition();
        for (RadiationZone zone : zones) {
            if (zone.isInside(pos)) {
                return zone.getStage();
            }
        }
        return 0;
    }

    public static List<RadiationZone> getAllZones() {
        return zones;
    }
}
