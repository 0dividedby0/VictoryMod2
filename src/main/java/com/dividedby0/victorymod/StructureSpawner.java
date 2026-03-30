package com.dividedby0.victorymod;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

import java.util.Random;

public class StructureSpawner {

    private static final String[] COLORS = {
        "white","orange","magenta","light_blue","yellow","lime","pink","gray",
        "light_gray","cyan","purple","blue","brown","green","red","black"
    };

    public static void spawnAll(ServerLevel level) {
        BlockPos spawn = level.getSharedSpawnPos();

        int spawnX = spawn.getX();
        int spawnZ = spawn.getZ();
        int spawnY = getGroundY(level, spawnX, spawnZ);

        BlockPos victoryPos = new BlockPos(spawnX, spawnY, spawnZ);
        placeStructure(level, "victory_monument", victoryPos);

        Random rand = new Random();
        for (String color : COLORS) {
            // Keep dungeons within nearby region so superflat/empty worlds show them.
            double angle = rand.nextDouble() * Math.PI * 2.0;
            int radius = 40 + rand.nextInt(120); // 40..159
            int x = spawnX + (int) Math.round(radius * Math.cos(angle));
            int z = spawnZ + (int) Math.round(radius * Math.sin(angle));

            int y = getGroundY(level, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            placeStructure(level, "dungeon_" + color, pos);
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
}