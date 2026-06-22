package turnute.mcprehistoria.registry;

import turnute.mcprehistoria.MinecraftPrehistoria;

/**
 * Central place for loot-related hooks.
 *
 * Planned uses:
 * - suspicious sand / suspicious gravel / suspicious dirt loot entries
 * - archaeology loot pools for mob eggs
 * - potential mob drops
 */
public final class ModLoot {
	private ModLoot() {}

	public static void register() {
		MinecraftPrehistoria.LOGGER.info("Registering loot hooks for {}", MinecraftPrehistoria.MOD_NAME);
	}
}
