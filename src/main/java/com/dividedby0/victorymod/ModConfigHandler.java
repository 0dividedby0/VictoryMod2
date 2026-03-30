package com.dividedby0.victorymod;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfigHandler {

    public static class Common {
        public final ForgeConfigSpec.IntValue minDungeonRadius;
        public final ForgeConfigSpec.IntValue maxDungeonRadius;
        public final ForgeConfigSpec.IntValue structureBufferDistance;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.comment("Victory Monument Spawning Configuration");
            
            minDungeonRadius = builder
                .comment("Minimum radius for dungeon placement around spawn point (in blocks)")
                .defineInRange("minDungeonRadius", 40, 10, 500);

            maxDungeonRadius = builder
                .comment("Maximum radius for dungeon placement around spawn point (in blocks)")
                .defineInRange("maxDungeonRadius", 750, 50, 1000);

            structureBufferDistance = builder
                .comment("Minimum buffer distance between structures to prevent overlap (in blocks)")
                .defineInRange("structureBufferDistance", 30, 5, 200);
        }
    }

    public static final ForgeConfigSpec CONFIG_SPEC;
    public static final Common COMMON;

    static {
        var pair = new ForgeConfigSpec.Builder()
            .configure(Common::new);
        COMMON = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }
}



