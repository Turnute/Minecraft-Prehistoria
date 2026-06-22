package turnute.mcprehistoria.registry;

import turnute.mcprehistoria.MinecraftPrehistoria;

/**
 * Register every item here: mob eggs, archaeology finds, maps, seeds, etc.
 *
 * Suggested order for the jam:
 * 1. Simple placeholder items.
 * 2. Mob eggs.
 * 3. Special items with behavior.
 */
public final class ModItems {
	private ModItems() {}

	public static void register() {
		MinecraftPrehistoria.LOGGER.info("Registering items for {}", MinecraftPrehistoria.MOD_NAME);
	}
}
