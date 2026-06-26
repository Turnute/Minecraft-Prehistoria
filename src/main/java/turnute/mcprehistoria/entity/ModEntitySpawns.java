package turnute.mcprehistoria.entity;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import turnute.mcprehistoria.MinecraftPrehistoria;
import turnute.mcprehistoria.entity.aquatic.GiantSquidEntity;
import turnute.mcprehistoria.entity.insect.MegameliEntity;
import turnute.mcprehistoria.registry.ModEntities;
import turnute.mcprehistoria.world.biome.ModBiomes;

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

        registerSpawnPlacementSafely(
                ModEntities.GIANT_SQUID,
                SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GiantSquidEntity::canSpawn
        );

        registerSpawnPlacementSafely(
                ModEntities.MEGAMELI,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntitySpawns::canMegameliSpawn
        );

        // Vanilla sniffers do not define natural spawn placement rules. The biome JSON can list
        // them forever and they still will not appear unless we give natural spawning a valid rule.
        registerSpawnPlacementSafely(
                EntityType.SNIFFER,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntitySpawns::canDinoSnifferSpawn
        );
    }

    private static <T extends Mob> void registerSpawnPlacementSafely(
            EntityType<T> entityType,
            SpawnPlacementType placementType,
            Heightmap.Types heightmap,
            SpawnPlacements.SpawnPredicate<T> predicate
    ) {
        try {
            SpawnPlacements.register(entityType, placementType, heightmap, predicate);
        } catch (IllegalStateException exception) {
            MinecraftPrehistoria.LOGGER.debug(
                    "Spawn placement already registered for {}",
                    EntityType.getKey(entityType),
                    exception
            );
        }
    }

    private static boolean canDinoSnifferSpawn(
            EntityType<Sniffer> entityType,
            ServerLevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            net.minecraft.util.RandomSource random
    ) {
        return isDinoJungle(level, pos)
                //&& isDinoFloor(level.getBlockState(pos.below()))
                && level.getBlockState(pos).isAir()
                && level.getFluidState(pos).isEmpty()
                && random.nextInt(5) == 0;
    }

    private static boolean canMegameliSpawn(
            EntityType<MegameliEntity> entityType,
            ServerLevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            net.minecraft.util.RandomSource random
    ) {
        return isDinoJungle(level, pos)
                //&& isDinoFloor(level.getBlockState(pos.below()))
                && level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && level.getFluidState(pos).isEmpty()
                && random.nextInt(12) == 0;
    }

    private static boolean isDinoJungle(ServerLevelAccessor level, BlockPos pos) {
        return level.getBiome(pos).is(ModBiomes.DINO_JUNGLE);
    }

    private static boolean isDinoFloor(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.MUD)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.PODZOL);
    }
}
