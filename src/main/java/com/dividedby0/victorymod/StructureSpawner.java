package com.dividedby0.victorymod;

import com.dividedby0.victorymod.config.ConfigManager;
import com.dividedby0.victorymod.config.JSON5ConfigManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StructureSpawner {

    private static final String[] COLORS = {
        "white","orange","magenta","lightblue","yellow","lime","pink","gray",
        "lightgray","cyan","purple","blue","brown","green","red","black"
    };

    private static final List<BlockPos> placedStructures = new ArrayList<>();

    public static List<String> getStructureNames() {
        List<String> names = new ArrayList<>();
        names.add("victory_monument");
        for (String color : COLORS) {
            names.add("dungeon_" + color);
        }
        return names;
    }

    public static void spawnAll(ServerLevel level) {
        placedStructures.clear();

        BlockPos spawn = level.getSharedSpawnPos();

        int spawnX = spawn.getX();
        int spawnZ = spawn.getZ();
        Random rand = new Random();
        JSON5ConfigManager configManager = ConfigManager.getInstance();
        int minRadius = configManager.getInt("minDungeonRadius", 40);
        int maxRadius = configManager.getInt("maxDungeonRadius", 750);
        int bufferDistance = configManager.getInt("structureBufferDistance", 30);
        JsonObject defaultRules = configManager.getJsonObject("defaultRules", new JsonObject());
        JsonObject structureOverrides = configManager.getJsonObject("structures", new JsonObject());
        StructureRules monumentRules = getRulesForStructure("victory_monument", defaultRules, structureOverrides);

        BlockPos victoryPos = createCandidatePos(level, rand, spawnX, spawnZ, monumentRules);
        if (victoryPos == null) {
            victoryPos = new BlockPos(spawnX, getGroundY(level, spawnX, spawnZ), spawnZ);
        }

        // Try to find a valid location for the victory monument
        BlockPos validMonumentPos = victoryPos;
        if (!isValidSpawnLocation(level, victoryPos, monumentRules)) {
            // If exact spawn is invalid, search nearby for a valid location
            validMonumentPos = findNearbyValidLocation(level, rand, spawnX, spawnZ, 100, monumentRules);
            if (validMonumentPos == null) {
                System.err.println("[VictoryMod] Could not find valid spawn location for victory monument!");
                validMonumentPos = victoryPos;
            }
        }

        placeStructure(level, "victory_monument", validMonumentPos);
        placedStructures.add(validMonumentPos);

        // Print coordinates in chat to all players
        String msg = String.format("§6Victory Monument spawned at X: %d, Y: %d, Z: %d", validMonumentPos.getX(), validMonumentPos.getY(), validMonumentPos.getZ());
        level.getServer().getPlayerList().broadcastSystemMessage(
            net.minecraft.network.chat.Component.literal(msg), false
        );

        for (String color : COLORS) {
            String structureName = "dungeon_" + color;
            StructureRules dungeonRules = getRulesForStructure(structureName, defaultRules, structureOverrides);
            BlockPos dungeonPos = spawnDungeonWithFallbacks(
                level,
                rand,
                spawnX,
                spawnZ,
                structureName,
                minRadius,
                maxRadius,
                bufferDistance,
                dungeonRules
            );
            if (dungeonPos != null) {
                placedStructures.add(dungeonPos);
            }
        }
    }

    /**
     * Spawn a dungeon with progressive fallback strategies to ensure placement.
     * Tries in order while always respecting the configured spawn radius:
     * full rules + buffer, full rules, relaxed terrain checks, relaxed biome checks,
     * relaxed height rules, and finally a forced surface placement within radius.
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
        // Strategy 1: Valid location + respect buffer distance
        BlockPos pos = findDungeonLocation(level, rand, spawnX, spawnZ, minRadius, maxRadius, bufferDistance, true, true, true, rules);
        if (pos != null) {
            placeStructure(level, structureName, pos);
            return pos;
        }

        // Strategy 2: Valid location only (ignore buffer)
        System.out.println("[VictoryMod] " + structureName + " relaxing buffer constraint");
        pos = findDungeonLocation(level, rand, spawnX, spawnZ, minRadius, maxRadius, 0, true, true, true, rules);
        if (pos != null) {
            placeStructure(level, structureName, pos);
            return pos;
        }

        // Strategy 3: Any location + respect buffer (ignore valid location check)
        System.out.println("[VictoryMod] " + structureName + " relaxing terrain validation");
        pos = findDungeonLocation(level, rand, spawnX, spawnZ, minRadius, maxRadius, bufferDistance, false, true, true, rules);
        if (pos != null) {
            placeStructure(level, structureName, pos);
            return pos;
        }

        // Strategy 4: Any location within radius (ignore valid location and buffer)
        System.out.println("[VictoryMod] " + structureName + " relaxing proximity constraints");
        pos = findDungeonLocation(level, rand, spawnX, spawnZ, minRadius, maxRadius, 0, false, true, true, rules);
        if (pos != null) {
            placeStructure(level, structureName, pos);
            return pos;
        }

        // Strategy 5: Ignore biome requirements if needed, still within radius
        System.out.println("[VictoryMod] " + structureName + " relaxing biome constraints");
        pos = findDungeonLocation(level, rand, spawnX, spawnZ, minRadius, maxRadius, 0, false, false, true, rules);
        if (pos != null) {
            placeStructure(level, structureName, pos);
            return pos;
        }

        // Strategy 6: Ignore configured height rules and place on the surface within radius
        System.out.println("[VictoryMod] " + structureName + " relaxing height constraints");
        pos = findDungeonLocation(level, rand, spawnX, spawnZ, minRadius, maxRadius, 0, false, false, false, rules);
        if (pos != null) {
            placeStructure(level, structureName, pos);
            return pos;
        }

        // Strategy 7: Force placement within the configured radius ring and fall back to surface height
        System.out.println("[VictoryMod] " + structureName + " forcing placement within configured radius");
        BlockPos forcePos = createForcedRadiusPos(level, rand, spawnX, spawnZ, minRadius, maxRadius, rules);
        placeStructure(level, structureName, forcePos);
        return forcePos;
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
        boolean requireValidLocation,
        boolean requireBiomeMatch,
        boolean useConfiguredHeight,
        StructureRules rules
    ) {
        int attempts = 0;
        int maxAttempts = 100;

        while (attempts < maxAttempts) {
            BlockPos horizontalPos = sampleWithinRadius(rand, spawnX, spawnZ, minRadius, maxRadius);
            int x = horizontalPos.getX();
            int z = horizontalPos.getZ();

            if (requireBiomeMatch && !biomeMatches(level, x, z, rules.biomeRules)) {
                attempts++;
                continue;
            }

            BlockPos candidatePos = useConfiguredHeight
                ? createCandidatePos(level, rand, x, z, rules)
                : createSurfaceFallbackPos(level, x, z);
            if (candidatePos == null) {
                attempts++;
                continue;
            }

            boolean locationValid = !requireValidLocation || isValidSpawnLocation(level, candidatePos, rules);
            boolean bufferOk = bufferDistance == 0 || !overlapsWithExisting(candidatePos, bufferDistance);

            if (locationValid && bufferOk) {
                return candidatePos;
            }

            attempts++;
        }

        return null;
    }

    private static BlockPos sampleWithinRadius(Random rand, int spawnX, int spawnZ, int minRadius, int maxRadius) {
        double angle = rand.nextDouble() * Math.PI * 2.0;
        int radius = minRadius + rand.nextInt(Math.max(1, maxRadius - minRadius + 1));
        int x = spawnX + (int) Math.round(radius * Math.cos(angle));
        int z = spawnZ + (int) Math.round(radius * Math.sin(angle));
        return new BlockPos(x, 0, z);
    }

    private static BlockPos createSurfaceFallbackPos(ServerLevel level, int x, int z) {
        return new BlockPos(x, getGroundY(level, x, z), z);
    }

    private static BlockPos createForcedRadiusPos(
        ServerLevel level,
        Random rand,
        int spawnX,
        int spawnZ,
        int minRadius,
        int maxRadius,
        StructureRules rules
    ) {
        BlockPos horizontalPos = sampleWithinRadius(rand, spawnX, spawnZ, minRadius, maxRadius);
        return createSurfaceFallbackPos(level, horizontalPos.getX(), horizontalPos.getZ());
    }

    private static int getGroundY(ServerLevel level, int x, int z) {
        // Use WORLD_SURFACE_WG in world spawn phase to avoid the 0-y bug from premature data.
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

        if (y <= level.getMinBuildHeight() + 1) {
            // fallback scan in case heightmap is not populated yet
            for (int scanY = level.getMaxBuildHeight() - 1; scanY > level.getMinBuildHeight(); scanY--) {
                if (!level.isEmptyBlock(new BlockPos(x, scanY, z))) {
                    y = scanY + 1;
                    break;
                }
            }
        }

        y = Math.max(y, level.getMinBuildHeight() + 1);
        y = Math.min(y, level.getMaxBuildHeight() - 1);
        return y;
    }

    /**
     * Finds a nearby valid spawn location within a search radius.
     * Searches in expanding circles around the center point.
     */
    private static BlockPos findNearbyValidLocation(
        ServerLevel level,
        Random rand,
        int centerX,
        int centerZ,
        int searchRadius,
        StructureRules rules
    ) {
        // Try random locations in expanding circles
        for (int radius = 10; radius <= searchRadius; radius += 10) {
            for (int attempt = 0; attempt < 20; attempt++) {
                double angle = rand.nextDouble() * Math.PI * 2.0;
                int x = centerX + (int) Math.round(radius * Math.cos(angle));
                int z = centerZ + (int) Math.round(radius * Math.sin(angle));

                if (!biomeMatches(level, x, z, rules.biomeRules)) {
                    continue;
                }

                BlockPos candidatePos = createCandidatePos(level, rand, x, z, rules);
                if (candidatePos != null && isValidSpawnLocation(level, candidatePos, rules)) {
                    return candidatePos;
                }
            }
        }

        return null;
    }

    /**
     * Checks if a location is valid for structure spawning.
     * Valid locations must be on solid land (not water or air-like blocks).
     */
    private static boolean isValidSpawnLocation(ServerLevel level, BlockPos pos, StructureRules rules) {
        if (!biomeMatches(level, pos.getX(), pos.getZ(), rules.biomeRules)) {
            return false;
        }

        BlockPos belowPos = pos.below();
        var blockBelow = level.getBlockState(belowPos).getBlock();
        var blockAt = level.getBlockState(pos).getBlock();

        if (!rules.placementRules.allowWater) {
            if (blockBelow instanceof LiquidBlock || blockAt instanceof LiquidBlock) {
                return false;
            }
        }

        if (!rules.placementRules.allowTrees && isTreeLike(blockBelow)) {
            return false;
        }

        if (rules.placementRules.requireSolidGround) {
            if (blockBelow instanceof LiquidBlock) {
                return false;
            }
            if (blockBelow instanceof LeavesBlock) {
                return false;
            }
            if (blockBelow == Blocks.AIR || blockBelow == Blocks.VOID_AIR || blockBelow == Blocks.CAVE_AIR) {
                return false;
            }
            if (isVegetationLike(blockBelow)) {
                return false;
            }
        }

        if ("underground".equals(rules.heightRules.mode) && !rules.placementRules.allowWater) {
            if (level.getFluidState(pos).isEmpty() && level.getFluidState(pos.above()).isEmpty()) {
                return true;
            }
            return false;
        }

        return true;
    }

    /**
     * Checks if a position overlaps with any previously placed structure.
     * Considers the buffer distance around each placed structure.
     */
    private static boolean overlapsWithExisting(BlockPos pos, int bufferDistance) {
        for (BlockPos placedPos : placedStructures) {
            // Calculate horizontal distance (ignoring Y)
            int dx = pos.getX() - placedPos.getX();
            int dz = pos.getZ() - placedPos.getZ();
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
            
            if (horizontalDistance < bufferDistance) {
                return true;
            }
        }
        return false;
    }

    public static boolean placeStructureByName(ServerLevel level, String name, BlockPos pos) {
        return placeStructure(level, name, pos);
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
            System.err.println("[VictoryMod] failed to place structure " + name + " at " + pos);
        } else {
            System.out.println("[VictoryMod] placed " + name + " at " + pos);
        }
        return placed;
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
        int y = resolveY(level, rand, x, z, rules.heightRules);
        if (y < level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) {
            return null;
        }
        return new BlockPos(x, y, z);
    }

    private static int resolveY(ServerLevel level, Random rand, int x, int z, HeightRules rules) {
        return switch (rules.mode) {
            case "fixed" -> MthUtil.clampToBuildHeight(level, rules.fixedY);
            case "underground" -> MthUtil.randomBetween(rand, level, rules.minY, rules.maxY);
            case "air" -> MthUtil.randomBetween(rand, level, rules.minY, rules.maxY);
            case "surface" -> MthUtil.clampToBuildHeight(level, getGroundY(level, x, z) + rules.surfaceOffset);
            default -> MthUtil.clampToBuildHeight(level, getGroundY(level, x, z));
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

    private static boolean isTreeLike(net.minecraft.world.level.block.Block block) {
        String blockName = block.getName().getString().toLowerCase();
        return block instanceof LeavesBlock || blockName.contains("log") || blockName.contains("leaves");
    }

    private static boolean isVegetationLike(net.minecraft.world.level.block.Block block) {
        String blockName = block.getName().getString().toLowerCase();
        return blockName.contains("grass")
            || blockName.contains("seagrass")
            || blockName.contains("flower")
            || blockName.contains("mushroom")
            || blockName.contains("vine")
            || blockName.contains("kelp");
    }

    private static final class StructureRules {
        private final BiomeRules biomeRules;
        private final HeightRules heightRules;
        private final PlacementRules placementRules;

        private StructureRules(BiomeRules biomeRules, HeightRules heightRules, PlacementRules placementRules) {
            this.biomeRules = biomeRules;
            this.heightRules = heightRules;
            this.placementRules = placementRules;
        }

        private static StructureRules fromJson(JsonObject root) {
            return new StructureRules(
                BiomeRules.fromJson(root.getAsJsonObject("biomes")),
                HeightRules.fromJson(root.getAsJsonObject("height")),
                PlacementRules.fromJson(root.getAsJsonObject("placement"))
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

    private static final class PlacementRules {
        private final boolean requireSolidGround;
        private final boolean allowWater;
        private final boolean allowTrees;

        private PlacementRules(boolean requireSolidGround, boolean allowWater, boolean allowTrees) {
            this.requireSolidGround = requireSolidGround;
            this.allowWater = allowWater;
            this.allowTrees = allowTrees;
        }

        private static PlacementRules fromJson(JsonObject root) {
            return new PlacementRules(
                getBoolean(root, "requireSolidGround", true),
                getBoolean(root, "allowWater", false),
                getBoolean(root, "allowTrees", false)
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

    private static boolean getBoolean(JsonObject root, String key, boolean fallback) {
        if (root != null && root.has(key) && root.get(key).isJsonPrimitive()) {
            return root.get(key).getAsBoolean();
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
