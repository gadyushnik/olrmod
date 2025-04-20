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

import java.util.Random;

public class CommandGenerateRadiationZones extends CommandBase {

    @Override
    public String getName() {
        return "genradiationzones";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/genradiationzones — генерирует зоны радиации";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        World world = sender.getEntityWorld();
        Random rand = new Random();

        for (Chunk chunk : world.getChunkProvider().getLoadedChunks()) {
            ChunkPos pos = chunk.getPos();
            Biome biome = world.getBiome(new BlockPos(pos.x << 4, 64, pos.z << 4));

            for (RadiationZone.ZoneType type : RadiationZone.ZoneType.values()) {
                RadiationSpawnConfig.SpawnSettings set = RadiationSpawnConfig.getSettings(biome, type);
                if (set != null && rand.nextFloat() < set.chance) {
                    BlockPos p1 = new BlockPos((pos.x << 4) + rand.nextInt(16), 64, (pos.z << 4) + rand.nextInt(16));
                    BlockPos p2 = p1.add(3 + rand.nextInt(4), 2, 3 + rand.nextInt(4));
                    int stage = type.ordinal() + 1;
                    RadiationZone zone = new RadiationZone(p1, p2, type, stage);
                    RadiationZoneManager.addZone(zone);
                }
            }
        }

        RadiationZoneManager.saveZones();
        sender.sendMessage(new TextComponentString("Радиационные зоны сгенерированы."));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
