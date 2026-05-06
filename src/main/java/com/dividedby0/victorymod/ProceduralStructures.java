package com.dividedby0.victorymod;

import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ProceduralStructures {

    private static final Set<String> PROCEDURAL = Set.of(
        "dungeon_white",
        "dungeon_magenta",
        "dungeon_lightblue",
        "dungeon_yellow",
        "dungeon_lime",
        "dungeon_pink",
        "dungeon_lightgray",
        "dungeon_cyan",
        "dungeon_purple",
        "dungeon_blue",
        "dungeon_green",
        "dungeon_black"
    );

    private ProceduralStructures() {}

    public static boolean supports(String name) {
        return PROCEDURAL.contains(name);
    }

    public static Set<String> getProceduralNames() {
        return PROCEDURAL;
    }

    public static Spec getSpec(String name) {
        return switch (name) {
            case "dungeon_white" -> new Spec(-24, -2, -24, 24, 18, 24);
            case "dungeon_magenta" -> new Spec(-23, -2, -23, 23, 26, 23);
            case "dungeon_lightblue" -> new Spec(-25, -2, -25, 25, 18, 25);
            case "dungeon_yellow" -> new Spec(-28, -2, -26, 28, 20, 26);
            case "dungeon_lime" -> new Spec(-24, -2, -24, 24, 20, 24);
            case "dungeon_pink" -> new Spec(-24, -2, -24, 24, 18, 24);
            case "dungeon_lightgray" -> new Spec(-25, -2, -25, 25, 18, 25);
            case "dungeon_cyan" -> new Spec(-24, -2, -24, 24, 18, 24);
            case "dungeon_purple" -> new Spec(-23, -2, -23, 23, 24, 23);
            case "dungeon_blue" -> new Spec(-24, -2, -24, 24, 28, 24);
            case "dungeon_green" -> new Spec(-26, -2, -26, 26, 20, 26);
            case "dungeon_black" -> new Spec(-26, -2, -26, 26, 22, 26);
            default -> throw new IllegalArgumentException("Unsupported procedural structure: " + name);
        };
    }

    public static boolean place(ServerLevel level, String name, BlockPos origin) {
        Builder b = new Builder(level, origin);
        switch (name) {
            case "dungeon_white" -> buildWhite(b);
            case "dungeon_magenta" -> buildMagenta(b);
            case "dungeon_lightblue" -> buildLightBlue(b);
            case "dungeon_yellow" -> buildYellow(b);
            case "dungeon_lime" -> buildLime(b);
            case "dungeon_pink" -> buildPink(b);
            case "dungeon_lightgray" -> buildLightGray(b);
            case "dungeon_cyan" -> buildCyan(b);
            case "dungeon_purple" -> buildPurple(b);
            case "dungeon_blue" -> buildBlue(b);
            case "dungeon_green" -> buildGreen(b);
            case "dungeon_black" -> buildBlack(b);
            default -> {
                return false;
            }
        }
        return b.changed;
    }

    private static void buildWhite(Builder b) {
        Theme t = new Theme(
            bs(Blocks.SMOOTH_QUARTZ), bs(Blocks.QUARTZ_BRICKS), bs(Blocks.PACKED_ICE), bs(Blocks.CHISELED_QUARTZ_BLOCK),
            bs(Blocks.POWDER_SNOW), bs(Blocks.BLUE_ICE), EntityType.STRAY, EntityType.SKELETON, EntityType.CAVE_SPIDER, Blocks.WHITE_WOOL
        );
        b.clear(-26, -2, -26, 26, 19, 26);
        basePad(b, 24, t.floor);
        ovalShell(b, 0, 0, 0, 14, 21, 18, 11, t.wall, true);
        wing(b, -19, -6, 7, 10, t.wall, t.floor);
        wing(b, 12, -6, 19, 10, t.wall, t.floor);
        colonnadeEntrance(b, t, 0, 20, 8);
        frozenMoatCross(b, t, 16);
        spiralGallery(b, t, -14, -8, 9, true);
        spiralGallery(b, t, 14, -8, 9, false);
        vaultWithApproach(b, t, 0, 2, -14, Dir.SOUTH, 10);
        braidPath(b, t, 16, 0xA11CE0L, true);
        placeTriSpawners(b, t, new int[][] {{-16, 1, 4}, {16, 1, 4}, {0, 1, -4}});
        decoyCache(b, t, -18, 1, -2, true);
        decoyCache(b, t, 18, 1, -2, true);
        iceNeedles(b, t, 18);
    }

    private static void buildMagenta(Builder b) {
        Theme t = new Theme(
            bs(Blocks.POLISHED_BLACKSTONE_BRICKS), bs(Blocks.OBSIDIAN), bs(Blocks.AMETHYST_BLOCK), bs(Blocks.PURPUR_BLOCK),
            bs(Blocks.CRYING_OBSIDIAN), bs(Blocks.SOUL_CAMPFIRE), EntityType.WITCH, EntityType.ENDERMITE, EntityType.CAVE_SPIDER, Blocks.MAGENTA_WOOL
        );
        b.clear(-25, -2, -25, 25, 27, 25);
        basePad(b, 23, t.floor);
        tallSpireCluster(b, t, 0, 0, 0, 17, 23);
        colonnadeEntrance(b, t, 0, 21, 9);
        stackedRuneChambers(b, t, 0, 4, 0, 15);
        ringBalconies(b, t, 14, new int[] {6, 11, 16});
        verticalTrapShaft(b, t, 0, 1, -10, 14);
        vaultWithApproach(b, t, 0, 17, 0, Dir.NORTH, 7);
        bridgeToShaft(b, t, -12, 6, 12, 6, 8);
        bridgeToShaft(b, t, 12, 11, -12, 11, 8);
        placeTriSpawners(b, t, new int[][] {{0, 5, 12}, {-10, 11, 0}, {10, 16, 0}});
        decoyCache(b, t, -13, 6, -13, true);
        decoyCache(b, t, 13, 11, 13, true);
    }

    private static void buildLightBlue(Builder b) {
        Theme t = new Theme(
            bs(Blocks.PRISMARINE_BRICKS), bs(Blocks.SMOOTH_QUARTZ), bs(Blocks.SEA_LANTERN), bs(Blocks.PRISMARINE),
            bs(Blocks.MAGMA_BLOCK), bs(Blocks.COBWEB), EntityType.DROWNED, EntityType.SPIDER, EntityType.SKELETON, Blocks.LIGHT_BLUE_WOOL
        );
        b.clear(-27, -2, -27, 27, 19, 27);
        basePad(b, 25, t.floor);
        basinOuterWalls(b, t, 23, 10);
        colonnadeEntrance(b, t, 0, 24, 8);
        dryCanals(b, t, 18);
        sunkenCourt(b, t, 0, 0, 0, 15);
        bridgeToShaft(b, t, -18, 3, 18, 3, 5);
        bridgeToShaft(b, t, -18, 3, -18, 3, 5);
        bridgeToShaft(b, t, 18, 3, 18, 3, 5);
        bridgeToShaft(b, t, 18, 3, -18, 3, 5);
        crossMaze(b, t, 14, 0xB10E11L);
        vaultWithApproach(b, t, 0, 2, -18, Dir.SOUTH, 10);
        placeTriSpawners(b, t, new int[][] {{-16, 1, 0}, {16, 1, 0}, {0, 1, 14}});
        decoyCache(b, t, -20, 1, -16, false);
        decoyCache(b, t, 20, 1, -16, false);
    }

    private static void buildYellow(Builder b) {
        Theme t = new Theme(
            bs(Blocks.CUT_SANDSTONE), bs(Blocks.SMOOTH_SANDSTONE), bs(Blocks.CHISELED_SANDSTONE), bs(Blocks.RED_SANDSTONE),
            bs(Blocks.TNT), bs(Blocks.MAGMA_BLOCK), EntityType.HUSK, EntityType.CAVE_SPIDER, EntityType.CREEPER, Blocks.YELLOW_WOOL
        );
        b.clear(-30, -2, -28, 30, 21, 28);
        basePad(b, 28, t.floor);
        canyonZiggurat(b, t);
        canyonEntrance(b, t, 0, 24);
        switchbackRamps(b, t, 20);
        buriedMaze(b, t, 17, 0xDE51A7L);
        falseTreasureTerraces(b, t, 19);
        vaultWithApproach(b, t, 0, 6, -16, Dir.SOUTH, 12);
        placeTriSpawners(b, t, new int[][] {{-18, 1, 10}, {18, 1, 10}, {0, 9, 6}});
        decoyCache(b, t, -21, 7, -8, true);
        decoyCache(b, t, 21, 7, -8, true);
    }

    private static void buildLime(Builder b) {
        Theme t = new Theme(
            bs(Blocks.MOSS_BLOCK), bs(Blocks.MOSSY_STONE_BRICKS), bs(Blocks.JUNGLE_LOG), bs(Blocks.ROOTED_DIRT),
            bs(Blocks.COBWEB), bs(Blocks.POWDER_SNOW), EntityType.SLIME, EntityType.CAVE_SPIDER, EntityType.ZOMBIE, Blocks.LIME_WOOL
        );
        b.clear(-26, -2, -26, 26, 21, 26);
        basePad(b, 24, t.floor);
        cenoteRuin(b, t);
        vineEntrance(b, t, 0, 21);
        overgrownMazeGarden(b, t, 17, 0x11A0E1L);
        canopyLadders(b, t, 15);
        sinkholeSpine(b, t, 0, 0, 0);
        vaultWithApproach(b, t, 0, 2, -16, Dir.SOUTH, 11);
        placeTriSpawners(b, t, new int[][] {{-16, 1, 8}, {16, 1, 8}, {0, 8, 0}});
        decoyCache(b, t, -18, 1, -18, false);
        decoyCache(b, t, 18, 1, -18, false);
    }

    private static void buildPink(Builder b) {
        Theme t = new Theme(
            bs(Blocks.CHERRY_PLANKS), bs(Blocks.BIRCH_PLANKS), bs(Blocks.CHERRY_LOG), bs(Blocks.PINK_TERRACOTTA),
            bs(Blocks.COBWEB), bs(Blocks.SOUL_CAMPFIRE), EntityType.SPIDER, EntityType.SKELETON, EntityType.CREEPER, Blocks.PINK_WOOL
        );
        b.clear(-26, -2, -26, 26, 19, 26);
        basePad(b, 24, t.floor);
        grandTheater(b, t);
        blossomGate(b, t, 0, 22);
        backstageCatwalks(b, t);
        curtainMaze(b, t, 16, 0x91AA0CL);
        orchestraPit(b, t, 0, 0, 6);
        vaultWithApproach(b, t, 0, 2, -17, Dir.SOUTH, 10);
        placeTriSpawners(b, t, new int[][] {{-14, 5, 8}, {14, 5, 8}, {0, 1, -2}});
        decoyCache(b, t, -18, 1, 14, true);
        decoyCache(b, t, 18, 1, 14, true);
    }

    private static void buildLightGray(Builder b) {
        Theme t = new Theme(
            bs(Blocks.STONE_BRICKS), bs(Blocks.CRACKED_STONE_BRICKS), bs(Blocks.TUFF), bs(Blocks.COBBLESTONE),
            bs(Blocks.COBWEB), bs(Blocks.MAGMA_BLOCK), EntityType.SILVERFISH, EntityType.ZOMBIE, EntityType.SKELETON, Blocks.LIGHT_GRAY_WOOL
        );
        b.clear(-27, -2, -27, 27, 19, 27);
        basePad(b, 25, t.floor);
        quarryKeep(b, t);
        brokenGate(b, t, 0, 23);
        quarryTrenches(b, t);
        buttressMaze(b, t, 17, 0x11687A9L);
        rubbleChutes(b, t);
        vaultWithApproach(b, t, 0, 2, -18, Dir.SOUTH, 11);
        placeTriSpawners(b, t, new int[][] {{-17, 1, 0}, {17, 1, 0}, {0, 7, 10}});
        decoyCache(b, t, -21, 1, -12, true);
        decoyCache(b, t, 21, 1, -12, true);
    }

    private static void buildCyan(Builder b) {
        Theme t = new Theme(
            bs(Blocks.PRISMARINE), bs(Blocks.PRISMARINE_BRICKS), bs(Blocks.CALCITE), bs(Blocks.DARK_PRISMARINE),
            bs(Blocks.MAGMA_BLOCK), bs(Blocks.COBWEB), EntityType.DROWNED, EntityType.ZOMBIE, EntityType.CAVE_SPIDER, Blocks.CYAN_WOOL
        );
        b.clear(-26, -2, -26, 26, 19, 26);
        basePad(b, 24, t.floor);
        reefTemple(b, t);
        reefMouthEntrance(b, t, 0, 21);
        ribHallways(b, t, 18);
        tidalLabyrinth(b, t, 16, 0xC1A6AEL);
        coralCrossings(b, t);
        vaultWithApproach(b, t, 0, 2, -16, Dir.SOUTH, 10);
        placeTriSpawners(b, t, new int[][] {{-15, 1, 6}, {15, 1, 6}, {0, 1, -4}});
        decoyCache(b, t, -19, 1, -16, false);
        decoyCache(b, t, 19, 1, -16, false);
    }

    private static void buildPurple(Builder b) {
        Theme t = new Theme(
            bs(Blocks.OBSIDIAN), bs(Blocks.CRYING_OBSIDIAN), bs(Blocks.AMETHYST_BLOCK), bs(Blocks.PURPUR_BLOCK),
            bs(Blocks.SOUL_CAMPFIRE), bs(Blocks.COBWEB), EntityType.ENDERMITE, EntityType.BLAZE, EntityType.WITCH, Blocks.PURPLE_WOOL
        );
        b.clear(-25, -2, -25, 25, 25, 25);
        basePad(b, 23, t.floor);
        voidArchive(b, t);
        portalMouthEntrance(b, t, 0, 22);
        floatingStacks(b, t);
        runeGridMaze(b, t, 15, 0xA11CE5L);
        verticalTrapShaft(b, t, 0, 1, -12, 13);
        vaultWithApproach(b, t, 0, 14, 0, Dir.NORTH, 7);
        placeTriSpawners(b, t, new int[][] {{-12, 8, 0}, {12, 8, 0}, {0, 3, 12}});
        decoyCache(b, t, -16, 8, -16, true);
        decoyCache(b, t, 16, 8, -16, true);
    }

    private static void buildBlue(Builder b) {
        Theme t = new Theme(
            bs(Blocks.PRISMARINE), bs(Blocks.LAPIS_BLOCK), bs(Blocks.SEA_LANTERN), bs(Blocks.SMOOTH_QUARTZ),
            bs(Blocks.MAGMA_BLOCK), bs(Blocks.COBWEB), EntityType.STRAY, EntityType.DROWNED, EntityType.SPIDER, Blocks.BLUE_WOOL
        );
        b.clear(-26, -2, -26, 26, 29, 26);
        basePad(b, 24, t.floor);
        lighthouseCitadel(b, t);
        lighthouseDoor(b, t, 0, 22);
        helicalStairRings(b, t, 18);
        stormBridgeMaze(b, t, 16, 0xB10E55L);
        beaconCore(b, t);
        vaultWithApproach(b, t, 0, 19, 0, Dir.NORTH, 8);
        placeTriSpawners(b, t, new int[][] {{-12, 7, 0}, {12, 13, 0}, {0, 3, -12}});
        decoyCache(b, t, -17, 13, 16, false);
        decoyCache(b, t, 17, 7, 16, false);
    }

    private static void buildGreen(Builder b) {
        Theme t = new Theme(
            bs(Blocks.MOSSY_STONE_BRICKS), bs(Blocks.JUNGLE_PLANKS), bs(Blocks.JUNGLE_LOG), bs(Blocks.MUD_BRICKS),
            bs(Blocks.COBWEB), bs(Blocks.TNT), EntityType.CREEPER, EntityType.CAVE_SPIDER, EntityType.ZOMBIE, Blocks.GREEN_WOOL
        );
        b.clear(-28, -2, -28, 28, 21, 28);
        basePad(b, 26, t.floor);
        calderaTemple(b, t);
        jungleMawEntrance(b, t, 0, 24);
        steppedRuinPaths(b, t);
        explosiveMaze(b, t, 18, 0x9AEE21L);
        rootBridges(b, t);
        vaultWithApproach(b, t, 0, 3, -18, Dir.SOUTH, 12);
        placeTriSpawners(b, t, new int[][] {{-18, 1, 12}, {18, 1, 12}, {0, 9, 2}});
        decoyCache(b, t, -20, 3, -16, true);
        decoyCache(b, t, 20, 3, -16, true);
    }

    private static void buildBlack(Builder b) {
        Theme t = new Theme(
            bs(Blocks.POLISHED_BLACKSTONE_BRICKS), bs(Blocks.BASALT), bs(Blocks.GILDED_BLACKSTONE), bs(Blocks.NETHER_BRICKS),
            bs(Blocks.SOUL_CAMPFIRE), bs(Blocks.MAGMA_BLOCK), EntityType.WITHER_SKELETON, EntityType.BLAZE, EntityType.CREEPER, Blocks.BLACK_WOOL
        );
        b.clear(-28, -2, -28, 28, 23, 28);
        basePad(b, 26, t.floor);
        basaltMaw(b, t);
        hellgateEntrance(b, t, 0, 24);
        crucibleWalks(b, t);
        infernalMaze(b, t, 18, 0xB1AC09L);
        ashPits(b, t);
        vaultWithApproach(b, t, 0, 3, -18, Dir.SOUTH, 12);
        placeTriSpawners(b, t, new int[][] {{-18, 2, 8}, {18, 2, 8}, {0, 10, -4}});
        decoyCache(b, t, -22, 2, -18, true);
        decoyCache(b, t, 22, 2, -18, true);
    }

    private static void basePad(Builder b, int radius, BlockState floor) {
        b.fill(-radius, -2, -radius, radius, -2, radius, bs(Blocks.BEDROCK));
        b.fill(-radius, -1, -radius, radius, -1, radius, floor);
    }

    private static void wing(Builder b, int x1, int z1, int x2, int z2, BlockState wall, BlockState floor) {
        b.hollowBox(x1, 0, z1, x2, 8, z2, wall, bs(Blocks.AIR));
        b.fill(x1 + 1, 0, z1 + 1, x2 - 1, 0, z2 - 1, floor);
    }

    private static void ovalShell(Builder b, int cx, int cy, int cz, int rx, int rz, int topY, int thickness, BlockState wall, boolean roof) {
        for (int x = cx - rx; x <= cx + rx; x++) {
            for (int z = cz - rz; z <= cz + rz; z++) {
                double dx = (double) (x - cx) / rx;
                double dz = (double) (z - cz) / rz;
                double dist = dx * dx + dz * dz;
                if (dist > 1.0) {
                    continue;
                }
                if (dist >= 1.0 - (double) thickness / Math.max(rx, rz)) {
                    b.fill(x, cy, z, x, topY, z, wall);
                } else if (roof && dist < 0.85) {
                    int roofY = topY - (int) Math.round((1.0 - dist) * 5.0);
                    b.set(x, roofY, z, wall);
                }
            }
        }
    }

    private static void colonnadeEntrance(Builder b, Theme t, int centerX, int frontZ, int depth) {
        b.fill(centerX - 5, 0, frontZ - depth, centerX + 5, 8, frontZ + 1, t.wall);
        b.fill(centerX - 3, 1, frontZ - depth + 1, centerX + 3, 6, frontZ, bs(Blocks.AIR));
        b.fill(centerX - 1, 1, frontZ - depth - 4, centerX + 1, 5, frontZ - depth + 1, bs(Blocks.AIR));
        for (int x = centerX - 5; x <= centerX + 5; x += 2) {
            b.fill(x, 1, frontZ - depth + 1, x, 6, frontZ, t.accent);
        }
        b.fill(centerX - 2, 7, frontZ - depth + 1, centerX + 2, 8, frontZ, t.detail);
    }

    private static void frozenMoatCross(Builder b, Theme t, int span) {
        b.fill(-span, 0, -2, span, 0, 2, t.hazardB);
        b.fill(-2, 0, -span, 2, 0, span, t.hazardB);
        b.fill(-span, 1, -1, span, 1, 1, bs(Blocks.AIR));
        b.fill(-1, 1, -span, 1, 1, span, bs(Blocks.AIR));
    }

    private static void spiralGallery(Builder b, Theme t, int cx, int cz, int height, boolean clockwise) {
        b.cylinderShell(cx, cz, 0, height, 4, t.wall);
        for (int i = 0; i < height * 2; i++) {
            int ring = i % 8;
            int x = switch (ring) {
                case 0, 1 -> 3;
                case 2, 3 -> 2;
                case 4, 5 -> -3;
                default -> -2;
            };
            int z = switch (ring) {
                case 0, 7 -> -3;
                case 1, 2 -> 2;
                case 3, 4 -> 3;
                default -> -2;
            };
            if (!clockwise) {
                x = -x;
            }
            b.set(cx + x, 1 + i / 2, cz + z, t.accent);
        }
        b.spawner(cx, 1, cz, t.secondary);
    }

    private static void braidPath(Builder b, Theme t, int radius, long seed, boolean addCross) {
        crossMaze(b, t, radius, seed);
        if (addCross) {
            for (int i = -radius; i <= radius; i += 4) {
                b.set(i, 1, i / 2, t.hazardA);
                b.set(-i, 1, i / 2, t.hazardB);
            }
        }
    }

    private static void tallSpireCluster(Builder b, Theme t, int cx, int cy, int cz, int radius, int height) {
        b.cylinderShell(cx, cz, cy, cy + height, radius, t.wall);
        b.cylinderShell(cx, cz, cy, cy + height, radius - 5, t.accent);
        for (int dx = -10; dx <= 10; dx += 10) {
            for (int dz = -10; dz <= 10; dz += 10) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                b.cylinderShell(cx + dx, cz + dz, cy, cy + height - 8, 5, t.wall);
            }
        }
        b.fill(-12, 0, -12, 12, 0, 12, t.floor);
        b.opening(-2, 1, 17, 2, 6, 23);
    }

    private static void stackedRuneChambers(Builder b, Theme t, int cx, int baseY, int cz, int span) {
        for (int y = baseY; y <= 16; y += 5) {
            b.ring(cx - span, cz - span, cx + span, cz + span, y, t.detail);
            b.ring(cx - span + 2, cz - span + 2, cx + span - 2, cz + span - 2, y, t.accent);
        }
        b.fill(-span + 2, 1, -span + 2, span - 2, 3, span - 2, bs(Blocks.AIR));
        b.fill(-span + 4, 6, -span + 4, span - 4, 8, span - 4, bs(Blocks.AIR));
        b.fill(-span + 6, 11, -span + 6, span - 6, 13, span - 6, bs(Blocks.AIR));
    }

    private static void ringBalconies(Builder b, Theme t, int radius, int[] ys) {
        for (int y : ys) {
            b.ring(-radius, -radius, radius, radius, y, t.detail);
            b.opening(-1, y, -radius, 1, y, radius);
            b.opening(-radius, y, -1, radius, y, 1);
        }
    }

    private static void verticalTrapShaft(Builder b, Theme t, int cx, int y, int cz, int depth) {
        b.fill(cx - 3, y, cz - 3, cx + 3, y + depth, cz + 3, t.wall);
        b.fill(cx - 2, y, cz - 2, cx + 2, y + depth, cz + 2, bs(Blocks.AIR));
        for (int i = 0; i < depth; i += 3) {
            b.fill(cx - 2, y + i, cz, cx + 2, y + i, cz, t.hazardA);
            b.spawner(cx, y + i + 1, cz - 1, i % 2 == 0 ? t.primary : t.secondary);
        }
        b.fill(cx - 1, y + depth, cz - 1, cx + 1, y + depth, cz + 1, t.accent);
    }

    private static void basinOuterWalls(Builder b, Theme t, int radius, int height) {
        b.hollowBox(-radius, 0, -radius, radius, height, radius, t.wall, bs(Blocks.AIR));
        b.ring(-radius + 2, -radius + 2, radius - 2, radius - 2, 4, t.detail);
        b.ring(-radius + 4, -radius + 4, radius - 4, radius - 4, 7, t.accent);
    }

    private static void dryCanals(Builder b, Theme t, int span) {
        b.fill(-span, 0, -3, span, 2, 3, t.detail);
        b.fill(-3, 0, -span, 3, 2, span, t.detail);
        b.fill(-span + 1, 1, -2, span - 1, 1, 2, bs(Blocks.AIR));
        b.fill(-2, 1, -span + 1, 2, 1, span - 1, bs(Blocks.AIR));
        for (int i = -span + 2; i <= span - 2; i += 4) {
            b.set(i, 0, 0, t.hazardA);
            b.set(0, 0, i, t.hazardA);
        }
    }

    private static void sunkenCourt(Builder b, Theme t, int cx, int cy, int cz, int radius) {
        b.fill(cx - radius, cy, cz - radius, cx + radius, cy, cz + radius, t.floor);
        b.fill(cx - radius + 2, cy + 1, cz - radius + 2, cx + radius - 2, cy + 3, cz + radius - 2, bs(Blocks.AIR));
        for (int step = 0; step < 4; step++) {
            b.ring(cx - radius + step * 2, cz - radius + step * 2, cx + radius - step * 2, cz + radius - step * 2, cy + step, t.wall);
        }
    }

    private static void canyonZiggurat(Builder b, Theme t) {
        for (int level = 0; level < 5; level++) {
            int x = 24 - level * 4;
            int z = 20 - level * 3;
            b.fill(-x, level, -z, x, level + 2, z, t.wall);
            b.fill(-x + 1, level + 1, -z + 1, x - 1, level + 2, z - 1, bs(Blocks.AIR));
        }
    }

    private static void canyonEntrance(Builder b, Theme t, int centerX, int frontZ) {
        b.fill(centerX - 6, 0, frontZ - 11, centerX + 6, 9, frontZ + 1, t.wall);
        b.fill(centerX - 2, 1, frontZ - 10, centerX + 2, 6, frontZ, bs(Blocks.AIR));
        b.fill(centerX - 5, 1, frontZ - 8, centerX - 4, 7, frontZ - 2, t.accent);
        b.fill(centerX + 4, 1, frontZ - 8, centerX + 5, 7, frontZ - 2, t.accent);
    }

    private static void switchbackRamps(Builder b, Theme t, int span) {
        for (int y = 1; y <= 8; y++) {
            int offset = (y - 1) * 2;
            b.fill(-span + offset, y, 14 - offset, span - 4, y, 15 - offset, t.detail);
            b.fill(-span + 4, y, -14 + offset, span - offset, y, -13 + offset, t.detail);
        }
    }

    private static void buriedMaze(Builder b, Theme t, int radius, long seed) {
        crossMaze(b, t, radius, seed);
        for (int x = -radius + 1; x <= radius - 1; x += 4) {
            for (int z = -radius + 1; z <= radius - 1; z += 4) {
                b.set(x, 1, z, ((x + z) & 4) == 0 ? t.hazardA : t.hazardB);
            }
        }
    }

    private static void falseTreasureTerraces(Builder b, Theme t, int radius) {
        for (int i = 0; i < 4; i++) {
            b.ring(-radius + i * 3, -radius + i * 2, radius - i * 3, radius - i * 2, 2 + i, t.accent);
        }
        decoyCache(b, t, 0, 8, 12, true);
    }

    private static void cenoteRuin(Builder b, Theme t) {
        b.hollowBox(-22, 0, -22, 22, 12, 22, t.wall, bs(Blocks.AIR));
        b.fill(-10, 0, -10, 10, 0, 10, bs(Blocks.AIR));
        b.fill(-12, -1, -12, 12, -1, 12, t.hazardB);
        for (int i = -20; i <= 20; i += 5) {
            b.fill(i, 1, -22, i, 9, -22, t.accent);
            b.fill(i, 1, 22, i, 9, 22, t.accent);
        }
    }

    private static void vineEntrance(Builder b, Theme t, int centerX, int frontZ) {
        b.fill(centerX - 5, 0, frontZ - 8, centerX + 5, 8, frontZ + 1, t.wall);
        b.fill(centerX - 2, 1, frontZ - 7, centerX + 2, 5, frontZ, bs(Blocks.AIR));
        b.fill(centerX - 5, 6, frontZ - 5, centerX + 5, 8, frontZ - 3, t.accent);
    }

    private static void overgrownMazeGarden(Builder b, Theme t, int radius, long seed) {
        crossMaze(b, t, radius, seed);
        for (int x = -radius; x <= radius; x += 6) {
            for (int z = -radius; z <= radius; z += 6) {
                b.fill(x - 1, 1, z - 1, x + 1, 2, z + 1, t.accent);
            }
        }
    }

    private static void canopyLadders(Builder b, Theme t, int radius) {
        b.ring(-radius, -radius, radius, radius, 8, t.detail);
        for (int x = -radius; x <= radius; x += radius * 2) {
            b.fill(x, 1, -radius + 2, x, 8, -radius + 2, t.accent);
        }
    }

    private static void sinkholeSpine(Builder b, Theme t, int cx, int cy, int cz) {
        b.fill(cx - 2, cy, cz - 16, cx + 2, cy + 8, cz + 10, t.wall);
        b.fill(cx - 1, cy + 1, cz - 15, cx + 1, cy + 7, cz + 9, bs(Blocks.AIR));
        for (int z = cz - 12; z <= cz + 8; z += 4) {
            b.spawner(cx, cy + 1 + (z & 3), z, z % 8 == 0 ? t.primary : t.secondary);
        }
    }

    private static void grandTheater(Builder b, Theme t) {
        b.hollowBox(-22, 0, -22, 22, 15, 22, t.wall, bs(Blocks.AIR));
        for (int row = 0; row < 5; row++) {
            b.fill(-18 + row * 2, row + 1, 6 + row * 2, 18 - row * 2, row + 1, 8 + row * 2, t.detail);
        }
        b.fill(-10, 1, -18, 10, 5, -6, t.accent);
        b.fill(-8, 2, -17, 8, 4, -7, bs(Blocks.AIR));
    }

    private static void blossomGate(Builder b, Theme t, int centerX, int frontZ) {
        b.fill(centerX - 7, 0, frontZ - 8, centerX + 7, 8, frontZ + 1, t.wall);
        b.fill(centerX - 3, 1, frontZ - 7, centerX + 3, 5, frontZ, bs(Blocks.AIR));
        b.fill(centerX - 6, 6, frontZ - 6, centerX + 6, 8, frontZ - 2, t.detail);
    }

    private static void backstageCatwalks(Builder b, Theme t) {
        b.fill(-20, 6, -4, 20, 6, -4, t.accent);
        b.fill(-20, 6, 10, 20, 6, 10, t.accent);
        b.fill(-20, 6, -4, -20, 6, 10, t.accent);
        b.fill(20, 6, -4, 20, 6, 10, t.accent);
    }

    private static void curtainMaze(Builder b, Theme t, int radius, long seed) {
        crossMaze(b, t, radius, seed);
        for (int z = -radius + 1; z <= radius - 1; z += 3) {
            b.fill(-radius + 2, 1, z, radius - 2, 3, z, t.detail);
            b.opening(((z / 3) & 1) == 0 ? -4 : 4, 1, z, ((z / 3) & 1) == 0 ? -2 : 6, 3, z);
        }
    }

    private static void orchestraPit(Builder b, Theme t, int cx, int cy, int cz) {
        b.fill(cx - 8, cy, cz - 4, cx + 8, cy + 3, cz + 4, t.wall);
        b.fill(cx - 7, cy + 1, cz - 3, cx + 7, cy + 3, cz + 3, bs(Blocks.AIR));
        for (int x = cx - 6; x <= cx + 6; x += 3) {
            b.spawner(x, cy + 1, cz, x % 2 == 0 ? t.secondary : t.tertiary);
        }
    }

    private static void quarryKeep(Builder b, Theme t) {
        b.hollowBox(-23, 0, -23, 23, 12, 23, t.wall, bs(Blocks.AIR));
        b.fill(-23, 0, -4, 23, 0, 4, bs(Blocks.AIR));
        b.fill(-4, 0, -23, 4, 0, 23, bs(Blocks.AIR));
        for (int x = -20; x <= 20; x += 8) {
            b.fill(x, 1, -23, x + 2, 10, -18, t.accent);
            b.fill(x, 1, 18, x + 2, 10, 23, t.accent);
        }
    }

    private static void brokenGate(Builder b, Theme t, int centerX, int frontZ) {
        b.fill(centerX - 6, 0, frontZ - 9, centerX + 6, 9, frontZ + 1, t.wall);
        b.fill(centerX - 2, 1, frontZ - 8, centerX + 2, 5, frontZ, bs(Blocks.AIR));
        b.fill(centerX - 6, 1, frontZ - 7, centerX - 4, 8, frontZ - 3, t.detail);
        b.fill(centerX + 4, 1, frontZ - 5, centerX + 6, 6, frontZ - 1, t.detail);
    }

    private static void quarryTrenches(Builder b, Theme t) {
        for (int x = -18; x <= 18; x += 9) {
            b.fill(x - 2, 0, -16, x + 2, 3, 16, bs(Blocks.AIR));
            b.fill(x - 2, -1, -16, x + 2, -1, 16, t.hazardB);
        }
    }

    private static void buttressMaze(Builder b, Theme t, int radius, long seed) {
        crossMaze(b, t, radius, seed);
        for (int x = -radius; x <= radius; x += 6) {
            b.fill(x, 1, -radius, x, 7, radius, t.accent);
            b.opening(x, 1, x / 2, x, 3, x / 2 + 2);
        }
    }

    private static void rubbleChutes(Builder b, Theme t) {
        for (int i = -18; i <= 18; i += 6) {
            b.set(i, 1, -10, t.hazardA);
            b.set(-i, 1, 10, t.hazardB);
        }
    }

    private static void reefTemple(Builder b, Theme t) {
        b.cylinderShell(0, 0, 0, 12, 19, t.wall);
        for (int i = 0; i < 6; i++) {
            int r = 17 - i * 2;
            b.ring(-r, -r, r, r, i + 1, i % 2 == 0 ? t.detail : t.accent);
        }
    }

    private static void reefMouthEntrance(Builder b, Theme t, int centerX, int frontZ) {
        b.fill(centerX - 5, 0, frontZ - 7, centerX + 5, 8, frontZ + 1, t.wall);
        b.fill(centerX - 2, 1, frontZ - 6, centerX + 2, 5, frontZ, bs(Blocks.AIR));
        b.fill(centerX - 5, 4, frontZ - 6, centerX + 5, 6, frontZ - 4, t.accent);
    }

    private static void ribHallways(Builder b, Theme t, int span) {
        for (int z = -span; z <= span; z += 4) {
            b.fill(-span, 1, z, -span, 8, z, t.detail);
            b.fill(span, 1, z, span, 8, z, t.detail);
        }
    }

    private static void tidalLabyrinth(Builder b, Theme t, int radius, long seed) {
        crossMaze(b, t, radius, seed);
        for (int x = -radius + 2; x <= radius - 2; x += 5) {
            b.fill(x, 1, -radius + 1, x, 2, radius - 1, t.detail);
            b.opening(x, 1, ((x / 5) & 1) == 0 ? -4 : 4, x, 2, ((x / 5) & 1) == 0 ? -2 : 6);
        }
    }

    private static void coralCrossings(Builder b, Theme t) {
        b.fill(-16, 4, 0, 16, 4, 0, t.accent);
        b.fill(0, 4, -16, 0, 4, 16, t.accent);
    }

    private static void voidArchive(Builder b, Theme t) {
        b.hollowBox(-21, 0, -21, 21, 18, 21, t.wall, bs(Blocks.AIR));
        for (int y = 4; y <= 16; y += 4) {
            b.ring(-18, -18, 18, 18, y, t.detail);
        }
        b.fill(-6, 0, -6, 6, 14, 6, bs(Blocks.AIR));
        for (int x = -16; x <= 16; x += 8) {
            b.fill(x, 1, -14, x + 2, 14, 14, t.accent);
        }
    }

    private static void portalMouthEntrance(Builder b, Theme t, int centerX, int frontZ) {
        b.fill(centerX - 6, 0, frontZ - 8, centerX + 6, 10, frontZ + 1, t.wall);
        b.fill(centerX - 2, 1, frontZ - 7, centerX + 2, 6, frontZ, bs(Blocks.AIR));
        b.fill(centerX - 4, 2, frontZ - 6, centerX + 4, 8, frontZ - 2, t.accent);
    }

    private static void floatingStacks(Builder b, Theme t) {
        for (int x = -12; x <= 12; x += 12) {
            b.fill(x - 2, 8, -12, x + 2, 13, -6, t.detail);
            b.fill(x - 2, 8, 6, x + 2, 13, 12, t.detail);
        }
    }

    private static void runeGridMaze(Builder b, Theme t, int radius, long seed) {
        crossMaze(b, t, radius, seed);
        for (int x = -radius + 1; x <= radius - 1; x += 4) {
            for (int z = -radius + 1; z <= radius - 1; z += 4) {
                if (((x + z) & 4) == 0) {
                    b.fill(x, 1, z, x, 4, z, t.accent);
                }
            }
        }
    }

    private static void lighthouseCitadel(Builder b, Theme t) {
        b.cylinderShell(0, 0, 0, 24, 14, t.wall);
        b.cylinderShell(0, 0, 0, 28, 8, t.detail);
        for (int i = 0; i < 4; i++) {
            int r = 18 + i;
            b.ring(-r, -r, r, r, i + 1, i % 2 == 0 ? t.accent : t.detail);
        }
    }

    private static void lighthouseDoor(Builder b, Theme t, int centerX, int frontZ) {
        b.fill(centerX - 4, 0, frontZ - 7, centerX + 4, 8, frontZ + 1, t.wall);
        b.fill(centerX - 1, 1, frontZ - 6, centerX + 1, 6, frontZ, bs(Blocks.AIR));
        b.fill(centerX - 3, 7, frontZ - 5, centerX + 3, 9, frontZ - 1, t.accent);
    }

    private static void helicalStairRings(Builder b, Theme t, int radius) {
        for (int y = 2; y <= 18; y += 2) {
            int inset = y / 2;
            b.ring(-radius + inset, -radius + inset, radius - inset, radius - inset, y, t.detail);
            b.opening(-1, y, -radius + inset, 1, y, -radius + inset + 3);
        }
    }

    private static void stormBridgeMaze(Builder b, Theme t, int radius, long seed) {
        crossMaze(b, t, radius, seed);
        for (int i = -radius + 2; i <= radius - 2; i += 4) {
            b.fill(-radius + 1, 7, i, radius - 1, 7, i, t.accent);
            b.opening(i / 2, 7, i, i / 2 + 2, 7, i);
        }
    }

    private static void beaconCore(Builder b, Theme t) {
        b.fill(-2, 19, -2, 2, 24, 2, t.accent);
        b.fill(-1, 25, -1, 1, 27, 1, bs(Blocks.SEA_LANTERN));
    }

    private static void calderaTemple(Builder b, Theme t) {
        for (int step = 0; step < 5; step++) {
            int x = 24 - step * 4;
            int z = 24 - step * 4;
            b.fill(-x, step, -z, x, step + 2, z, t.wall);
            b.fill(-x + 1, step + 1, -z + 1, x - 1, step + 2, z - 1, bs(Blocks.AIR));
        }
        b.fill(-8, 0, -8, 8, 8, 8, bs(Blocks.AIR));
    }

    private static void jungleMawEntrance(Builder b, Theme t, int centerX, int frontZ) {
        b.fill(centerX - 7, 0, frontZ - 9, centerX + 7, 10, frontZ + 1, t.wall);
        b.fill(centerX - 2, 1, frontZ - 8, centerX + 2, 6, frontZ, bs(Blocks.AIR));
        b.fill(centerX - 7, 5, frontZ - 7, centerX + 7, 7, frontZ - 5, t.accent);
    }

    private static void steppedRuinPaths(Builder b, Theme t) {
        for (int y = 1; y <= 9; y += 2) {
            b.fill(-20 + y, y, 14 - y, 20, y, 15 - y, t.detail);
            b.fill(-20, y, -14 + y, 20 - y, y, -13 + y, t.detail);
        }
    }

    private static void explosiveMaze(Builder b, Theme t, int radius, long seed) {
        crossMaze(b, t, radius, seed);
        for (int x = -radius + 2; x <= radius - 2; x += 5) {
            for (int z = -radius + 2; z <= radius - 2; z += 5) {
                b.set(x, 0, z, t.hazardB);
                if (((x + z) & 1) == 0) {
                    b.decoyChest(x, 1, z, true);
                }
            }
        }
    }

    private static void rootBridges(Builder b, Theme t) {
        b.fill(-18, 6, -2, 18, 6, 2, t.accent);
        b.fill(-2, 6, -18, 2, 6, 18, t.accent);
    }

    private static void basaltMaw(Builder b, Theme t) {
        b.hollowBox(-24, 0, -24, 24, 14, 24, t.wall, bs(Blocks.AIR));
        for (int i = 0; i < 6; i++) {
            int inset = i * 3;
            b.ring(-24 + inset, -24 + inset, 24 - inset, 24 - inset, i + 1, i % 2 == 0 ? t.detail : t.accent);
        }
        b.fill(-10, 0, -10, 10, 0, 10, bs(Blocks.MAGMA_BLOCK));
        b.fill(-6, 1, -6, 6, 8, 6, bs(Blocks.AIR));
    }

    private static void hellgateEntrance(Builder b, Theme t, int centerX, int frontZ) {
        b.fill(centerX - 7, 0, frontZ - 10, centerX + 7, 11, frontZ + 1, t.wall);
        b.fill(centerX - 2, 1, frontZ - 9, centerX + 2, 7, frontZ, bs(Blocks.AIR));
        b.fill(centerX - 6, 2, frontZ - 8, centerX + 6, 9, frontZ - 4, t.accent);
    }

    private static void crucibleWalks(Builder b, Theme t) {
        b.fill(-20, 5, 0, 20, 5, 0, t.detail);
        b.fill(0, 5, -20, 0, 5, 20, t.detail);
        b.fill(-18, 9, -2, 18, 9, 2, t.accent);
    }

    private static void infernalMaze(Builder b, Theme t, int radius, long seed) {
        crossMaze(b, t, radius, seed);
        for (int i = -radius + 1; i <= radius - 1; i += 4) {
            b.fill(i, 1, -radius + 1, i, 4, radius - 1, t.hazardB);
            b.opening(i, 1, i / 2, i, 4, i / 2 + 2);
        }
    }

    private static void ashPits(Builder b, Theme t) {
        for (int x = -16; x <= 16; x += 8) {
            b.fill(x - 2, 1, -14, x + 2, 3, -10, t.hazardA);
            b.spawner(x, 4, -12, x % 16 == 0 ? t.primary : t.secondary);
        }
    }

    private static void vaultWithApproach(Builder b, Theme t, int chestX, int baseY, int chestZ, Dir entranceSide, int corridorLength) {
        b.fill(chestX - 4, baseY, chestZ - 4, chestX + 4, baseY + 5, chestZ + 4, bs(Blocks.BEDROCK));
        b.fill(chestX - 3, baseY + 1, chestZ - 3, chestX + 3, baseY + 4, chestZ + 3, bs(Blocks.AIR));
        b.rewardChest(chestX, baseY + 2, chestZ, t.rewardWool);
        b.spawner(chestX - 2, baseY + 2, chestZ - 2, t.primary);
        b.spawner(chestX + 2, baseY + 2, chestZ + 2, t.secondary);

        int dx = entranceSide.dx;
        int dz = entranceSide.dz;
        int plugX = chestX + dx * 4;
        int plugZ = chestZ + dz * 4;
        int hallStartX = chestX + dx * (4 + corridorLength);
        int hallStartZ = chestZ + dz * (4 + corridorLength);
        int sideX = dz;
        int sideZ = dx;

        b.fill(Math.min(plugX, hallStartX) - Math.abs(sideX), baseY + 1, Math.min(plugZ, hallStartZ) - Math.abs(sideZ),
            Math.max(plugX, hallStartX) + Math.abs(sideX), baseY + 3, Math.max(plugZ, hallStartZ) + Math.abs(sideZ), bs(Blocks.AIR));
        b.fill(Math.min(plugX, hallStartX) - Math.abs(sideX), baseY, Math.min(plugZ, hallStartZ) - Math.abs(sideZ),
            Math.max(plugX, hallStartX) + Math.abs(sideX), baseY, Math.max(plugZ, hallStartZ) + Math.abs(sideZ), t.floor);
        b.fill(plugX - Math.abs(sideX), baseY + 1, plugZ - Math.abs(sideZ), plugX + Math.abs(sideX), baseY + 3, plugZ + Math.abs(sideZ), bs(Blocks.OBSIDIAN));
        for (int i = 2; i < corridorLength; i += 3) {
            int tx = chestX + dx * (4 + i);
            int tz = chestZ + dz * (4 + i);
            b.set(tx, baseY + 1, tz, (i & 1) == 0 ? t.hazardA : t.hazardB);
            b.spawner(tx + sideX, baseY + 1, tz + sideZ, (i & 1) == 0 ? t.tertiary : t.primary);
        }
    }

    private static void bridgeToShaft(Builder b, Theme t, int x1, int y1, int z1, int y2, int z2) {
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);
        int y = Math.max(y1, y2);
        b.fill(x1 - 1, y, minZ, x1 + 1, y, maxZ, t.detail);
    }

    private static void placeTriSpawners(Builder b, Theme t, int[][] positions) {
        EntityType<?>[] types = {t.primary, t.secondary, t.tertiary};
        for (int i = 0; i < positions.length; i++) {
            int[] p = positions[i];
            b.spawner(p[0], p[1], p[2], types[i % types.length]);
        }
    }

    private static void decoyCache(Builder b, Theme t, int x, int y, int z, boolean trapped) {
        b.fill(x - 2, y, z - 2, x + 2, y + 3, z + 2, t.wall);
        b.fill(x - 1, y + 1, z - 1, x + 1, y + 2, z + 1, bs(Blocks.AIR));
        b.decoyChest(x, y + 1, z, trapped);
        b.set(x, y, z, bs(Blocks.TNT));
        b.opening(x - 1, y + 1, z + 2, x + 1, y + 2, z + 2);
    }

    private static void iceNeedles(Builder b, Theme t, int radius) {
        for (int i = -radius; i <= radius; i += 6) {
            b.fill(i, 1, -radius, i, 8, -radius, t.accent);
            b.fill(-i, 1, radius, -i, 8, radius, t.accent);
        }
    }

    private static void crossMaze(Builder b, Theme t, int radius, long seed) {
        MazeData maze = MazeData.generate(8, seed);
        int min = -radius;
        int max = radius;
        int wallTop = 4;

        b.fill(min - 1, 0, min - 1, max + 1, wallTop, max + 1, t.wall);
        b.fill(min, 1, min, max, wallTop - 1, max, bs(Blocks.AIR));

        for (int x = min; x <= max; x += 2) {
            for (int z = min; z <= max; z += 2) {
                b.opening(x, 1, z, x, 3, z);
            }
        }

        int offset = radius - 1;
        for (int cx = 0; cx < maze.cells; cx++) {
            for (int cz = 0; cz < maze.cells; cz++) {
                int wx = cx * 4 - offset * 2 + 1;
                int wz = cz * 4 - offset * 2 + 1;
                carveMazeCell(b, t, maze, cx, cz, wx, wz);
            }
        }

        b.opening(-2, 1, max, 2, 3, max + 2);
        b.fill(-1, 0, max, 1, 0, max + 6, t.floor);
    }

    private static void carveMazeCell(Builder b, Theme t, MazeData maze, int cx, int cz, int wx, int wz) {
        b.fill(wx - 1, 1, wz - 1, wx + 1, 3, wz + 1, bs(Blocks.AIR));
        EnumSet<Dir> dirs = maze.connections[cx][cz];
        for (Dir dir : dirs) {
            b.fill(wx + dir.dx, 1, wz + dir.dz, wx + dir.dx * 2, 3, wz + dir.dz * 2, bs(Blocks.AIR));
        }

        int degree = dirs.size();
        if (degree >= 3 && ((cx + cz) & 1) == 0) {
            b.spawner(wx, 1, wz, (cx & 1) == 0 ? t.primary : t.secondary);
        } else if (degree == 1 && !(cx == maze.startX && cz == maze.startZ)) {
            if (((cx * 13 + cz * 17) & 1) == 0) {
                b.decoyChest(wx, 1, wz, true);
                b.set(wx, 0, wz, bs(Blocks.TNT));
            } else {
                b.set(wx, 1, wz, ((cx + cz) & 1) == 0 ? t.hazardA : t.hazardB);
            }
        }
    }

    private static BlockState bs(Block block) {
        return block.defaultBlockState();
    }

    private record Theme(
        BlockState floor,
        BlockState wall,
        BlockState accent,
        BlockState detail,
        BlockState hazardA,
        BlockState hazardB,
        EntityType<?> primary,
        EntityType<?> secondary,
        EntityType<?> tertiary,
        Block rewardWool
    ) {}

    public record Spec(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public BlockPos start(BlockPos origin) {
            return origin.offset(minX, minY, minZ);
        }

        public BlockPos end(BlockPos origin) {
            return origin.offset(maxX, maxY, maxZ);
        }

        public BlockPos paddedStart(BlockPos origin) {
            return origin.offset(minX - 2, minY - 2, minZ - 2);
        }

        public BlockPos paddedEnd(BlockPos origin) {
            return origin.offset(maxX + 2, maxY + 2, maxZ + 2);
        }

        public Vec3i size() {
            return new Vec3i(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
        }
    }

    private enum Dir {
        NORTH(0, -1, 0, -1),
        SOUTH(0, 1, 0, 1),
        WEST(-1, 0, -1, 0),
        EAST(1, 0, 1, 0);

        private final int cellDx;
        private final int cellDz;
        private final int dx;
        private final int dz;

        Dir(int cellDx, int cellDz, int dx, int dz) {
            this.cellDx = cellDx;
            this.cellDz = cellDz;
            this.dx = dx;
            this.dz = dz;
        }

        private Dir opposite() {
            return switch (this) {
                case NORTH -> SOUTH;
                case SOUTH -> NORTH;
                case WEST -> EAST;
                case EAST -> WEST;
            };
        }
    }

    private static final class MazeData {
        private final int cells;
        private final EnumSet<Dir>[][] connections;
        private final int startX;
        private final int startZ;

        @SuppressWarnings("unchecked")
        private MazeData(int cells) {
            this.cells = cells;
            this.connections = new EnumSet[cells][cells];
            for (int x = 0; x < cells; x++) {
                for (int z = 0; z < cells; z++) {
                    connections[x][z] = EnumSet.noneOf(Dir.class);
                }
            }
            this.startX = cells / 2;
            this.startZ = cells - 1;
        }

        private static MazeData generate(int cells, long seed) {
            MazeData maze = new MazeData(cells);
            boolean[][] visited = new boolean[cells][cells];
            ArrayDeque<int[]> stack = new ArrayDeque<>();
            Random random = new Random(seed);
            stack.push(new int[] {maze.startX, maze.startZ});
            visited[maze.startX][maze.startZ] = true;

            while (!stack.isEmpty()) {
                int[] current = stack.peek();
                int cx = current[0];
                int cz = current[1];

                Dir[] dirs = Dir.values().clone();
                shuffle(dirs, random);
                boolean moved = false;

                for (Dir dir : dirs) {
                    int nx = cx + dir.cellDx;
                    int nz = cz + dir.cellDz;
                    if (nx < 0 || nz < 0 || nx >= cells || nz >= cells || visited[nx][nz]) {
                        continue;
                    }
                    visited[nx][nz] = true;
                    maze.connections[cx][cz].add(dir);
                    maze.connections[nx][nz].add(dir.opposite());
                    stack.push(new int[] {nx, nz});
                    moved = true;
                    break;
                }

                if (!moved) {
                    stack.pop();
                }
            }

            return maze;
        }

        private static void shuffle(Dir[] dirs, Random random) {
            for (int i = dirs.length - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                Dir tmp = dirs[i];
                dirs[i] = dirs[j];
                dirs[j] = tmp;
            }
        }
    }

    private static final class Builder {
        private final ServerLevel level;
        private final BlockPos origin;
        private boolean changed = false;

        private Builder(ServerLevel level, BlockPos origin) {
            this.level = level;
            this.origin = origin;
        }

        private void set(int dx, int dy, int dz, BlockState state) {
            level.setBlock(origin.offset(dx, dy, dz), state, 3);
            changed = true;
        }

        private void clear(int x1, int y1, int z1, int x2, int y2, int z2) {
            fill(x1, y1, z1, x2, y2, z2, bs(Blocks.AIR));
        }

        private void fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
            for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
                for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                    for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                        set(x, y, z, state);
                    }
                }
            }
        }

        private void hollowBox(int x1, int y1, int z1, int x2, int y2, int z2, BlockState wall, BlockState inner) {
            fill(x1, y1, z1, x2, y2, z2, wall);
            if (x2 - x1 >= 2 && y2 - y1 >= 2 && z2 - z1 >= 2) {
                fill(x1 + 1, y1 + 1, z1 + 1, x2 - 1, y2 - 1, z2 - 1, inner);
            }
        }

        private void ring(int x1, int z1, int x2, int z2, int y, BlockState state) {
            fill(x1, y, z1, x2, y, z1, state);
            fill(x1, y, z2, x2, y, z2, state);
            fill(x1, y, z1, x1, y, z2, state);
            fill(x2, y, z1, x2, y, z2, state);
        }

        private void cylinderShell(int cx, int cz, int y1, int y2, int radius, BlockState state) {
            int outer = radius * radius;
            int inner = (radius - 1) * (radius - 1);
            for (int x = cx - radius; x <= cx + radius; x++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    int d = (x - cx) * (x - cx) + (z - cz) * (z - cz);
                    if (d <= outer && d > inner) {
                        fill(x, y1, z, x, y2, z, state);
                    }
                }
            }
        }

        private void opening(int x1, int y1, int z1, int x2, int y2, int z2) {
            fill(x1, y1, z1, x2, y2, z2, bs(Blocks.AIR));
        }

        private void decoyChest(int x, int y, int z, boolean trapped) {
            set(x, y, z, trapped ? bs(Blocks.TRAPPED_CHEST) : bs(Blocks.CHEST));
        }

        private void rewardChest(int x, int y, int z, Block rewardWool) {
            BlockPos pos = origin.offset(x, y, z);
            level.setBlock(pos, bs(Blocks.CHEST), 3);
            if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
                chest.clearContent();
                chest.setItem(13, new ItemStack(rewardWool, 16));
                chest.setChanged();
            }
            changed = true;
        }

        private void spawner(int x, int y, int z, EntityType<?> type) {
            BlockPos pos = origin.offset(x, y, z);
            level.setBlock(pos, bs(Blocks.SPAWNER), 3);
            if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
                spawner.setEntityId(type, level.random);
                spawner.setChanged();
            }
            changed = true;
        }
    }
}
