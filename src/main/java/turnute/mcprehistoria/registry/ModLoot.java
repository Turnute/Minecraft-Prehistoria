package turnute.mcprehistoria.registry;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
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

	private static final ResourceKey<LootTable> SNIFFER_DIGGING_LOOT_TABLE = ResourceKey.create(
			Registries.LOOT_TABLE,
			Identifier.fromNamespaceAndPath("minecraft", "gameplay/sniffer_digging")
	);

	public static final ResourceKey<LootTable> DINO_JUNGLE_CAVERN_ARCHAEOLOGY = ResourceKey.create(
			Registries.LOOT_TABLE,
			MinecraftPrehistoria.id("archaeology/dino_jungle_cavern")
	);

	public static void register() {
		MinecraftPrehistoria.LOGGER.info("Registering loot hooks for {}", MinecraftPrehistoria.MOD_NAME);

		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (source.isBuiltin() && SNIFFER_DIGGING_LOOT_TABLE.equals(key)) {
				LootPool.Builder ancientBonePool = LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(
								LootItem.lootTableItem(ModItems.ANCIENT_BONE)
										.when(LootItemRandomChanceCondition.randomChance(0.15F))
										.apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
						);

				tableBuilder.withPool(ancientBonePool);
			}
		});
	}
}
