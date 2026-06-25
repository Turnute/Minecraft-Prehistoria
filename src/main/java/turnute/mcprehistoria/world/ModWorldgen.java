package turnute.mcprehistoria.world;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import turnute.mcprehistoria.MinecraftPrehistoria;
import turnute.mcprehistoria.world.feature.DinoJungleDecoratorFeature;
import turnute.mcprehistoria.world.feature.DinoJungleEntranceFeature;
import turnute.mcprehistoria.world.structure.ModStructures;

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

        // The actual Dino Jungle is now a Structure, not a biome feature.
        // Keep the old feature registry entries above so stale datapack/generated JSON
        // files do not crash registry loading, but do not inject them into biomes.
        ModStructures.register();
    }
}
