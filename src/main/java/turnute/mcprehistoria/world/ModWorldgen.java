package turnute.mcprehistoria.world;

import turnute.mcprehistoria.MinecraftPrehistoria;

/**
 * World generation registration.
 *
 * Planned uses:
 * - Dino Dino Jungle structure registration
 * - underground placement rules, roughly Y 30 to Y -50
 * - deep-ocean giant squid spawn rules
 */
public final class ModWorldgen {
	private ModWorldgen() {}

	public static void register() {
		MinecraftPrehistoria.LOGGER.info("Registering worldgen for {}", MinecraftPrehistoria.MOD_NAME);
	}
}
