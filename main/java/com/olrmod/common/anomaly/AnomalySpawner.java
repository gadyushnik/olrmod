package com.olrmod.anomaly;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

public class AnomalySpawner {
    private static final List<BlockPos> spawnedAnomalies = new ArrayList<>();
    private static final Random rand = new Random();

    // Настроечные параметры
    private static final double anomalyChancePerChunk = 0.2; // 20% шанс на одну аномалию в чанке
    private static final Block anomalyBlock = Block.getBlockFromName("visualmod:anomaly_burning_fuzz");

    public static void clearOldAnomalies(World world) {
        for (BlockPos pos : spawnedAnomalies) {
            if (world.isBlockLoaded(pos)) {
                world.setBlockToAir(pos);
            }
        }
        spawnedAnomalies.clear();
    }

    public static void spawnNewAnomalies(World world) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) return;

        int radiusChunks = 8;

        server.getPlayerList().getPlayers().forEach(player -> {
            int playerChunkX = player.chunkCoordX;
            int playerChunkZ = player.chunkCoordZ;

            for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
                for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
                    Chunk chunk = world.getChunkFromChunkCoords(playerChunkX + dx, playerChunkZ + dz);
                    if (rand.nextDouble() < anomalyChancePerChunk) {
                        BlockPos pos = findValidPositionInChunk(world, chunk);
                        if (pos != null) {
                            world.setBlockState(pos, anomalyBlock.getDefaultState());
                            spawnedAnomalies.add(pos);
                        }
                    }
                }
            }
        });
    }

    private static BlockPos findValidPositionInChunk(World world, Chunk chunk) {
        int x = chunk.x * 16 + rand.nextInt(16);
        int z = chunk.z * 16 + rand.nextInt(16);
        int y = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY();

        BlockPos pos = new BlockPos(x, y, z);
        IBlockState below = world.getBlockState(pos.down());
        IBlockState target = world.getBlockState(pos);

        if (below.getMaterial().isSolid() && target.getMaterial().isReplaceable()) {
            return pos;
        }

        return null;
    }
}
