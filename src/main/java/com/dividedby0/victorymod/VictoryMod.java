package com.dividedby0.victorymod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

@Mod(VictoryMod.MODID)
public class VictoryMod {
    public static final String MODID = "victorymod";

    public VictoryMod() {
        // Register the config spec - this makes the Config button available in the mods menu
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModConfigHandler.CONFIG_SPEC);
        
        WorldInit.init();
        MonumentTracker.init();
    }
}