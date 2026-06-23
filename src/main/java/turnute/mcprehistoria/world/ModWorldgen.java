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
import turnute.mcprehistoria.MinecraftPrehistoria;
import turnute.mcprehistoria.world.feature.DinoJungleFeature;

public final class ModWorldgen {
	private ModWorldgen() {}

	public static final Feature<NoneFeatureConfiguration> DINO_JUNGLE =
			new DinoJungleFeature(NoneFeatureConfiguration.CODEC);

	public static final ResourceKey<net.minecraft.world.level.levelgen.placement.PlacedFeature> DINO_JUNGLE_PLACED_KEY =
			ResourceKey.create(
					Registries.PLACED_FEATURE,
					MinecraftPrehistoria.id("dino_jungle")
			);

	public static void register() {
		MinecraftPrehistoria.LOGGER.info("Registering worldgen for {}", MinecraftPrehistoria.MOD_NAME);

		Registry.register(
				BuiltInRegistries.FEATURE,
				MinecraftPrehistoria.id("dino_jungle"),
				DINO_JUNGLE
		);

		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Decoration.UNDERGROUND_DECORATION,
				DINO_JUNGLE_PLACED_KEY
		);
	}
}