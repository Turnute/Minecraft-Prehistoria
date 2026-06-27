package turnute.mcprehistoria.village;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PoiHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.block.Block;
import turnute.mcprehistoria.MinecraftPrehistoria;
import turnute.mcprehistoria.registry.ModBlocks;

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
	public static ResourceKey<PoiType> ARCHAEOLOGIST_POI_KEY = ResourceKey.create(
			Registries.POINT_OF_INTEREST_TYPE,
			MinecraftPrehistoria.id("archaeologist")
	);

	public static final ResourceKey<VillagerProfession> ARCHAEOLOGIST_KEY = ResourceKey.create(
			Registries.VILLAGER_PROFESSION,
			MinecraftPrehistoria.id("archaeologist")
	);

	public static final PoiType ARCHAEOLOGIST_POI = PoiHelper.register(
			MinecraftPrehistoria.id("archaeologist"),
			1,
			1,
			ModBlocks.EXCAVATION_TABLE
	);


	private static final ResourceKey<TradeSet> ARCHAEOLOGIST_LEVEL_1 = tradeSet("archaeologist/level_1");
	private static final ResourceKey<TradeSet> ARCHAEOLOGIST_LEVEL_2 = tradeSet("archaeologist/level_2");
	private static final ResourceKey<TradeSet> ARCHAEOLOGIST_LEVEL_3 = tradeSet("archaeologist/level_3");
	private static final ResourceKey<TradeSet> ARCHAEOLOGIST_LEVEL_4 = tradeSet("archaeologist/level_4");
	private static final ResourceKey<TradeSet> ARCHAEOLOGIST_LEVEL_5 = tradeSet("archaeologist/level_5");

	public static final VillagerProfession ARCHAEOLOGIST = Registry.register(
			BuiltInRegistries.VILLAGER_PROFESSION,
			ARCHAEOLOGIST_KEY,
			new VillagerProfession(
					Component.translatable("entity.minecraft-prehistoria.villager.archaeologist"),
					holder -> holder.is(ARCHAEOLOGIST_POI_KEY),
					holder -> holder.is(ARCHAEOLOGIST_POI_KEY),
					ImmutableSet.<Item>of(Items.BONE, Items.BRUSH),
					ImmutableSet.<Block>of(ModBlocks.EXCAVATION_TABLE),
					SoundEvents.VILLAGER_WORK_MASON,
					Int2ObjectMap.ofEntries(
							Int2ObjectMap.entry(1, ARCHAEOLOGIST_LEVEL_1),
							Int2ObjectMap.entry(2, ARCHAEOLOGIST_LEVEL_2),
							Int2ObjectMap.entry(3, ARCHAEOLOGIST_LEVEL_3),
							Int2ObjectMap.entry(4, ARCHAEOLOGIST_LEVEL_4),
							Int2ObjectMap.entry(5, ARCHAEOLOGIST_LEVEL_5)
					)
			)
	);

	private static ResourceKey<TradeSet> tradeSet(String path) {
		return ResourceKey.create(Registries.TRADE_SET, MinecraftPrehistoria.id(path));
	}

	public static void register() {
		MinecraftPrehistoria.LOGGER.info("Registering villagers for {}", MinecraftPrehistoria.MOD_NAME);
	}
}
