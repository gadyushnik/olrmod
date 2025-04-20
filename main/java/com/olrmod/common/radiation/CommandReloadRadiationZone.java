package com.olrmod.radiation;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;

public class CommandReloadRadiationZone extends CommandBase {

    @Override
    public String getName() {
        return "reloadradiationzone";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/reloadradiationzone — перегенерирует радиацию в текущем чанке";
    }

    @Override
    public void execute(net.minecraft.server.MinecraftServer server, ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayerMP)) return;

        EntityPlayerMP player = (EntityPlayerMP) sender;
        BlockPos pos = player.getPosition();
        World world = player.world;
        ChunkPos chunkPos = new ChunkPos(pos);
        Random rand = new Random();

        Biome biome = world.getBiome(pos);
        RadiationZoneManager.removeZonesInChunk(chunkPos.x, chunkPos.z);

        for (RadiationZone.ZoneType type : RadiationZone.ZoneType.values()) {
            RadiationSpawnConfig.SpawnSettings set = RadiationSpawnConfig.getSettings(biome, type);
            if (set != null && rand.nextFloat() < set.chance) {
                BlockPos p1 = new BlockPos((chunkPos.x << 4) + rand.nextInt(16), 64, (chunkPos.z << 4) + rand.nextInt(16));
                BlockPos p2 = p1.add(3 + rand.nextInt(4), 2, 3 + rand.nextInt(4));
                int stage = type.ordinal() + 1;
                RadiationZone zone = new RadiationZone(p1, p2, type, stage);
                RadiationZoneManager.addZone(zone);
            }
        }

        RadiationZoneManager.saveZones();
        sender.sendMessage(new TextComponentString("Радиационные зоны в чанке обновлены."));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
