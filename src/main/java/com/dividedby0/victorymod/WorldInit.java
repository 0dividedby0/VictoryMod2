package com.dividedby0.victorymod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;

public class WorldInit {

    private static final java.util.Set<String> generatedWorlds = new java.util.HashSet<>();

    public static void init() {
        MinecraftForge.EVENT_BUS.register(WorldInit.class);
    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        // Only generate once in the overworld: this event fires for each dimension.
        if (!level.dimension().equals(Level.OVERWORLD)) return;

        // Use a world key (world folder + dimension + seed) for distinct worlds even with same seed.
        String worldName = level.getServer().getWorldData().getLevelName();
        String worldKey = worldName + "@" + level.dimension().location() + "@" + level.getSeed();

        if (generatedWorlds.contains(worldKey)) return;

        generatedWorlds.add(worldKey);
        StructureSpawner.spawnAll(level);
    }
}