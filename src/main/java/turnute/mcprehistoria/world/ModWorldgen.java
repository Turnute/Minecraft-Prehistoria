package turnute.mcprehistoria.world;

import turnute.mcprehistoria.MinecraftPrehistoria;
import turnute.mcprehistoria.world.structure.ModStructures;

public final class ModWorldgen {
    private ModWorldgen() {}

    public static void register() {
        MinecraftPrehistoria.LOGGER.info("Registering worldgen for {}", MinecraftPrehistoria.MOD_NAME);
        ModStructures.register();
    }
}
