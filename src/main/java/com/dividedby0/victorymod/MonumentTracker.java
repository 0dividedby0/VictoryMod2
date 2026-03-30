package com.dividedby0.victorymod;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;

public class MonumentTracker {

    public static void init() {
        MinecraftForge.EVENT_BUS.register(MonumentTracker.class);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {

        if (!(event.getPlacedBlock().getBlock() instanceof net.minecraft.world.level.block.WoolCarpetBlock))
            return;

        String color = event.getPlacedBlock().getBlock().getName().getString();

        PlayerData.addWool(color);

        if (PlayerData.hasAll()) {
            event.getEntity().sendSystemMessage(
                net.minecraft.network.chat.Component.literal("§6VICTORY COMPLETE!")
            );
        }
    }
}