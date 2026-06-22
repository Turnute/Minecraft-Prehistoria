package turnute.mcprehistoria.archaeology;

import turnute.mcprehistoria.MinecraftPrehistoria;

/**
 * Archaeology systems that are not just item/block registration.
 *
 * Planned uses:
 * - excavation table descriptions
 * - helper methods to identify mod artifacts
 * - later: data-driven descriptions if time allows
 */
public final class ModArchaeology {
	private ModArchaeology() {}

	public static void register() {
		MinecraftPrehistoria.LOGGER.info("Registering archaeology systems for {}", MinecraftPrehistoria.MOD_NAME);
	}
}
