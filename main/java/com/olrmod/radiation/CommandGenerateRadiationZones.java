package com.olrmod.radiation;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CommandGenerateRadiationZones extends CommandBase {

    @Override
    public String getName() {
        return "genradiationzones";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/genradiationzones — генерация радиационных зон в загруженных чанках";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        World world = sender.getEntityWorld();
        Random random = new Random();

        Map<String, Map<RadiationZone.ZoneType, Integer>> counters = new HashMap<>();

        for (Chunk chunk : world.getChunkProvider().getLoadedChunks()) {
            ChunkPos chunkPos = chunk.getPos();
            Biome biome = world.getBiome(new BlockPos(chunkPos.x << 4, 64, chunkPos.z << 4));
            String biomeName = biome.getRegistryName().getPath();

            for (RadiationZone.ZoneType type : RadiationZone.ZoneType.values()) {
                RadiationSpawnConfig.SpawnData data = RadiationSpawnConfig.get(biomeName, type);
                if (data == null) continue;

                counters.putIfAbsent(biomeName, new HashMap<>());
                Map<RadiationZone.ZoneType, Integer> biomeCounts = counters.get(biomeName);
                int count = biomeCounts.getOrDefault(type, 0);

                if (count >= data.max) continue;
                if (random.nextFloat() >= data.chance) continue;

                BlockPos p1 = new BlockPos((chunkPos.x << 4) + random.nextInt(16), 64, (chunkPos.z << 4) + random.nextInt(16));
                BlockPos p2 = p1.add(3 + random.nextInt(4), 2, 3 + random.nextInt(4));
                int stage = type.ordinal() + 1;

                RadiationZone zone = new RadiationZone(p1, p2, type, stage);
                RadiationZoneManager.addZone(zone);
                biomeCounts.put(type, count + 1);
            }
        }

        RadiationZoneManager.saveZones();
        sender.sendMessage(new TextComponentString("Генерация радиационных зон завершена."));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
