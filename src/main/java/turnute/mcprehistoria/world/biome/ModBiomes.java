package turnute.mcprehistoria.world.biome;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import turnute.mcprehistoria.MinecraftPrehistoria;

public final class ModBiomes {
    private ModBiomes() {}

    public static final ResourceKey<Biome> DINO_JUNGLE = ResourceKey.create(
            Registries.BIOME,
            MinecraftPrehistoria.id("dino_jungle")
    );
}
