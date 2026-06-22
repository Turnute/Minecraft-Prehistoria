package turnute.mcprehistoria.village;

import turnute.mcprehistoria.MinecraftPrehistoria;

/**
 * Villager-related registration.
 *
 * Planned uses:
 * - archaeologist profession
 * - excavation table point of interest
 * - trades: bones -> emeralds, map to Dino Dino Jungle, ancient seeds, sniffer egg
 */
public final class ModVillagers {
	private ModVillagers() {}

	public static void register() {
		MinecraftPrehistoria.LOGGER.info("Registering villagers for {}", MinecraftPrehistoria.MOD_NAME);
	}
}
