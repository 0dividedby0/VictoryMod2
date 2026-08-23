# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog and this project follows semantic versioning.

## [1.0.1] - 2026-08-23

### Changed
- Spread structure generation across server ticks, placing at most one monument or dungeon per tick.
- Replaced up to hundreds of random placement attempts per fallback stage with bounded golden-angle candidate searches.
- Simplified surface validation to scan downward past foliage while rejecting liquid columns during normal placement.
- Removed obsolete placement-rule config fields; biome and height rules remain supported.

### Fixed
- Added a deterministic final fallback inside `maxDungeonRadius` so every color dungeon receives a placement position even when normal biome, terrain, radius, or buffer constraints conflict.
- Prevented incomplete monument/dungeon batches from being marked generated; failed template placement is retried on a later world load.
- Prevented world generation from stalling under restrictive buffer and placement constraints.

## [1.0.0] - 2026-06-29

### Added
- Per-structure spawn rules in `victorymod.json5` for biome filtering and height selection.
- Support for `surface`, `underground`, `air`, and `fixed` height modes on both the victory monument and individual dungeons.
- Biome allow/deny lists using biome ids or biome tags for structure-specific placement control.

### Changed
- Updated config initialization to use Forge's config directory path so the mod behaves correctly in shared dev-pack runs and other nonstandard launch environments.
- Removed beta version tags for the stable publish build.
- Increased the default dungeon minimum radius and structure buffer distance to reduce overlaps around spawn.
- Improved dungeon placement fallback order so structure spacing relaxes only after bounded usable-terrain searches.
- Kept the configured maximum dungeon radius as a hard cap for normal and forced placements.
- Reduced dungeon coordinate logging so only the victory monument location is revealed.

## [1.0.0-beta.1] - 2026-04-02

### Added
- Victory Monument structure spawns near world spawn on first load.
- 16 color-coded dungeons (one per wool color) placed randomly around the monument.
- Configurable dungeon radius (min/max) and buffer distance between structures.
- Wool-carpet collection tracker: placing all 16 colors triggers the victory message.
- Server-side persistence prevents structure re-spawning across restarts.
- In-game configuration screen for dungeon placement settings.
- Hard infernal-style mob setups inside dungeons.
