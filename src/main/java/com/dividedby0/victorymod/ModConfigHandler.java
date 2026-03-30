package com.dividedby0.victorymod;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfigHandler {

    public static class Common {
        public final ForgeConfigSpec.IntValue minDungeonRadius;
        public final ForgeConfigSpec.IntValue maxDungeonRadius;
        public final ForgeConfigSpec.IntValue structureBufferDistance;

        // XP thresholds for heart scaling
        public final ForgeConfigSpec.IntValue xpThreshold_1;
        public final ForgeConfigSpec.IntValue xpThreshold_2;
        public final ForgeConfigSpec.IntValue xpThreshold_3;
        public final ForgeConfigSpec.IntValue xpThreshold_4;
        public final ForgeConfigSpec.IntValue xpThreshold_5;
        public final ForgeConfigSpec.IntValue xpThreshold_6;
        public final ForgeConfigSpec.IntValue xpThreshold_7;
        public final ForgeConfigSpec.IntValue xpThreshold_8;
        public final ForgeConfigSpec.IntValue xpThreshold_9;

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

            builder.comment("XP thresholds for heart scaling");
            xpThreshold_1 = builder.comment("XP level required for 2 hearts").defineInRange("xpThreshold_1", 10, 1, 1000);
            xpThreshold_2 = builder.comment("XP level required for 3 hearts").defineInRange("xpThreshold_2", 20, 1, 1000);
            xpThreshold_3 = builder.comment("XP level required for 4 hearts").defineInRange("xpThreshold_3", 30, 1, 1000);
            xpThreshold_4 = builder.comment("XP level required for 5 hearts").defineInRange("xpThreshold_4", 40, 1, 1000);
            xpThreshold_5 = builder.comment("XP level required for 6 hearts").defineInRange("xpThreshold_5", 50, 1, 1000);
            xpThreshold_6 = builder.comment("XP level required for 8 hearts").defineInRange("xpThreshold_6", 75, 1, 1000);
            xpThreshold_7 = builder.comment("XP level required for 10 hearts").defineInRange("xpThreshold_7", 100, 1, 1000);
            xpThreshold_8 = builder.comment("XP level required for 12 hearts").defineInRange("xpThreshold_8", 150, 1, 1000);
            xpThreshold_9 = builder.comment("XP level required for 15 hearts").defineInRange("xpThreshold_9", 200, 1, 1000);
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



