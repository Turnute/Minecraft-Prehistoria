# Dino Jungle worldgen notes

This version turns the prototype underground jungle pocket into a real biome-driven setup.

## What is in place

- `data/minecraft-prehistoria/worldgen/biome/dino_jungle.json`
  - Defines the biome resource used by TerraBlender.
- `ModBiomes.DINO_JUNGLE`
  - Resource key for future spawn rules and biome checks.
- `PrehistoriaOverworldRegion`
  - Registers the biome into TerraBlender's overworld climate map as a warm/wet underground slice.
- `DinoJungleDecoratorFeature`
  - Runs chunk-locally in the dino jungle biome.
  - Carves an underground biome-sized cavity between Y -50 and Y 30.
  - Builds floor/roof, a bedrock shell, plants, trees, lights, and stalactites.
- `DinoJungleEntranceFeature`
  - Runs rarely in the overworld.
  - If the chunk has dino jungle below, it carves a shaft down to it.
  - It always creates a natural lake under that entrance.

## Why the generator is chunk-local

The previous prototype was a single large feature. Minecraft crashed because large features can try to write into chunks too far from the chunk currently being generated.

This version never writes outside the current chunk in X/Z. That means the full biome is built chunk by chunk instead of as one enormous feature.

## Future mob spawns

When you add prehistoric mobs, target the biome directly:

```java
BiomeModifications.addSpawn(
        BiomeSelectors.includeByKey(ModBiomes.DINO_JUNGLE),
        MobCategory.CREATURE,
        ModEntities.YOUR_DINO,
        80,
        1,
        3
);
```

Then add a normal `SpawnPlacements.register(...)` rule for the mob.

## Tuning

- Biome frequency: `MinecraftPrehistoria#onTerraBlenderInitialized`, region weight currently `6`.
- Entrance rarity: `placed_feature/dino_jungle_entrance.json`, `chance` currently `180`.
- Floor/roof Y range: `DinoJungleMath.MIN_Y` and `MAX_Y`; biome columns are detected by sampling several Y values in that range.
- Light density: `DinoJungleDecoratorFeature#placeCeilingLight` and `placeWallOrFloorLight`.


## Fix2 note

The legacy `minecraft-prehistoria:dino_jungle` feature id is now registered as a safe alias for the chunk-local decorator. This avoids registry crashes from stale V0 `dino_jungle.json` configured features left in `build/resources` or an overwritten local project. Still run `./gradlew.bat clean runClient` after replacing files.


## fix3 note: normal Overworld biome injection

The first TerraBlender version used one raw Climate.ParameterPoint, which made the biome valid in Single Biome worlds but too hard to find in a normal Overworld.

fix3 changes PrehistoriaOverworldRegion to use TerraBlender's VanillaParameterOverlayBuilder + ParameterPointListBuilder pattern. It also temporarily raises the region weight to 60 so `/locate biome minecraft-prehistoria:dino_jungle` can find the biome during testing. After verifying generation, try lowering the weight to 20, 12, or 6.
