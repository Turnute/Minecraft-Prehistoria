package turnute.mcprehistoria.world.structure;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import turnute.mcprehistoria.block.BlueMushroomBlock;
import turnute.mcprehistoria.registry.ModBlocks;
import turnute.mcprehistoria.registry.ModEntities;
import turnute.mcprehistoria.world.biome.ModBiomes;

public class DinoJungleCavernPiece extends StructurePiece {
    private static final int SHELL_PADDING = 6;
    private static final int SHAFT_AIR_RADIUS = 3;
    private static final int SHAFT_WALL_RADIUS = 5;
    private static final int LAKE_RADIUS_X = 14;
    private static final int LAKE_RADIUS_Z = 12;
    private static final int MIN_WALKABLE_HEIGHT = 23;
    private static final double INNER_FOOTPRINT = 1.0D;
    private static final double WALL_FOOTPRINT = 1.12D;
    private static final int FLOOR_SHELL_THICKNESS = 5;
    private static final int ROOF_SHELL_THICKNESS = 5;


    private final BlockPos center;
    private final BlockPos entrance;
    private final int surfaceY;
    private final int radiusX;
    private final int radiusY;
    private final int radiusZ;

    public DinoJungleCavernPiece(
            BlockPos center,
            BlockPos entrance,
            int surfaceY,
            int radiusX,
            int radiusY,
            int radiusZ
    ) {
        super(ModStructures.DINO_JUNGLE_CAVERN_PIECE, 0, makeBoundingBox(center, entrance, surfaceY, radiusX, radiusY, radiusZ));
        this.center = center;
        this.entrance = entrance;
        this.surfaceY = surfaceY;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.radiusZ = radiusZ;
    }

