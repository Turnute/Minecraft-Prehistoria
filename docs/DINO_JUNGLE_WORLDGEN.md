# Dino Dino Jungle worldgen notes

The Dino Dino Jungle is now generated as a custom **Structure**, not as old chunk-local biome features.

## Active files

- `data/minecraft-prehistoria/worldgen/structure/dino_jungle_cavern.json`
  - Declares the cavern structure and its local spawn overrides.
- `data/minecraft-prehistoria/worldgen/structure_set/dino_jungle_caverns.json`
  - Controls how often caverns appear in the overworld.
- `data/minecraft-prehistoria/worldgen/biome/dino_jungle.json`
  - Defines the internal biome painted into the generated cavern volume.
- `ModBiomes.DINO_JUNGLE`
  - Resource key used by generation and spawn checks.
- `DinoJungleCavernStructure`
  - Finds valid overworld positions, depth, size, and entrance location.
- `DinoJungleCavernPiece`
  - Builds the cavern, paints the biome, creates the lake/shaft, and handles plants, trees, glow berries, vines, and organic walls.
- `ModEntitySpawns`
  - Registers spawn placement rules for the giant squid, Megameli, and natural sniffer spawning.

## Removed legacy path

The old `DinoJungleFeature`, `DinoJungleDecoratorFeature`, `DinoJungleEntranceFeature`, `DinoJungleMath`, placed features, configured features, and TerraBlender region were removed. They belonged to the older biome-feature generator and are no longer used by the structure version.

## Current tuning knobs

- Cavern frequency: `worldgen/structure_set/dino_jungle_caverns.json`
  - `spacing`: average grid distance between attempts.
  - `separation`: minimum distance between attempts.
- Cavern dimensions/depth: `DinoJungleCavernStructure`
  - `radiusX`, `radiusY`, `radiusZ`, and `wantedDepth`.
- Wall/floor materials: `DinoJungleCavernPiece#wallState`, `floorState`, `fillerState`, `ceilingState`.
- Vegetation density: `placePlant`, `placeTree`, `placeGlowBerryVines`, `placeTreeVines`.
- Mob balance: `dino_jungle.json`, `dino_jungle_cavern.json`, and the random throttles in `ModEntitySpawns`.

## Spawn balance

Sniffers and Megameli are both under the `creature` category now. Megameli used to be listed under `ambient`, which did not match its `MobCategory.CREATURE` entity type and made balancing messy. Sniffers also needed an explicit `SpawnPlacements` rule because vanilla sniffers do not naturally spawn from biome JSON alone.
