package com.dividedby0.victorymod;

import net.minecraftforge.fml.common.Mod;

@Mod(VictoryMod.MODID)
public class VictoryMod {
    public static final String MODID = "victorymod";

    public VictoryMod() {
        WorldInit.init();
    }
}