    public DinoJungleCavernPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.DINO_JUNGLE_CAVERN_PIECE, tag);
        this.center = new BlockPos(
                tag.getIntOr("CenterX", 0),
                tag.getIntOr("CenterY", -40),
                tag.getIntOr("CenterZ", 0)
        );
        this.entrance = new BlockPos(
                tag.getIntOr("EntranceX", center.getX()),
                tag.getIntOr("EntranceY", 64),
                tag.getIntOr("EntranceZ", center.getZ())
        );
        this.surfaceY = tag.getIntOr("SurfaceY", entrance.getY());
        this.radiusX = tag.getIntOr("RadiusX", 64);
        this.radiusY = tag.getIntOr("RadiusY", 34);
        this.radiusZ = tag.getIntOr("RadiusZ", 60);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CenterX", center.getX());
        tag.putInt("CenterY", center.getY());
        tag.putInt("CenterZ", center.getZ());
        tag.putInt("EntranceX", entrance.getX());
        tag.putInt("EntranceY", entrance.getY());
        tag.putInt("EntranceZ", entrance.getZ());
        tag.putInt("SurfaceY", surfaceY);
        tag.putInt("RadiusX", radiusX);
        tag.putInt("RadiusY", radiusY);
        tag.putInt("RadiusZ", radiusZ);
    }

    @Override
    public BlockPos getLocatorPosition() {
        return entrance;
    }

    @Override
    public void postProcess(
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator chunkGenerator,
            RandomSource random,
            BoundingBox chunkBounds,
            ChunkPos chunkPos,
            BlockPos pivot
    ) {
        paintDinoBiome(level, chunkBounds);
        buildOverworldSliceCavern(level, chunkBounds);
        buildEntranceLake(level, chunkBounds);
        buildSingleShaft(level, chunkBounds);
        decorateCavern(level, chunkBounds);
        spawnInitialCavernMobs(level, chunkBounds);
    }

    private void spawnInitialCavernMobs(WorldGenLevel level, BoundingBox chunkBounds) {
        int minX = Math.max(this.boundingBox.minX(), chunkBounds.minX());
        int maxX = Math.min(this.boundingBox.maxX(), chunkBounds.maxX());
        int minZ = Math.max(this.boundingBox.minZ(), chunkBounds.minZ());
        int maxZ = Math.min(this.boundingBox.maxZ(), chunkBounds.maxZ());

        for(int x = minX; x <= maxX; x++) {
            for(int z = minZ; z <= maxZ; z++) {
                if(horizontalDistance(x, z) > 0.76D || isLakeColumn(x, z) || isShaftColumn(x, z)) {
                    continue;
                }

                int floorY = floorY(x, z);
                BlockPos groundPos = new BlockPos(x, floorY, z);
                BlockPos spawnPos = groundPos.above();

                if(!isCreatureSpawnFloor(level.getBlockState(groundPos))) {
                    continue;
                }

                if(hashRange(x, floorY, z, 900, 1100) == 0) {
                    spawnCavernMob(level, chunkBounds, spawnPos, EntityType.SNIFFER, 901);
                }

                if(hashRange(x, floorY, z, 902, 1400) == 0) {
                    spawnCavernMob(level, chunkBounds, spawnPos, ModEntities.MEGAMELI, 903);
                }

                if(hashRange(x, floorY, z, 902, 1600) == 0) {
                    spawnCavernMob(level, chunkBounds, spawnPos, ModEntities.ODONTHERIUM, 900);
                }
            }
        }
    }

    private boolean isCreatureSpawnFloor(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.MUD)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.PODZOL);
    }

    private <T extends Mob> void spawnCavernMob(
            WorldGenLevel level,
            BoundingBox chunkBounds,
            BlockPos pos,
            EntityType<T> entityType,
            int salt
    ) {
        if(!chunkBounds.isInside(pos)
            || level.isOutsideBuildHeight(pos)
            || !level.getBlockState(pos).isAir()
            || !level.getBlockState(pos.above()).isAir()
            || !level.getFluidState(pos).isEmpty()
        ) { return; }

        T mob = entityType.create(level.getLevel(), EntitySpawnReason.CHUNK_GENERATION);

        if (mob == null) {
            return;
        }

        float yaw = hashRange(pos.getX(), pos.getY(), pos.getZ(), salt, 360);

        mob.setPos(
                pos.getX() + 0.5D,
                pos.getY(),
                pos.getZ() + 0.5D
        );

        mob.setYRot(yaw);
        mob.setXRot(0.0F);
        mob.yBodyRot = yaw;
        mob.yHeadRot = yaw;

        mob.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(pos),
                EntitySpawnReason.CHUNK_GENERATION,
                null
        );

        level.addFreshEntity(mob);
    }

    /**
     * Builds a height-field cavern instead of a 3D ellipsoid.
     *
     * V2.2 intentionally keeps this boring at first: one continuous floor, one continuous ceiling,
     * and only low-frequency terrain noise. The older versions looked like swiss cheese because
     * too much cave-shaped detail was baked into the base volume. Details should come from
     * decoration later, not from holes in the terrain silhouette.
     */
    private void buildOverworldSliceCavern(WorldGenLevel level, BoundingBox chunkBounds) {
        int minX = Math.max(this.boundingBox.minX(), chunkBounds.minX());
        int maxX = Math.min(this.boundingBox.maxX(), chunkBounds.maxX());
        int minY = Math.max(this.boundingBox.minY(), chunkBounds.minY());
        int maxY = Math.min(this.boundingBox.maxY(), chunkBounds.maxY());
        int minZ = Math.max(this.boundingBox.minZ(), chunkBounds.minZ());
        int maxZ = Math.min(this.boundingBox.maxZ(), chunkBounds.maxZ());

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double footprint = horizontalDistance(x, z);
                if (footprint > WALL_FOOTPRINT) {
                    continue;
                }

                int floorY = floorY(x, z);
                int roofY = roofY(x, z);

                for (int y = minY; y <= maxY; y++) {
                    pos.set(x, y, z);
                    if (level.isOutsideBuildHeight(pos)) {
                        continue;
                    }

                    if (footprint <= INNER_FOOTPRINT) {
                        placeInteriorColumnBlock(level, pos, x, y, z, floorY, roofY);
                    } else if (y >= floorY - FLOOR_SHELL_THICKNESS && y <= roofY + ROOF_SHELL_THICKNESS) {
                        level.setBlock(pos, wallState(x, y, z), 2);
                    }
                }
            }
        }
    }

    private void placeInteriorColumnBlock(
            WorldGenLevel level,
            BlockPos.MutableBlockPos pos,
            int x,
            int y,
            int z,
            int floorY,
            int roofY
    ) {
        if (y < floorY - FLOOR_SHELL_THICKNESS) {
            return;
        }

        if (y == floorY - FLOOR_SHELL_THICKNESS) {
            level.setBlock(pos, wallState(x, y, z), 2);
        } else if (y < floorY) {
            level.setBlock(pos, fillerState(x, y, z), 2);
        } else if (y == floorY) {
            level.setBlock(pos, floorState(x, z), 2);
        } else if (y < roofY) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        } else if (y <= roofY + ROOF_SHELL_THICKNESS) {
            level.setBlock(pos, ceilingState(x, y, z), 2);
        }
    }

    private void buildEntranceLake(WorldGenLevel level, BoundingBox chunkBounds) {
        int lakeWaterY = lakeWaterY();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int minX = Math.max(entrance.getX() - LAKE_RADIUS_X - 4, chunkBounds.minX());
        int maxX = Math.min(entrance.getX() + LAKE_RADIUS_X + 4, chunkBounds.maxX());
        int minZ = Math.max(entrance.getZ() - LAKE_RADIUS_Z - 4, chunkBounds.minZ());
        int maxZ = Math.min(entrance.getZ() + LAKE_RADIUS_Z + 4, chunkBounds.maxZ());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double lakeDistance = lakeDistance(x, z);
                if (lakeDistance > 1.42D || horizontalDistance(x, z) > 0.96D) {
                    continue;
                }

                int roofY = roofY(x, z);

                if (lakeDistance <= 1.0D) {
                    int bottomY = lakeBottomY(lakeWaterY, lakeDistance);

                    for (int y = bottomY - 3; y < bottomY; y++) {
                        pos.set(x, y, z);
                        placeIfInBounds(level, chunkBounds, pos, Blocks.SAND.defaultBlockState());
                    }

                    pos.set(x, bottomY, z);
                    placeIfInBounds(level, chunkBounds, pos, lakeBottomState(x, bottomY, z));

                    for (int y = bottomY + 1; y <= lakeWaterY; y++) {
                        pos.set(x, y, z);
                        placeIfInBounds(level, chunkBounds, pos, Blocks.WATER.defaultBlockState());
                    }

                    for (int y = lakeWaterY + 1; y < roofY; y++) {
                        pos.set(x, y, z);
                        placeIfInBounds(level, chunkBounds, pos, Blocks.AIR.defaultBlockState());
                    }
                } else {
                    buildSandyShore(level, chunkBounds, pos, x, z, lakeWaterY, roofY, lakeDistance);
                }
            }
        }
    }

    private void buildSandyShore(
            WorldGenLevel level,
            BoundingBox chunkBounds,
            BlockPos.MutableBlockPos pos,
            int x,
            int z,
            int lakeWaterY,
            int roofY,
            double lakeDistance
    ) {
        int shoreY = lakeDistance < 1.20D ? lakeWaterY : lakeWaterY + 1;

        for (int y = shoreY - 3; y < shoreY; y++) {
            pos.set(x, y, z);
            placeIfInBounds(level, chunkBounds, pos, Blocks.SAND.defaultBlockState());
        }

        pos.set(x, shoreY, z);
        placeIfInBounds(level, chunkBounds, pos, Blocks.SAND.defaultBlockState());

        for (int y = shoreY + 1; y < roofY; y++) {
            pos.set(x, y, z);
            placeIfInBounds(level, chunkBounds, pos, Blocks.AIR.defaultBlockState());
        }
    }

    private int lakeBottomY(int lakeWaterY, double lakeDistance) {
        if (lakeDistance < 0.42D) {
            return lakeWaterY - 4;
        }
        if (lakeDistance < 0.72D) {
            return lakeWaterY - 3;
        }
        if (lakeDistance < 0.92D) {
            return lakeWaterY - 2;
        }
        return lakeWaterY - 1;
    }

    private void buildSingleShaft(WorldGenLevel level, BoundingBox chunkBounds) {
        int lakeWaterY = lakeWaterY();
        int roofY = roofY(entrance.getX(), entrance.getZ());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // The air column starts just above the lake, but the organic wall ring only starts at the ceiling.
        // That gives an actual hole in the roof instead of a pipe descending into the cavern.
        for (int y = lakeWaterY + 1; y <= surfaceY + 1; y++) {
            for (int dx = -SHAFT_WALL_RADIUS; dx <= SHAFT_WALL_RADIUS; dx++) {
                for (int dz = -SHAFT_WALL_RADIUS; dz <= SHAFT_WALL_RADIUS; dz++) {
                    int distSquared = dx * dx + dz * dz;
                    if (distSquared > SHAFT_WALL_RADIUS * SHAFT_WALL_RADIUS) {
                        continue;
                    }

                    int x = entrance.getX() + dx;
                    int z = entrance.getZ() + dz;
                    pos.set(x, y, z);

                    if (!chunkBounds.isInside(pos) || level.isOutsideBuildHeight(pos)) {
                        continue;
                    }

                    if (distSquared <= SHAFT_AIR_RADIUS * SHAFT_AIR_RADIUS) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    } else if (y >= roofY) {
                        level.setBlock(pos, shaftWallState(y), 2);
                    }
                }
            }

            if (y >= roofY + 4 && y < surfaceY - 8 && y % 17 == Math.floorMod(hash(entrance.getX(), y, entrance.getZ(), 300), 17)) {
                pos.set(entrance.getX() + SHAFT_AIR_RADIUS, y, entrance.getZ());
                placeIfInBounds(level, chunkBounds, pos, Blocks.SHROOMLIGHT.defaultBlockState());
            }
        }
    }

    private void decorateCavern(WorldGenLevel level, BoundingBox chunkBounds) {
        int minX = Math.max(this.boundingBox.minX(), chunkBounds.minX());
        int maxX = Math.min(this.boundingBox.maxX(), chunkBounds.maxX());
        int minZ = Math.max(this.boundingBox.minZ(), chunkBounds.minZ());
        int maxZ = Math.min(this.boundingBox.maxZ(), chunkBounds.maxZ());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (horizontalDistance(x, z) > 0.86D || isLakeColumn(x, z) || isShaftColumn(x, z)) {
                    continue;
                }

                int floorY = floorY(x, z);
                int roofY = roofY(x, z);

                placeCeilingLight(level, chunkBounds, x, z, roofY);
                placeGlowBerryVines(level, chunkBounds, x, z, roofY, floorY);
                placeHangingRoots(level, chunkBounds, x, z, roofY);
                placeSporeBlossom(level, chunkBounds, x, z, roofY);
                placeStalactite(level, chunkBounds, x, z, roofY, floorY);
                placePlant(level, chunkBounds, x, z, floorY);
                placeTree(level, chunkBounds, x, z, floorY, roofY);
            }
        }
    }

    private void paintDinoBiome(WorldGenLevel level, BoundingBox chunkBounds) {
        Holder<Biome> dinoBiome = level.registryAccess()
                .lookupOrThrow(Registries.BIOME)
                .getOrThrow(ModBiomes.DINO_JUNGLE);

        int minX = Math.max(this.boundingBox.minX(), chunkBounds.minX());
        int maxX = Math.min(this.boundingBox.maxX(), chunkBounds.maxX());
        int minY = Math.max(this.boundingBox.minY(), chunkBounds.minY());
        int maxY = Math.min(this.boundingBox.maxY(), chunkBounds.maxY());
        int minZ = Math.max(this.boundingBox.minZ(), chunkBounds.minZ());
        int maxZ = Math.min(this.boundingBox.maxZ(), chunkBounds.maxZ());

        for (int x = floorToBiomeCell(minX); x <= maxX; x += 4) {
            for (int z = floorToBiomeCell(minZ); z <= maxZ; z += 4) {
                for (int y = floorToBiomeCell(minY); y <= maxY; y += 4) {
                    if (level.isOutsideBuildHeight(y)) {
                        continue;
                    }

                    if (isInsideDinoBiomeVolume(x, y, z)) {
                        setBiomeCell(level, x, y, z, dinoBiome);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void setBiomeCell(WorldGenLevel level, int x, int y, int z, Holder<Biome> biome) {
        ChunkAccess chunk = level.getChunk(x >> 4, z >> 4);
        int sectionIndex = chunk.getSectionIndex(y);

        if (sectionIndex < 0 || sectionIndex >= chunk.getSections().length) {
            return;
        }

        LevelChunkSection section = chunk.getSection(sectionIndex);
        PalettedContainerRO<Holder<Biome>> biomes = section.getBiomes();

        if (biomes instanceof PalettedContainer<?> rawContainer) {
            PalettedContainer<Holder<Biome>> mutableBiomes = (PalettedContainer<Holder<Biome>>) rawContainer;
            mutableBiomes.set((x & 15) >> 2, (y & 15) >> 2, (z & 15) >> 2, biome);
        }
    }

    private boolean isInsideDinoBiomeVolume(int x, int y, int z) {
        double footprint = horizontalDistance(x, z);
        if (footprint <= INNER_FOOTPRINT) {
            return y >= floorY(x, z) - FLOOR_SHELL_THICKNESS && y <= roofY(x, z) + ROOF_SHELL_THICKNESS;
        }

        return isShaftColumn(x, z) && y >= roofY(entrance.getX(), entrance.getZ()) && y <= surfaceY + 1;
    }

    private void placeCeilingLight(WorldGenLevel level, BoundingBox chunkBounds, int x, int z, int roofY) {
        if (hashRange(x, roofY, z, 41, 56) != 0) {
            return;
        }

        BlockPos pos = new BlockPos(x, roofY, z);
        BlockState glow = switch (hashRange(x, roofY, z, 42, 4)) {
            case 0 -> Blocks.VERDANT_FROGLIGHT.defaultBlockState();
            case 1 -> Blocks.OCHRE_FROGLIGHT.defaultBlockState();
            default -> Blocks.SHROOMLIGHT.defaultBlockState();
        };
        placeIfInBounds(level, chunkBounds, pos, glow);
    }

    private void placeGlowBerryVines(WorldGenLevel level, BoundingBox chunkBounds, int x, int z, int roofY, int floorY) {
        if (hashRange(x, roofY, z, 45, 13) != 0) {
            return;
        }

        int maxLength = Math.min(2 + hashRange(x, roofY, z, 46, 6), Math.max(1, roofY - floorY - 5));
        placeHangingCaveVine(level, chunkBounds, x, roofY - 1, z, maxLength, true, 47);
    }

    private void placeHangingRoots(WorldGenLevel level, BoundingBox chunkBounds, int x, int z, int roofY) {
        if (hashRange(x, roofY, z, 48, 18) != 0) {
            return;
        }

        BlockPos pos = new BlockPos(x, roofY - 1, z);
        if (chunkBounds.isInside(pos) && !level.isOutsideBuildHeight(pos) && level.getBlockState(pos).isAir()) {
            level.setBlock(pos, Blocks.HANGING_ROOTS.defaultBlockState(), 2);
        }
    }

    private void placeSporeBlossom(WorldGenLevel level, BoundingBox chunkBounds, int x, int z, int roofY) {
        if (hashRange(x, roofY, z, 49, 180) != 0) {
            return;
        }

        BlockPos pos = new BlockPos(x, roofY - 1, z);
        if (chunkBounds.isInside(pos) && !level.isOutsideBuildHeight(pos) && level.getBlockState(pos).isAir()) {
            level.setBlock(pos, Blocks.SPORE_BLOSSOM.defaultBlockState(), 2);
        }
    }

    private void placeStalactite(WorldGenLevel level, BoundingBox chunkBounds, int x, int z, int roofY, int floorY) {
        if (hashRange(x, roofY, z, 43, 58) != 0) {
            return;
        }

        int maxLength = Math.min(2 + hashRange(x, roofY, z, 44, 4), Math.max(1, roofY - floorY - 12));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int i = 1; i <= maxLength; i++) {
            pos.set(x, roofY - i, z);
            if (!chunkBounds.isInside(pos) || level.isOutsideBuildHeight(pos) || !level.getBlockState(pos).isAir()) {
                break;
            }

            level.setBlock(pos, Blocks.DRIPSTONE_BLOCK.defaultBlockState(), 2);
        }
    }

    private void placePlant(WorldGenLevel level, BoundingBox chunkBounds, int x, int z, int floorY) {
        BlockPos groundPos = new BlockPos(x, floorY, z);
        BlockPos plantPos = new BlockPos(x, floorY + 1, z);

        if (
                level.isOutsideBuildHeight(plantPos)
                        || !level.getBlockState(plantPos).isAir()
                        || !level.getFluidState(plantPos).isEmpty()
                        || !isPlantableDinoFloor(level.getBlockState(groundPos))
        ) {
            return;
        }

        // Do not plant on every single floor block: the cavern should feel lush, not carpeted.
        if (hashRange(x, floorY, z, 59, 2) != 0) {
            return;
        }

        int roll = hashRange(x, floorY, z, 60, 100);

        if (roll < 30) {
            placeSinglePlant(level, chunkBounds, plantPos, Blocks.SHORT_GRASS.defaultBlockState());
        } else if (roll < 50) {
            placeSinglePlant(level, chunkBounds, plantPos, Blocks.FERN.defaultBlockState());
        } else if (roll < 66) {
            placeSinglePlant(level, chunkBounds, plantPos, Blocks.MOSS_CARPET.defaultBlockState());
        } else if (roll < 72) {
            placeBamboo(level, chunkBounds, plantPos);
        } else if (roll < 76) {
            placeSinglePlant(level, chunkBounds, plantPos, Blocks.JUNGLE_SAPLING.defaultBlockState());
        } else if (roll < 78) {
            placeSinglePlant(level, chunkBounds, plantPos, Blocks.AZALEA.defaultBlockState());
        } else if (roll < 79) {
            placeSinglePlant(level, chunkBounds, plantPos, Blocks.FLOWERING_AZALEA.defaultBlockState());
        } else if (roll < 80) {
            placeSinglePlant(level, chunkBounds, plantPos, Blocks.TORCHFLOWER.defaultBlockState());
        } else if (roll < 81) {
            placePitcherPlant(level, chunkBounds, plantPos);
        } else if (roll < 90) {
            placeBlueMushroom(level, chunkBounds, plantPos);
        }
    }

    private void placeSinglePlant(WorldGenLevel level, BoundingBox chunkBounds, BlockPos pos, BlockState plant) {
        if (chunkBounds.isInside(pos) && !level.isOutsideBuildHeight(pos) && level.getBlockState(pos).isAir()) {
            level.setBlock(pos, plant, 2);
        }
    }

    private void placePitcherPlant(WorldGenLevel level, BoundingBox chunkBounds, BlockPos pos) {
        BlockPos upper = pos.above();
        if (
                chunkBounds.isInside(pos)
                        && chunkBounds.isInside(upper)
                        && !level.isOutsideBuildHeight(pos)
                        && !level.isOutsideBuildHeight(upper)
                        && level.getBlockState(pos).isAir()
                        && level.getBlockState(upper).isAir()
        ) {
            DoublePlantBlock.placeAt(level, Blocks.PITCHER_PLANT.defaultBlockState(), pos, 2);
        }
    }

    private void placeBamboo(WorldGenLevel level, BoundingBox chunkBounds, BlockPos pos)
    {
        if (chunkBounds.isInside(pos) && !level.isOutsideBuildHeight(pos) && level.getBlockState(pos).isAir()) {
            BlockState plant = Blocks.BAMBOO.defaultBlockState();
            level.setBlock(pos, plant, 2);

            Random ran = new Random();

            int roll = ran.nextInt(10);
            BlockPos newPos = pos;
            for(int i = 0 ; i < roll - 1 ; i++) {
                newPos = newPos.above();
                level.setBlock(newPos, plant, 2);
            }
        }
    }

    private void placeBlueMushroom(WorldGenLevel level, BoundingBox chunkBounds, BlockPos pos) {
        if (chunkBounds.isInside(pos) && !level.isOutsideBuildHeight(pos) && level.getBlockState(pos).isAir()) {
            level.setBlock(pos, ModBlocks.BLUE_MUSHROOM.defaultBlockState(), 2);
        }
    }

    private static boolean isPlantableDinoFloor(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.MUD)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.DIRT);
    }

    private void placeTree(WorldGenLevel level, BoundingBox chunkBounds, int x, int z, int floorY, int roofY) {
        int treeChance = 45;    // smaller = more dense
        int treeSuccesses = 2;

        if (hashRange(x, floorY, z, 70, treeChance) >= treeSuccesses) {
            return;
        }

        // Keep trees away from the current chunk edge so crowns do not get clipped.
        if (x - 3 < chunkBounds.minX() || x + 3 > chunkBounds.maxX() || z - 3 < chunkBounds.minZ() || z + 3 > chunkBounds.maxZ()) {
            return;
        }

        int height = 7 + hashRange(x, floorY, z, 71, 8);
        if (floorY + height + 5 >= roofY) {
            return;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = 1; y <= height; y++) {
            pos.set(x, floorY + y, z);
            if (!chunkBounds.isInside(pos) || level.isOutsideBuildHeight(pos) || !level.getBlockState(pos).isAir()) {
                return;
            }
        }

        for (int y = 1; y <= height; y++) {
            pos.set(x, floorY + y, z);
            level.setBlock(pos, Blocks.JUNGLE_LOG.defaultBlockState(), 2);
        }

        BlockState leaves = Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        int topY = floorY + height;

        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) + Math.abs(dy) > 5) {
                        continue;
                    }

                    pos.set(x + dx, topY + dy, z + dz);
                    if (chunkBounds.isInside(pos) && !level.isOutsideBuildHeight(pos) && level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, leaves, 2);
                    }
                }
            }
        }

        placeTreeCanopyLight(level, chunkBounds, x, topY, z);
        placeTreeVines(level, chunkBounds, x, topY, z);
    }

    private void placeTreeCanopyLight(WorldGenLevel level, BoundingBox chunkBounds, int x, int topY, int z) {
        if (hashRange(x, topY, z, 73, 3) != 0) {
            return;
        }

        BlockPos pos = new BlockPos(x, topY, z);
        if (chunkBounds.isInside(pos) && !level.isOutsideBuildHeight(pos)) {
            level.setBlock(pos, Blocks.SHROOMLIGHT.defaultBlockState(), 2);
        }
    }

    private void placeTreeVines(WorldGenLevel level, BoundingBox chunkBounds, int x, int topY, int z) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int flatDistance = Math.abs(dx) + Math.abs(dz);
                if (flatDistance < 3 || flatDistance > 5 || hashRange(x + dx, topY, z + dz, 74, 4) != 0) {
                    continue;
                }

                BlockPos support = new BlockPos(x + dx, topY - 1, z + dz);
                BlockPos start = support.below();
                if (
                        !chunkBounds.isInside(support)
                                || !chunkBounds.isInside(start)
                                || level.isOutsideBuildHeight(support)
                                || level.isOutsideBuildHeight(start)
                                || !level.getBlockState(support).is(Blocks.JUNGLE_LEAVES)
                                || !level.getBlockState(start).isAir()
                ) {
                    continue;
                }

                int length = 1 + hashRange(x + dx, topY, z + dz, 75, 4);
                placeHangingCaveVine(level, chunkBounds, start.getX(), start.getY(), start.getZ(), length, false, 76);
            }
        }
    }

    private void placeHangingCaveVine(
            WorldGenLevel level,
            BoundingBox chunkBounds,
            int x,
            int startY,
            int z,
            int length,
            boolean bright,
            int salt
    ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int placed = 0;

        for (int i = 0; i < length; i++) {
            pos.set(x, startY - i, z);
            if (!chunkBounds.isInside(pos) || level.isOutsideBuildHeight(pos) || !level.getBlockState(pos).isAir()) {
                break;
            }
            placed++;
        }

        for (int i = 0; i < placed; i++) {
            pos.set(x, startY - i, z);
            boolean head = i == placed - 1;
            boolean berries = bright
                    ? (head || hashRange(x, startY - i, z, salt, 3) == 0)
                    : (head && hashRange(x, startY - i, z, salt, 5) == 0);
            BlockState vine = (head ? Blocks.CAVE_VINES : Blocks.CAVE_VINES_PLANT)
                    .defaultBlockState()
                    .setValue(CaveVines.BERRIES, berries);
            level.setBlock(pos, vine, 2);
        }
    }

    private int floorY(int x, int z) {
        double normalizedDistance = Math.sqrt(Math.min(horizontalDistance(x, z), 1.0D));
        double edgeRise = smoothStep(0.84D, 1.0D, normalizedDistance) * 5.0D;

        int base = center.getY() - radiusY / 2 - 1;
        double broadHills = signedValueNoise(x, z, 34, 501) * 2.4D;
        double softVariation = signedValueNoise(x, z, 18, 502) * 1.1D;

        return base + (int) Math.round(broadHills + softVariation + edgeRise);
    }

    private int roofY(int x, int z) {
        double normalizedDistance = Math.sqrt(Math.min(horizontalDistance(x, z), 1.0D));
        double edge = smoothStep(0.78D, 1.0D, normalizedDistance);

        int floorY = floorY(x, z);
        int clearance = 36 - (int) Math.round(edge * 16.0D);
        int domeLift = (int) Math.round((1.0D - edge) * 5.0D);
        int roofVariation = (int) Math.round(signedValueNoise(x, z, 28, 503) * 1.5D);

        return floorY + Math.max(MIN_WALKABLE_HEIGHT, clearance + domeLift + roofVariation);
    }

    private int lakeWaterY() {
        return floorY(entrance.getX(), entrance.getZ()) - 1;
    }

    private boolean isLakeColumn(int x, int z) {
        return lakeDistance(x, z) <= 1.28D;
    }

    private boolean isShaftColumn(int x, int z) {
        int dx = x - entrance.getX();
        int dz = z - entrance.getZ();
        return dx * dx + dz * dz <= SHAFT_WALL_RADIUS * SHAFT_WALL_RADIUS;
    }

    private double lakeDistance(int x, int z) {
        double dx = (double) (x - entrance.getX()) / LAKE_RADIUS_X;
        double dz = (double) (z - entrance.getZ()) / LAKE_RADIUS_Z;
        return dx * dx + dz * dz;
    }

    private double horizontalDistance(int x, int z) {
        double dx = (double) (x - center.getX()) / radiusX;
        double dz = (double) (z - center.getZ()) / radiusZ;
        return dx * dx + dz * dz;
    }

    private BlockState shaftWallState(int y) {
        if (y >= surfaceY - 5) {
            return hashRange(entrance.getX(), y, entrance.getZ(), 200, 3) == 0
                    ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                    : Blocks.STONE.defaultBlockState();
        }

        if (y >= surfaceY - 12) {
            return switch (hashRange(entrance.getX(), y, entrance.getZ(), 201, 5)) {
                case 0 -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                case 1 -> Blocks.ROOTED_DIRT.defaultBlockState();
                default -> Blocks.STONE.defaultBlockState();
            };
        }

        return wallState(entrance.getX(), y, entrance.getZ());
    }

    private static BlockState floorState(int x, int z) {
        return switch (hashRange(x, 0, z, 10, 12)) {
            case 0 -> Blocks.MUD.defaultBlockState();
            case 1 -> Blocks.ROOTED_DIRT.defaultBlockState();
            case 2 -> Blocks.COARSE_DIRT.defaultBlockState();
            case 3, 4 -> Blocks.MOSS_BLOCK.defaultBlockState();
            default -> Blocks.GRASS_BLOCK.defaultBlockState();
        };
    }

    private static BlockState fillerState(int x, int y, int z) {
        return switch (hashRange(x, y, z, 11, 8)) {
            case 0 -> Blocks.MUD.defaultBlockState();
            case 1 -> Blocks.COARSE_DIRT.defaultBlockState();
            case 2 -> Blocks.ROOTED_DIRT.defaultBlockState();
            case 3 -> Blocks.MOSS_BLOCK.defaultBlockState();
            default -> Blocks.DIRT.defaultBlockState();
        };
    }

    private static BlockState ceilingState(int x, int y, int z) {
        return switch (hashRange(x, y, z, 12, 16)) {
            case 0 -> Blocks.DRIPSTONE_BLOCK.defaultBlockState();
            case 1, 2 -> Blocks.MOSS_BLOCK.defaultBlockState();
            case 3 -> Blocks.ROOTED_DIRT.defaultBlockState();
            case 4 -> Blocks.TUFF.defaultBlockState();
            case 5 -> Blocks.CALCITE.defaultBlockState();
            case 6 -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            default -> wallState(x, y, z);
        };
    }

    private static BlockState wallState(int x, int y, int z) {
        return switch (hashRange(x, y, z, 13, 18)) {
            case 0, 1 -> Blocks.MOSS_BLOCK.defaultBlockState();
            case 2 -> Blocks.ROOTED_DIRT.defaultBlockState();
            case 3 -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            case 4, 5 -> Blocks.TUFF.defaultBlockState();
            case 6 -> Blocks.CALCITE.defaultBlockState();
            case 7, 8, 9 -> Blocks.DEEPSLATE.defaultBlockState();
            default -> Blocks.STONE.defaultBlockState();
        };
    }

    private static BlockState lakeBottomState(int x, int y, int z) {
        return switch (hashRange(x, y, z, 121, 9)) {
            case 0 -> Blocks.CLAY.defaultBlockState();
            case 1 -> Blocks.GRAVEL.defaultBlockState();
            default -> Blocks.SAND.defaultBlockState();
        };
    }

    private static void placeIfInBounds(WorldGenLevel level, BoundingBox chunkBounds, BlockPos pos, BlockState state) {
        if (chunkBounds.isInside(pos) && !level.isOutsideBuildHeight(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private static double signedValueNoise(int x, int z, int cellSize, int salt) {
        return valueNoise(x, z, cellSize, salt) * 2.0D - 1.0D;
    }

    private static double valueNoise(int x, int z, int cellSize, int salt) {
        int x0 = Math.floorDiv(x, cellSize);
        int z0 = Math.floorDiv(z, cellSize);
        int x1 = x0 + 1;
        int z1 = z0 + 1;

        double tx = smoothStep(0.0D, 1.0D, (double) Math.floorMod(x, cellSize) / (double) cellSize);
        double tz = smoothStep(0.0D, 1.0D, (double) Math.floorMod(z, cellSize) / (double) cellSize);

        double a = hashUnit(x0, 0, z0, salt);
        double b = hashUnit(x1, 0, z0, salt);
        double c = hashUnit(x0, 0, z1, salt);
        double d = hashUnit(x1, 0, z1, salt);

        return lerp(lerp(a, b, tx), lerp(c, d, tx), tz);
    }

    private static double hashUnit(int x, int y, int z, int salt) {
        return (double) Integer.toUnsignedLong(hash(x, y, z, salt)) / 4294967295.0D;
    }

    private static double smoothStep(double min, double max, double value) {
        if (value <= min) {
            return 0.0D;
        }
        if (value >= max) {
            return 1.0D;
        }

        double t = (value - min) / (max - min);
        return t * t * (3.0D - 2.0D * t);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static int floorToBiomeCell(int coordinate) {
        return Math.floorDiv(coordinate, 4) * 4;
    }

    private static int hashRange(int x, int y, int z, int salt, int bound) {
        return Math.floorMod(hash(x, y, z, salt), bound);
    }

    private static int hash(int x, int y, int z, int salt) {
        int h = x * 73428767 ^ y * 19349663 ^ z * 91227153 ^ salt * 42317861;
        h ^= (h >>> 13);
        h *= 1274126177;
        h ^= (h >>> 16);
        return h;
    }

    private static BoundingBox makeBoundingBox(BlockPos center, BlockPos entrance, int surfaceY, int radiusX, int radiusY, int radiusZ) {
        int minX = Math.min(center.getX() - radiusX - SHELL_PADDING, entrance.getX() - SHAFT_WALL_RADIUS - 1);
        int maxX = Math.max(center.getX() + radiusX + SHELL_PADDING, entrance.getX() + SHAFT_WALL_RADIUS + 1);
        int minY = center.getY() - radiusY - SHELL_PADDING;
        int maxY = Math.max(center.getY() + radiusY + SHELL_PADDING, surfaceY + 1);
        int minZ = Math.min(center.getZ() - radiusZ - SHELL_PADDING, entrance.getZ() - SHAFT_WALL_RADIUS - 1);
        int maxZ = Math.max(center.getZ() + radiusZ + SHELL_PADDING, entrance.getZ() + SHAFT_WALL_RADIUS + 1);
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
