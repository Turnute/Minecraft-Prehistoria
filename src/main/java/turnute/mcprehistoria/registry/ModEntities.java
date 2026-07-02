package turnute.mcprehistoria.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import turnute.mcprehistoria.MinecraftPrehistoria;
import turnute.mcprehistoria.entity.aquatic.GiantSquidEntity;
import turnute.mcprehistoria.entity.insect.MegameliEntity;
import turnute.mcprehistoria.entity.dino.OdontheriumEntity;
import turnute.mcprehistoria.entity.dino.EkriktikosaurusEntity;
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

	public static final EntityType<GiantSquidEntity> GIANT_SQUID = register(
			"giant_squid",
			EntityType.Builder.of(GiantSquidEntity::new, MobCategory.WATER_CREATURE)
					.sized(0.8f, 0.8f)
					.clientTrackingRange(20)
	);

	public static final EntityType<MegameliEntity> MEGAMELI = register(
			"megameli",
			EntityType.Builder.of(MegameliEntity::new, MobCategory.CREATURE)
					.sized(1.4f, 1.0f)
					.clientTrackingRange(10)
	);

	public static final EntityType<OdontheriumEntity> ODONTHERIUM = register(
			"odontherium",
			EntityType.Builder.of(OdontheriumEntity::new, MobCategory.MONSTER)
					.sized(1.25f, 1.35f)
					.clientTrackingRange(10)
	);

	public static final EntityType<EkriktikosaurusEntity> EKRIKTIKOSAURUS = register(
			"ekriktikosaurus",
			EntityType.Builder.of(EkriktikosaurusEntity::new, MobCategory.MONSTER)
					.sized(1.25f, 2.25f)
					.clientTrackingRange(10)
	);

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(
				Registries.ENTITY_TYPE,
				MinecraftPrehistoria.id(name)
		);

		return Registry.register(
				BuiltInRegistries.ENTITY_TYPE,
				key,
				builder.build(key)
		);
	}

	public static void register() {
		MinecraftPrehistoria.LOGGER.info("Registering entities for {}", MinecraftPrehistoria.MOD_NAME);
		registerAttributes();
	}

	public static void registerAttributes() {
		FabricDefaultAttributeRegistry.register(
				GIANT_SQUID,
				GiantSquidEntity.createAttributes()
		);

		FabricDefaultAttributeRegistry.register(
				MEGAMELI,
				MegameliEntity.createAttributes()
		);

		FabricDefaultAttributeRegistry.register(
				ODONTHERIUM,
				OdontheriumEntity.createAttributes()
		);

		FabricDefaultAttributeRegistry.register(
				EKRIKTIKOSAURUS,
				EkriktikosaurusEntity.createAttributes()
		);
	}
}
