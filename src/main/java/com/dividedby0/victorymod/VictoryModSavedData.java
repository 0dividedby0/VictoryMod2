package com.dividedby0.victorymod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class VictoryModSavedData extends SavedData {
    private static final String KEY = "VictoryModStructuresSpawned";
    private boolean structuresSpawned = false;

    public VictoryModSavedData() {}
    public VictoryModSavedData(CompoundTag tag) {
        this.structuresSpawned = tag.getBoolean(KEY);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean(KEY, structuresSpawned);
        return tag;
    }

    public boolean isStructuresSpawned() {
        return structuresSpawned;
    }

    public void setStructuresSpawned(boolean value) {
        this.structuresSpawned = value;
        setDirty();
    }

    public static VictoryModSavedData load(CompoundTag tag) {
        return new VictoryModSavedData(tag);
    }
}