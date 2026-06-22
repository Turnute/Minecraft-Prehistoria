package turnute.mcprehistoria.registry;

import turnute.mcprehistoria.MinecraftPrehistoria;

/**
 * Register all EntityType objects here.
 *
 * Keep mobs grouped by gameplay role, not by implementation difficulty:
 * - small ambient / passive dinos
 * - medium jungle threats
 * - big showcase mob
 * - giant squid for deep oceans
 */
public final class ModEntities {
	private ModEntities() {}

	public static void register() {
		MinecraftPrehistoria.LOGGER.info("Registering entities for {}", MinecraftPrehistoria.MOD_NAME);
	}
}
