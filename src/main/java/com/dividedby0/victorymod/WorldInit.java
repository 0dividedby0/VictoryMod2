package com.dividedby0.victorymod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;

public class WorldInit {

    private static final String DATA_NAME = "victorymod_data";
    private static StructureSpawner.SpawnJob activeJob;

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

        if (activeJob == null) {
            activeJob = new StructureSpawner.SpawnJob(level.getSharedSpawnPos());
            activeJob.start(level);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END
            || activeJob == null
            || !(event.level instanceof ServerLevel level)
            || !level.dimension().equals(Level.OVERWORLD)) {
            return;
        }

        // Generate at most one structure per tick so initial world load and the
        // watchdog are never held by the complete 17-structure batch.
        if (!activeJob.spawnNext(level)) {
            activeJob.finish(level);
            activeJob = null;
        }
    }
}
