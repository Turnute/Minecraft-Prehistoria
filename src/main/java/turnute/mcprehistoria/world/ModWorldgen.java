package turnute.mcprehistoria.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import turnute.mcprehistoria.MinecraftPrehistoria;
import turnute.mcprehistoria.world.biome.ModBiomes;
import turnute.mcprehistoria.world.feature.DinoJungleDecoratorFeature;
import turnute.mcprehistoria.world.feature.DinoJungleEntranceFeature;

public final class ModWorldgen {
    private ModWorldgen() {}

    public static final Feature<NoneFeatureConfiguration> DINO_JUNGLE_DECORATOR =
            new DinoJungleDecoratorFeature(NoneFeatureConfiguration.CODEC);

    public static final Feature<NoneFeatureConfiguration> DINO_JUNGLE_ENTRANCE =
            new DinoJungleEntranceFeature(NoneFeatureConfiguration.CODEC);

    // Compatibility alias for old generated/stale datapack files named
    // configured_feature/dino_jungle.json. This prevents registry loading from
    // crashing if Gradle or the local project still contains the first V0 JSON.
    public static final Feature<NoneFeatureConfiguration> DINO_JUNGLE_LEGACY =
            new DinoJungleDecoratorFeature(NoneFeatureConfiguration.CODEC);

    public static final ResourceKey<PlacedFeature> DINO_JUNGLE_DECORATOR_PLACED_KEY = ResourceKey.create(
            Registries.PLACED_FEATURE,
            MinecraftPrehistoria.id("dino_jungle_decorator")
    );

    public static final ResourceKey<PlacedFeature> DINO_JUNGLE_ENTRANCE_PLACED_KEY = ResourceKey.create(
            Registries.PLACED_FEATURE,
            MinecraftPrehistoria.id("dino_jungle_entrance")
    );

    public static void register() {
        MinecraftPrehistoria.LOGGER.info("Registering worldgen for {}", MinecraftPrehistoria.MOD_NAME);

        Registry.register(
                BuiltInRegistries.FEATURE,
                MinecraftPrehistoria.id("dino_jungle_decorator"),
                DINO_JUNGLE_DECORATOR
        );

        Registry.register(
                BuiltInRegistries.FEATURE,
                MinecraftPrehistoria.id("dino_jungle_entrance"),
                DINO_JUNGLE_ENTRANCE
        );

        Registry.register(
                BuiltInRegistries.FEATURE,
                MinecraftPrehistoria.id("dino_jungle"),
                DINO_JUNGLE_LEGACY
        );

        // Both features are chunk-local and internally check whether the dino jungle
        // biome is present, which avoids the "setBlock in a far chunk" crash.
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(ModBiomes.DINO_JUNGLE),
                GenerationStep.Decoration.UNDERGROUND_DECORATION,
                DINO_JUNGLE_DECORATOR_PLACED_KEY
        );

        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_DECORATION,
                DINO_JUNGLE_ENTRANCE_PLACED_KEY
        );
    }
}
