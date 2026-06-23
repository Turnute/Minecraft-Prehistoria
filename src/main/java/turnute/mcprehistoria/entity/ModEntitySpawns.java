package turnute.mcprehistoria.entity;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import turnute.mcprehistoria.entity.aquatic.GiantSquidEntity;
import turnute.mcprehistoria.registry.ModEntities;

public class ModEntitySpawns {

    public static void register() {
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(
                        Biomes.DEEP_OCEAN,
                        Biomes.DEEP_COLD_OCEAN,
                        Biomes.DEEP_FROZEN_OCEAN,
                        Biomes.DEEP_LUKEWARM_OCEAN
                ),
                MobCategory.WATER_CREATURE,
                ModEntities.GIANT_SQUID,
                GiantSquidEntity.SPAWN_WEIGHT,
                GiantSquidEntity.SPAWN_MIN_GROUP,
                GiantSquidEntity.SPAWN_MAX_GROUP
        );

        SpawnPlacements.register(
                ModEntities.GIANT_SQUID,
                SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GiantSquidEntity::canSpawn
        );
    }
}