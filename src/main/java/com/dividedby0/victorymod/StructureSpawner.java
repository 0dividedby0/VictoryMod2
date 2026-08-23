package com.dividedby0.victorymod;

import com.dividedby0.victorymod.config.ConfigManager;
import com.dividedby0.victorymod.config.JSON5ConfigManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StructureSpawner {
    private static final int BUFFERED_LOCATION_ATTEMPTS = 8;
    private static final int UNBUFFERED_LOCATION_ATTEMPTS = 1;
    private static final int MONUMENT_LOCATION_ATTEMPTS = 16;
    private static final int MONUMENT_SEARCH_RADIUS = 96;
    private static final int TERRAIN_SCAN_DEPTH = 24;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));

    private static final String[] COLORS = {
        "white","orange","magenta","lightblue","yellow","lime","pink","gray",
        "lightgray","cyan","purple","blue","brown","green","red","black"
    };

    private static final List<BlockPos> placedStructures = new ArrayList<>();

    public static void spawnAll(ServerLevel level) {
        SpawnJob job = new SpawnJob(level.getSharedSpawnPos());
        job.start(level);
        while (job.spawnNext(level)) {
            // Keep the legacy immediate path for tests or future commands.
        }
        job.finish(level);
    }

    static final class SpawnJob {
        private final BlockPos spawn;
        private final Random rand = new Random();

        private JSON5ConfigManager configManager;
        private JsonObject defaultRules;
        private JsonObject structureOverrides;
        private int minRadius;
        private int maxRadius;
        private int bufferDistance;
        private int nextStructureIndex = -1;
        private int dungeonsPlaced;
        private int dungeonsFailed;
        private boolean monumentPlaced;
        private long startedAtNanos;

        SpawnJob(BlockPos spawn) {
            this.spawn = spawn;
        }

        void start(ServerLevel level) {
            placedStructures.clear();
            configManager = ConfigManager.getInstance();
            minRadius = configManager.getInt("minDungeonRadius", 40);
            maxRadius = configManager.getInt("maxDungeonRadius", 750);
            bufferDistance = configManager.getInt("structureBufferDistance", 30);
            defaultRules = configManager.getJsonObject("defaultRules", new JsonObject());
            structureOverrides = configManager.getJsonObject("structures", new JsonObject());
            startedAtNanos = System.nanoTime();

            System.out.println("[VictoryMod] starting structure placement at world creation");
        }

        boolean spawnNext(ServerLevel level) {
            if (nextStructureIndex == -1) {
                spawnMonument(level);
                nextStructureIndex++;
                return true;
            }

            if (nextStructureIndex >= COLORS.length) {
                return false;
            }

            spawnDungeon(level, COLORS[nextStructureIndex]);
            nextStructureIndex++;
            return true;
        }

        private void spawnMonument(ServerLevel level) {
            long started = System.nanoTime();
            StructureRules monumentRules = getRulesForStructure("victory_monument", defaultRules, structureOverrides);
            BlockPos victoryPos = createCandidatePos(level, rand, spawn.getX(), spawn.getZ(), monumentRules);
            if (victoryPos == null) {
                victoryPos = findDungeonLocation(level, rand, spawn.getX(), spawn.getZ(), 0, MONUMENT_SEARCH_RADIUS, 0, monumentRules, MONUMENT_LOCATION_ATTEMPTS);
            }

            if (victoryPos == null) {
                System.out.println("[VictoryMod] victory monument using unconditional spawn fallback");
                victoryPos = createUnconditionalCandidatePos(level, rand, spawn.getX(), spawn.getZ(), monumentRules);
            }

            if (placeStructure(level, "victory_monument", victoryPos)) {
                placedStructures.add(victoryPos);
                monumentPlaced = true;
            } else {
                System.err.println("[VictoryMod] victory monument placement failed; generation will remain incomplete");
                return;
            }

            String msg = String.format("§6Victory Monument spawned at X: %d, Y: %d, Z: %d", victoryPos.getX(), victoryPos.getY(), victoryPos.getZ());
            level.getServer().getPlayerList().broadcastSystemMessage(
                net.minecraft.network.chat.Component.literal(msg), false
            );
            logTiming("victory_monument", started);
        }

        private void spawnDungeon(ServerLevel level, String color) {
            String structureName = "dungeon_" + color;
            long started = System.nanoTime();
            StructureRules dungeonRules = getRulesForStructure(structureName, defaultRules, structureOverrides);
            BlockPos dungeonPos = spawnDungeonWithFallbacks(
                level,
                rand,
                spawn.getX(),
                spawn.getZ(),
                structureName,
                minRadius,
                maxRadius,
                bufferDistance,
                dungeonRules
            );
            if (dungeonPos != null) {
                placedStructures.add(dungeonPos);
                dungeonsPlaced++;
            } else {
                dungeonsFailed++;
            }
            logTiming(structureName, started);
        }

        void finish(ServerLevel level) {
            VictoryModSavedData data = level.getDataStorage().computeIfAbsent(
                VictoryModSavedData::load,
                VictoryModSavedData::new,
                "victorymod_data"
            );
            boolean complete = monumentPlaced && dungeonsPlaced == COLORS.length && dungeonsFailed == 0;
            if (complete) {
                data.setStructuresSpawned(true);
                data.setDirty();
            }

            long elapsedMs = (System.nanoTime() - startedAtNanos) / 1_000_000L;
            if (complete) {
                System.out.println("[VictoryMod] all " + COLORS.length + " dungeons successfully spawned");
            } else {
                System.err.println("[VictoryMod] placement incomplete; world will not be marked generated. Monument="
                    + monumentPlaced + ", dungeons=" + dungeonsPlaced + "/" + COLORS.length);
            }
            System.out.println("[VictoryMod] finished structure placement in " + elapsedMs + " ms");
        }
    }

    private static void logTiming(String structureName, long startedNanos) {
        long elapsedMs = (System.nanoTime() - startedNanos) / 1_000_000L;
        System.out.println("[VictoryMod] " + structureName + " placement step took " + elapsedMs + " ms");
    }

    /**
     * Spawn a dungeon with progressive fallback strategies to ensure placement.
     * Tries to keep the structure buffer as long as possible. The configured
     * max radius is a hard cap; only buffer and inner-radius constraints relax.
     */
    private static BlockPos spawnDungeonWithFallbacks(
        ServerLevel level,
        Random rand,
        int spawnX,
        int spawnZ,
        String structureName,
        int minRadius,
        int maxRadius,
        int bufferDistance,
        StructureRules rules
    ) {
        // Strategy 1: Configured radius and structure buffer. Terrain Y is resolved at the chosen column.
        BlockPos pos = findDungeonLocation(level, rand, spawnX, spawnZ, minRadius, maxRadius, bufferDistance, rules, BUFFERED_LOCATION_ATTEMPTS);
        if (pos != null && placeStructure(level, structureName, pos)) {
            return pos;
        }

        // Strategy 2: Keep the configured radius but relax the structure buffer.
        System.out.println("[VictoryMod] " + structureName + " relaxing buffer constraint");
        pos = findDungeonLocation(level, rand, spawnX, spawnZ, minRadius, maxRadius, 0, rules, UNBUFFERED_LOCATION_ATTEMPTS);
        if (pos != null && placeStructure(level, structureName, pos)) {
            return pos;
        }

        // Strategy 3: Keep the max radius hard cap, but allow closer-to-spawn placement.
        System.out.println("[VictoryMod] " + structureName + " relaxing inner radius");
        pos = findDungeonLocation(level, rand, spawnX, spawnZ, 0, maxRadius, bufferDistance, rules, BUFFERED_LOCATION_ATTEMPTS);
        if (pos != null && placeStructure(level, structureName, pos)) {
            return pos;
        }

        // Strategy 4: Last normal resort: anywhere within max radius, no buffer.
        System.out.println("[VictoryMod] " + structureName + " relaxing buffer and inner radius constraints");
        pos = findDungeonLocation(level, rand, spawnX, spawnZ, 0, maxRadius, 0, rules, UNBUFFERED_LOCATION_ATTEMPTS);
        if (pos != null && placeStructure(level, structureName, pos)) {
            return pos;
        }

        // Strategy 5: Deterministic guaranteed fallback. It retains the configured
        // radius and spreads all 16 dungeons around a ring, but deliberately ignores
        // biome and terrain exclusions so a valid template always gets a position.
        System.out.println("[VictoryMod] " + structureName + " using deterministic guaranteed fallback");
        int structureIndex = colorIndex(structureName);
        double angle = structureIndex * (Math.PI * 2.0D / COLORS.length);
        int radius = Math.max(0, Math.min(maxRadius, Math.max(minRadius, maxRadius - Math.max(0, bufferDistance))));
        int forceX = spawnX + (int) Math.round(radius * Math.cos(angle));
        int forceZ = spawnZ + (int) Math.round(radius * Math.sin(angle));
        BlockPos forcePos = createUnconditionalCandidatePos(level, rand, forceX, forceZ, rules);
        return placeStructure(level, structureName, forcePos) ? forcePos : null;
    }

    private static int colorIndex(String structureName) {
        String color = structureName.startsWith("dungeon_") ? structureName.substring("dungeon_".length()) : structureName;
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i].equals(color)) {
                return i;
            }
        }
        return Math.floorMod(structureName.hashCode(), COLORS.length);
    }

    private static BlockPos createUnconditionalCandidatePos(ServerLevel level, Random rand, int x, int z, StructureRules rules) {
        int groundY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        if (groundY <= level.getMinBuildHeight()) {
            groundY = level.getSeaLevel();
        }
        return new BlockPos(x, resolveY(level, rand, groundY, rules.heightRules), z);
    }

    /**
     * Find a dungeon location with configurable constraints.
     */
    private static BlockPos findDungeonLocation(
        ServerLevel level,
        Random rand,
        int spawnX,
        int spawnZ,
        int minRadius,
        int maxRadius,
        int bufferDistance,
        StructureRules rules,
        int maxAttempts
    ) {
        int attempts = 0;
        int searchMaxRadius = Math.max(0, maxRadius);
        int searchMinRadius = Math.max(0, Math.min(minRadius, searchMaxRadius));
        double phase = rand.nextDouble() * Math.PI * 2.0D;
        double minRadiusSquared = (double) searchMinRadius * searchMinRadius;
        double radiusArea = (double) searchMaxRadius * searchMaxRadius - minRadiusSquared;

        while (attempts < maxAttempts) {
            // A golden-angle sequence covers the annulus evenly and never retries the
            // same random neighborhood. sqrt keeps candidate density uniform by area.
            double sample = (attempts + 0.5D) / Math.max(1, maxAttempts);
            double angle = phase + attempts * GOLDEN_ANGLE;
            int radius = (int) Math.round(Math.sqrt(minRadiusSquared + radiusArea * sample));
            int x = spawnX + (int) Math.round(radius * Math.cos(angle));
            int z = spawnZ + (int) Math.round(radius * Math.sin(angle));

            if (!biomeMatches(level, x, z, rules.biomeRules)) {
                attempts++;
                continue;
            }

            boolean bufferOk = bufferDistance == 0 || !overlapsWithExisting(x, z, bufferDistance);
            if (!bufferOk) {
                attempts++;
                continue;
            }

            BlockPos candidatePos = createCandidatePos(level, rand, x, z, rules);
            if (candidatePos != null) {
                return candidatePos;
            }

            attempts++;
        }

        return null;
    }

    private static int getGroundY(ServerLevel level, int x, int z) {
        // Use WORLD_SURFACE_WG in world spawn phase to avoid the 0-y bug from premature data.
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        int y = findUsableGroundY(level, x, z, surfaceY);

        if (surfaceY <= level.getMinBuildHeight() + 1) {
            // fallback scan in case heightmap is not populated yet
            for (int scanY = level.getMaxBuildHeight() - 1; scanY > level.getMinBuildHeight(); scanY--) {
                if (!level.isEmptyBlock(new BlockPos(x, scanY, z))) {
                    y = findUsableGroundY(level, x, z, scanY + 1);
                    break;
                }
            }
        }

        if (y == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        y = Math.max(y, level.getMinBuildHeight() + 1);
        y = Math.min(y, level.getMaxBuildHeight() - 1);
        return y;
    }

    private static int findUsableGroundY(ServerLevel level, int x, int z, int surfaceY) {
        int minY = level.getMinBuildHeight();
        int topY = Math.min(surfaceY - 1, level.getMaxBuildHeight() - 1);
        int bottomY = Math.max(minY, topY - TERRAIN_SCAN_DEPTH);

        for (int y = topY; y >= bottomY; y--) {
            BlockPos groundPos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(groundPos);

            if (state.isAir()) {
                continue;
            }

            if (!state.getFluidState().isEmpty()) {
                return Integer.MIN_VALUE;
            }

            Block block = state.getBlock();
            if (isTreeLike(block) || isVegetationLike(block)) {
                continue;
            }

            return y + 1;
        }

        return Integer.MIN_VALUE;
    }

    /**
     * Checks if a position overlaps with any previously placed structure.
     * Considers the buffer distance around each placed structure.
     */
    private static boolean overlapsWithExisting(int x, int z, int bufferDistance) {
        for (BlockPos placedPos : placedStructures) {
            // Calculate horizontal distance (ignoring Y)
            int dx = x - placedPos.getX();
            int dz = z - placedPos.getZ();
            int distanceSquared = dx * dx + dz * dz;
            int bufferSquared = bufferDistance * bufferDistance;

            if (distanceSquared < bufferSquared) {
                return true;
            }
        }
        return false;
    }

    private static boolean placeStructure(ServerLevel level, String name, BlockPos pos) {
        ResourceLocation templateId = ResourceLocation.tryParse("victorymod:" + name);
        if (templateId == null) {
            System.err.println("[VictoryMod] invalid structure id: " + name);
            return false;
        }

        StructureTemplate template = level.getStructureManager().getOrCreate(templateId);

        if (template == null) {
            System.err.println("[VictoryMod] structure not found: " + name + " (id=" + templateId + ")");
            return false;
        }

        boolean placed = template.placeInWorld(level, pos, pos, new StructurePlaceSettings(), level.random, 3);
        if (!placed) {
            if (shouldRevealLocation(name)) {
                System.err.println("[VictoryMod] failed to place structure " + name + " at " + pos);
            } else {
                System.err.println("[VictoryMod] failed to place structure " + name);
            }
        } else {
            if (shouldRevealLocation(name)) {
                System.out.println("[VictoryMod] placed " + name + " at " + pos);
            } else {
                System.out.println("[VictoryMod] placed " + name);
            }
        }
        return placed;
    }

    private static boolean shouldRevealLocation(String structureName) {
        return "victory_monument".equals(structureName);
    }

    private static StructureRules getRulesForStructure(String structureName, JsonObject defaultRules, JsonObject structureOverrides) {
        JsonObject mergedRules = defaultRules.deepCopy();
        if (structureOverrides.has(structureName) && structureOverrides.get(structureName).isJsonObject()) {
            mergeInto(mergedRules, structureOverrides.getAsJsonObject(structureName));
        }
        return StructureRules.fromJson(mergedRules);
    }

    private static void mergeInto(JsonObject base, JsonObject override) {
        for (String key : override.keySet()) {
            JsonElement overrideValue = override.get(key);
            if (overrideValue.isJsonObject() && base.has(key) && base.get(key).isJsonObject()) {
                mergeInto(base.getAsJsonObject(key), overrideValue.getAsJsonObject());
            } else {
                base.add(key, overrideValue.deepCopy());
            }
        }
    }

    private static BlockPos createCandidatePos(ServerLevel level, Random rand, int x, int z, StructureRules rules) {
        int groundY = getGroundY(level, x, z);
        if (groundY == Integer.MIN_VALUE) {
            return null;
        }

        int y = resolveY(level, rand, groundY, rules.heightRules);
        if (y < level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) {
            return null;
        }
        return new BlockPos(x, y, z);
    }

    private static int resolveY(ServerLevel level, Random rand, int groundY, HeightRules rules) {
        return switch (rules.mode) {
            case "fixed" -> MthUtil.clampToBuildHeight(level, rules.fixedY);
            case "underground" -> MthUtil.randomBetween(rand, level, rules.minY, rules.maxY);
            case "air" -> MthUtil.randomBetween(rand, level, rules.minY, rules.maxY);
            case "surface" -> MthUtil.clampToBuildHeight(level, groundY + rules.surfaceOffset);
            default -> MthUtil.clampToBuildHeight(level, groundY);
        };
    }

    private static boolean biomeMatches(ServerLevel level, int x, int z, BiomeRules rules) {
        if ("any".equals(rules.mode) || rules.values.isEmpty()) {
            return true;
        }

        Holder<Biome> biomeHolder = level.getBiome(new BlockPos(x, level.getSeaLevel(), z));
        boolean matched = false;
        for (String value : rules.values) {
            if (value.startsWith("#")) {
                ResourceLocation tagId = ResourceLocation.tryParse(value.substring(1));
                if (tagId != null && biomeHolder.is(TagKey.create(Registries.BIOME, tagId))) {
                    matched = true;
                    break;
                }
            } else {
                ResourceLocation biomeId = ResourceLocation.tryParse(value);
                if (biomeId != null && biomeHolder.unwrapKey().map(key -> key.location().equals(biomeId)).orElse(false)) {
                    matched = true;
                    break;
                }
            }
        }

        return "deny".equals(rules.mode) ? !matched : matched;
    }

    private static boolean isTreeLike(Block block) {
        String blockName = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return block instanceof LeavesBlock
            || blockName.endsWith("_log")
            || blockName.endsWith("_wood")
            || blockName.endsWith("_stem")
            || blockName.endsWith("_hyphae")
            || blockName.contains("leaves");
    }

    private static boolean isVegetationLike(Block block) {
        String blockName = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return blockName.equals("grass")
            || blockName.equals("tall_grass")
            || blockName.equals("fern")
            || blockName.equals("large_fern")
            || blockName.contains("seagrass")
            || blockName.contains("flower")
            || blockName.contains("mushroom")
            || blockName.contains("vine")
            || blockName.contains("kelp");
    }

    private static final class StructureRules {
        private final BiomeRules biomeRules;
        private final HeightRules heightRules;

        private StructureRules(BiomeRules biomeRules, HeightRules heightRules) {
            this.biomeRules = biomeRules;
            this.heightRules = heightRules;
        }

        private static StructureRules fromJson(JsonObject root) {
            return new StructureRules(
                BiomeRules.fromJson(root.getAsJsonObject("biomes")),
                HeightRules.fromJson(root.getAsJsonObject("height"))
            );
        }
    }

    private static final class BiomeRules {
        private final String mode;
        private final List<String> values;

        private BiomeRules(String mode, List<String> values) {
            this.mode = mode;
            this.values = values;
        }

        private static BiomeRules fromJson(JsonObject root) {
            String mode = getString(root, "mode", "any");
            List<String> values = new ArrayList<>();
            if (root != null && root.has("values") && root.get("values").isJsonArray()) {
                for (JsonElement element : root.getAsJsonArray("values")) {
                    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                        values.add(element.getAsString());
                    }
                }
            }
            return new BiomeRules(mode, values);
        }
    }

    private static final class HeightRules {
        private final String mode;
        private final int minY;
        private final int maxY;
        private final int fixedY;
        private final int surfaceOffset;

        private HeightRules(String mode, int minY, int maxY, int fixedY, int surfaceOffset) {
            this.mode = mode;
            this.minY = minY;
            this.maxY = maxY;
            this.fixedY = fixedY;
            this.surfaceOffset = surfaceOffset;
        }

        private static HeightRules fromJson(JsonObject root) {
            return new HeightRules(
                getString(root, "mode", "surface"),
                getInt(root, "minY", 40),
                getInt(root, "maxY", 120),
                getInt(root, "y", 64),
                getInt(root, "surfaceOffset", 0)
            );
        }
    }

    private static String getString(JsonObject root, String key, String fallback) {
        if (root != null && root.has(key) && root.get(key).isJsonPrimitive()) {
            return root.get(key).getAsString();
        }
        return fallback;
    }

    private static int getInt(JsonObject root, String key, int fallback) {
        if (root != null && root.has(key) && root.get(key).isJsonPrimitive()) {
            return root.get(key).getAsInt();
        }
        return fallback;
    }

    private static final class MthUtil {
        private static int clampToBuildHeight(ServerLevel level, int y) {
            return Math.max(level.getMinBuildHeight() + 1, Math.min(y, level.getMaxBuildHeight() - 1));
        }

        private static int randomBetween(Random rand, ServerLevel level, int minY, int maxY) {
            int min = clampToBuildHeight(level, Math.min(minY, maxY));
            int max = clampToBuildHeight(level, Math.max(minY, maxY));
            if (max <= min) {
                return min;
            }
            return min + rand.nextInt(max - min + 1);
        }
    }
}
