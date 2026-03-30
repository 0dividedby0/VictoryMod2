package com.dividedby0.victorymod;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StructureSpawner {

    private static final String[] COLORS = {
        "white","orange","magenta","lightblue","yellow","lime","pink","gray",
        "lightgray","cyan","purple","blue","brown","green","red","black"
    };

    private static final List<BlockPos> placedStructures = new ArrayList<>();

    public static void spawnAll(ServerLevel level) {
        placedStructures.clear();
        
        BlockPos spawn = level.getSharedSpawnPos();

        int spawnX = spawn.getX();
        int spawnZ = spawn.getZ();
        int spawnY = getGroundY(level, spawnX, spawnZ);

        BlockPos victoryPos = new BlockPos(spawnX, spawnY, spawnZ);
        
        // Check if victory monument location is valid (on land)
        if (isValidSpawnLocation(level, victoryPos)) {
            placeStructure(level, "victory_monument", victoryPos);
            placedStructures.add(victoryPos);
        } else {
            System.err.println("[VictoryMod] Victory monument spawn location not valid (not on land): " + victoryPos);
        }

        Random rand = new Random();
        // Configurable spawn radius and buffer - read from config with fallback defaults
        int minRadius = getConfigIntValue(ModConfigHandler.COMMON.minDungeonRadius, 40);
        int maxRadius = getConfigIntValue(ModConfigHandler.COMMON.maxDungeonRadius, 750);
        int bufferDistance = getConfigIntValue(ModConfigHandler.COMMON.structureBufferDistance, 30);
        
        for (String color : COLORS) {
            // Attempt to find a valid spawn location for this dungeon
            int attempts = 0;
            int maxAttempts = 50;
            BlockPos validPos = null;
            
            while (validPos == null && attempts < maxAttempts) {
                double angle = rand.nextDouble() * Math.PI * 2.0;
                int radius = minRadius + rand.nextInt(maxRadius - minRadius + 1);
                int x = spawnX + (int) Math.round(radius * Math.cos(angle));
                int z = spawnZ + (int) Math.round(radius * Math.sin(angle));

                int y = getGroundY(level, x, z);
                BlockPos candidatePos = new BlockPos(x, y, z);
                
                // Check if location is on land and doesn't overlap with existing structures
                if (isValidSpawnLocation(level, candidatePos) && !overlapsWithExisting(candidatePos, bufferDistance)) {
                    validPos = candidatePos;
                }
                
                attempts++;
            }
            
            if (validPos != null) {
                placeStructure(level, "dungeon_" + color, validPos);
                placedStructures.add(validPos);
            } else {
                System.err.println("[VictoryMod] Could not find valid spawn location for dungeon_" + color + " after " + maxAttempts + " attempts");
            }
        }
    }

    private static int getGroundY(ServerLevel level, int x, int z) {
        // Use WORLD_SURFACE_WG in world spawn phase to avoid the 0-y bug from premature data.
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

        if (y <= level.getMinBuildHeight() + 1) {
            // fallback scan in case heightmap is not populated yet
            for (int scanY = level.getMaxBuildHeight() - 1; scanY > level.getMinBuildHeight(); scanY--) {
                if (!level.isEmptyBlock(new BlockPos(x, scanY, z))) {
                    y = scanY + 1;
                    break;
                }
            }
        }

        y = Math.max(y, level.getMinBuildHeight() + 1);
        y = Math.min(y, level.getMaxBuildHeight() - 1);
        return y;
    }

    /**
     * Checks if a location is valid for structure spawning.
     * Valid locations must be on solid land (not water or air-like blocks).
     */
    private static boolean isValidSpawnLocation(ServerLevel level, BlockPos pos) {
        // Check if the block below is solid (not water, not air, not leaves/trees)
        BlockPos belowPos = pos.below();
        net.minecraft.world.level.block.Block blockBelow = level.getBlockState(belowPos).getBlock();
        
        // Reject water blocks
        if (blockBelow instanceof net.minecraft.world.level.block.LiquidBlock) {
            return false;
        }
        
        // Reject leaves and tree-related blocks
        if (blockBelow instanceof net.minecraft.world.level.block.LeavesBlock) {
            return false;
        }
        
        // Reject air and void blocks
        if (blockBelow == Blocks.AIR || blockBelow == Blocks.VOID_AIR || blockBelow == Blocks.CAVE_AIR) {
            return false;
        }
        
        // Reject grass, seagrass, and similar non-solid vegetation
        String blockName = blockBelow.getName().getString();
        if (blockName.contains("grass") || blockName.contains("seagrass") || 
            blockName.contains("flower") || blockName.contains("mushroom") ||
            blockName.contains("vine") || blockName.contains("kelp")) {
            return false;
        }
        
        // Additional check: ensure it's not a liquid at the spawn position either
        if (level.getBlockState(pos).getBlock() instanceof net.minecraft.world.level.block.LiquidBlock) {
            return false;
        }
        
        return true;
    }

    /**
     * Checks if a position overlaps with any previously placed structure.
     * Considers the buffer distance around each placed structure.
     */
    private static boolean overlapsWithExisting(BlockPos pos, int bufferDistance) {
        for (BlockPos placedPos : placedStructures) {
            // Calculate horizontal distance (ignoring Y)
            int dx = pos.getX() - placedPos.getX();
            int dz = pos.getZ() - placedPos.getZ();
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
            
            if (horizontalDistance < bufferDistance) {
                return true;
            }
        }
        return false;
    }

    private static void placeStructure(ServerLevel level, String name, BlockPos pos) {
        ResourceLocation templateId = ResourceLocation.tryParse("victorymod:" + name);
        if (templateId == null) {
            System.err.println("[VictoryMod] invalid structure id: " + name);
            return;
        }

        StructureTemplate template = level.getStructureManager().getOrCreate(templateId);

        if (template == null) {
            System.err.println("[VictoryMod] structure not found: " + name + " (id=" + templateId + ")");
            return;
        }

        boolean placed = template.placeInWorld(level, pos, pos, new StructurePlaceSettings(), level.random, 3);
        if (!placed) {
            System.err.println("[VictoryMod] failed to place structure " + name + " at " + pos);
        } else {
            System.out.println("[VictoryMod] placed " + name + " at " + pos);
        }
    }

    /**
     * Safely get a config integer value, falling back to default if config not yet loaded
     */
    private static int getConfigIntValue(net.minecraftforge.common.ForgeConfigSpec.IntValue configValue, int defaultValue) {
        try {
            return configValue.get();
        } catch (Exception e) {
            // Config not loaded yet, use default
            return defaultValue;
        }
    }
}