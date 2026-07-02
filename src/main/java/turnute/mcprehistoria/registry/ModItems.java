package turnute.mcprehistoria.registry;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
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

	public static final Item GIANT_SQUID_SPAWN_EGG = register(
			"giant_squid_spawn_egg",
			SpawnEggItem::new,
			new Item.Properties().spawnEgg(ModEntities.GIANT_SQUID)
	);

	public static final Item MEGAMELI_SPAWN_EGG = register(
			"megameli_spawn_egg",
			SpawnEggItem::new,
			new Item.Properties().spawnEgg(ModEntities.MEGAMELI)
	);

	public static final Item ODONTHERIUM_SPAWN_EGG = register(
			"odontherium_spawn_egg",
			SpawnEggItem::new,
			new Item.Properties().spawnEgg(ModEntities.ODONTHERIUM)
	);

	public static final Item EKRIKTIKOSAURUS_SPAWN_EGG = register(
			"ekriktikosaurus_spawn_egg",
			SpawnEggItem::new,
			new Item.Properties().spawnEgg(ModEntities.EKRIKTIKOSAURUS)
	);

	public static final Item ANCIENT_BONE = register(
			"ancient_bone",
			Item::new,
			new Item.Properties()
	);

	private static Item register(String name, Function<Item.Properties, Item> itemFactory, Item.Properties properties) {
		ResourceKey<Item> itemKey = ResourceKey.create(
				Registries.ITEM,
				MinecraftPrehistoria.id(name)
		);

		Item item = itemFactory.apply(properties.setId(itemKey));

		return Registry.register(
				BuiltInRegistries.ITEM,
				itemKey,
				item
		);
	}

	public static void register() {
		MinecraftPrehistoria.LOGGER.info("Registering items for {}", MinecraftPrehistoria.MOD_NAME);
	}
}
