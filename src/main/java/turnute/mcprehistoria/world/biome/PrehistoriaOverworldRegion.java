package turnute.mcprehistoria.world.biome;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.VanillaParameterOverlayBuilder;

import java.util.function.Consumer;

import static terrablender.api.ParameterUtils.*;

public class PrehistoriaOverworldRegion extends Region {
    public PrehistoriaOverworldRegion(Identifier name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        // TerraBlender's own examples use VanillaParameterOverlayBuilder instead of
        // pushing a single raw Climate.ParameterPoint directly into the mapper. The
        // overlay builder makes our parameter points override compatible slices of
        // vanilla climate space, while the generated inverse points defer the rest
        // back to vanilla.
        VanillaParameterOverlayBuilder builder = new VanillaParameterOverlayBuilder();

        // Debug-friendly but still underground-oriented: this makes the biome common
        // enough for /locate biome to find it, without turning every surface biome
        // into dino_jungle. Once it is verified in-game, reduce the region weight in
        // MinecraftPrehistoria.onTerraBlenderInitialized().
        new ParameterPointListBuilder()
                .temperature(Temperature.FULL_RANGE)
                .humidity(Humidity.FULL_RANGE)
                .continentalness(
                        Continentalness.NEAR_INLAND,
                        Continentalness.MID_INLAND,
                        Continentalness.FAR_INLAND
                )
                .erosion(Erosion.FULL_RANGE)
                .depth(Depth.UNDERGROUND)
                .weirdness(Weirdness.FULL_RANGE)
                .build()
                .forEach(point -> builder.add(point, ModBiomes.DINO_JUNGLE));

        builder.build().forEach(mapper);
    }
}
