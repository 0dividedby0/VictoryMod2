package com.dividedby0.victorymod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;

public class HealthScalingHandler {

    private static final String INITIALIZED_KEY = "VictoryMod_HealthInitialized";
    private static final String TOTAL_HEARTS_KEY = "VictoryMod_TotalHearts";

    public static void init() {
        MinecraftForge.EVENT_BUS.register(HealthScalingHandler.class);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {

            CompoundTag tag = player.getPersistentData();

            if (!tag.getBoolean(INITIALIZED_KEY)) {
                tag.putBoolean(INITIALIZED_KEY, true);
                tag.putInt(TOTAL_HEARTS_KEY, 1); // start with 1 heart
            }

            applyHealth(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)) return;
        if (event.phase != TickEvent.Phase.END) return;

        updateFromXP(player);
        applyHealth(player);
    }

    private static void updateFromXP(ServerPlayer player) {

        CompoundTag tag = player.getPersistentData();

        int currentHearts = tag.getInt(TOTAL_HEARTS_KEY);
        int level = player.experienceLevel;

        int targetHearts = getHeartsFromLevel(level);

        // Only increase if XP would give MORE than current
        if (targetHearts > currentHearts) {
            tag.putInt(TOTAL_HEARTS_KEY, targetHearts);
        }
    }

    private static void applyHealth(ServerPlayer player) {

        CompoundTag tag = player.getPersistentData();
        int hearts = tag.getInt(TOTAL_HEARTS_KEY);

        double maxHealth = hearts * 2.0;

        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);

        if (player.getHealth() > maxHealth) {
            player.setHealth((float) maxHealth);
        }
    }

    private static int getHeartsFromLevel(int level) {
        if (level >= 200) return 15;
        if (level >= 150) return 12;
        if (level >= 100) return 10;
        if (level >= 75) return 8;
        if (level >= 50) return 6;
        if (level >= 40) return 5;
        if (level >= 30) return 4;
        if (level >= 20) return 3;
        if (level >= 10) return 2;
        return 1;
    }

    // 🔥 CALL THIS FROM HEART CONTAINER
    public static void addHeart(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();

        int hearts = tag.getInt(TOTAL_HEARTS_KEY);

        if (hearts < 15) {
            tag.putInt(TOTAL_HEARTS_KEY, hearts + 1);
        }
    }
}