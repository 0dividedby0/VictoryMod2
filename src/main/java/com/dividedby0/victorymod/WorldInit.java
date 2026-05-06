package com.dividedby0.victorymod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;

public class WorldInit {

    private static final String DATA_NAME = "victorymod_data";

    public static void init() {
        MinecraftForge.EVENT_BUS.register(WorldInit.class);
    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        // Only generate once in the overworld: this event fires for each dimension.
        if (!level.dimension().equals(Level.OVERWORLD)) return;

        VictoryModSavedData data = level.getDataStorage().computeIfAbsent(
            VictoryModSavedData::load,
            VictoryModSavedData::new,
            DATA_NAME
        );

        if (data.isStructuresSpawned()) return;

        data.setStructuresSpawned(true);
        data.setDirty();
        StructureSpawner.spawnAll(level);
    }

    // No longer needed: haveStructuresBeenSpawned and markStructuresAsSpawned
}
