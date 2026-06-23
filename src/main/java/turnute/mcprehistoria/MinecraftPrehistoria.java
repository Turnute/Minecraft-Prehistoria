package turnute.mcprehistoria;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turnute.mcprehistoria.archaeology.ExcavationDescriptions;
import turnute.mcprehistoria.archaeology.ModArchaeology;
import turnute.mcprehistoria.entity.ModEntitySpawns;
import turnute.mcprehistoria.registry.*;
import turnute.mcprehistoria.village.ModVillagers;
import turnute.mcprehistoria.world.ModWorldgen;

public class MinecraftPrehistoria implements ModInitializer {
	public static final String MOD_ID = "minecraft-prehistoria";
	public static final String MOD_NAME = "Minecraft Prehistoria";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing {}", MOD_NAME);

		// Keep this order boring and predictable. During a mod-jam, boring is good.
		ModEntities.register();
		ModEntitySpawns.register();
		ModBlocks.register();
		ModItems.register();		// AFTER MODENTITIES!
		ModVillagers.register();
		ModArchaeology.register();
		ModLoot.register();
		ModWorldgen.register();
		ExcavationDescriptions.register();
		ModCreativeTabs.register();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
