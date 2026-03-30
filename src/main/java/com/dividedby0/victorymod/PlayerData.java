package com.dividedby0.victorymod;

import java.util.HashSet;
import java.util.Set;

public class PlayerData {

    private static final Set<String> collectedWools = new HashSet<>();

    public static void addWool(String color) {
        collectedWools.add(color);
    }

    public static boolean hasAll() {
        return collectedWools.size() >= 16;
    }
}