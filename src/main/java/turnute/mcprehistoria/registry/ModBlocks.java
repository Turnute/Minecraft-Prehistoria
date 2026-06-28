package turnute.mcprehistoria.registry;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import turnute.mcprehistoria.MinecraftPrehistoria;
import turnute.mcprehistoria.block.BlueMushroomBlock;
import turnute.mcprehistoria.block.ExcavationTableBlock;
import turnute.mcprehistoria.block.HatchingEggBlock;


/**
 * Register mod blocks here.
 * First target: the excavation table / table de fouille.
 */
public final class ModBlocks {
	private ModBlocks() {}

	public static final Block EXCAVATION_TABLE = register(
			"excavation_table",
			ExcavationTableBlock::new,
			BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.WOOD),
			true
	);

	public static final Block BLUE_MUSHROOM = register(
			"blue_mushroom",
			BlueMushroomBlock::new,
			BlockBehaviour.Properties.of()
					.noCollision()
					.noOcclusion()
					.instabreak()
					.sound(SoundType.GRASS)
					.lightLevel(state -> 14),
			true
	);

	public static final Block MEGAMELI_EGG = register(
			"megameli_egg",
			properties -> new HatchingEggBlock(ModEntities.MEGAMELI, properties),
			BlockBehaviour.Properties.of()
					.strength(0.5F)
					.sound(SoundType.DRIED_GHAST)
					.randomTicks()
					.noOcclusion(),
			true
	);

	public static final Block ODONTHERIUM_EGG = register(
			"odontherium_egg",
			properties -> new HatchingEggBlock(ModEntities.ODONTHERIUM, properties),
			BlockBehaviour.Properties.of()
					.strength(0.5F)
					.sound(SoundType.DRIED_GHAST)
					.randomTicks()
					.noOcclusion(),
			true
	);

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties properties, boolean shouldRegisterItem) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, MinecraftPrehistoria.id(name));
		Block block = blockFactory.apply(properties.setId(blockKey));

		if (shouldRegisterItem) {
			ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, MinecraftPrehistoria.id(name));
			BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
			Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
		}

		return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
	}

	public static void register() {
		MinecraftPrehistoria.LOGGER.info("Registering blocks for {}", MinecraftPrehistoria.MOD_NAME);
	}
}
