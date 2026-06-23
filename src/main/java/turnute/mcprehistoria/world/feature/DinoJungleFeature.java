package turnute.mcprehistoria.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

public class DinoJungleFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_Y = - 50;
    private static final int MAX_Y = 30;

    public DinoJungleFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        if (origin.getY() < MIN_Y || origin.getY() > MAX_Y) {
            return false;
        }

        int radiusX = 16 + random.nextInt(4); // 16 à 19
        int radiusY = 9 + random.nextInt(4);  // 9 à 12
        int radiusZ = 16 + random.nextInt(4); // 16 à 19

        int chunkStartX = Math.floorDiv(origin.getX(), 16) * 16;
        int chunkStartZ = Math.floorDiv(origin.getZ(), 16) * 16;

        BlockPos center = new BlockPos(
                chunkStartX + 8,
                origin.getY(),
                chunkStartZ + 8
        );

        if (center.getY() - radiusY - 4 < level.getMinY()) {
            return false;
        }

        if (center.getY() + radiusY + 35 > level.getMaxY()) {
            return false;
        }

        List<BlockPos> floorSpots = new ArrayList<>();

        carveCavernWithBedrockShell(level, center, radiusX, radiusY, radiusZ);
        buildJungleFloor(level, center, radiusX, radiusY, radiusZ, random, floorSpots);
        placeWallLights(level, center, radiusX, radiusY, radiusZ, random);
        placeVegetation(level, random, floorSpots);
        carveEntrance(level, center, radiusY, random);

        return true;
    }

    private static void carveCavernWithBedrockShell(WorldGenLevel level, BlockPos center, int radiusX, int radiusY, int radiusZ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -radiusX - 3; dx <= radiusX + 3; dx++) {
            for (int dy = -radiusY - 3; dy <= radiusY + 3; dy++) {
                for (int dz = -radiusZ - 3; dz <= radiusZ + 3; dz++) {
                    double distance = ellipsoidDistance(dx, dy, dz, radiusX, radiusY, radiusZ);

                    pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);

                    if (level.isOutsideBuildHeight(pos)) {
                        continue;
                    }

                    if (distance <= 1.0) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    } else if (distance <= 1.18) {
                        level.setBlock(pos, Blocks.BEDROCK.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void buildJungleFloor(WorldGenLevel level, BlockPos center, int radiusX, int radiusY, int radiusZ, RandomSource random, List<BlockPos> floorSpots) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -radiusX + 2; dx <= radiusX -2; dx++) {
            for (int dz = -radiusZ +2; dz <= radiusZ -2; dz++) {
                int floorY = Integer.MIN_VALUE;

                for (int dy = -radiusY; dy <= radiusY; dy++) {
                    double distance = ellipsoidDistance(dx, dy, dz, radiusX, radiusY, radiusZ);

                    if (distance <= 0.92) {
                        floorY = center.getY() + dy;
                        break;
                    }
                }

                if (floorY == Integer.MIN_VALUE) {
                    continue;
                }

                pos.set(center.getX() + dx, floorY, center.getZ() + dz);

                BlockState floorState = switch (random.nextInt(5)) {
                    case 0 -> Blocks.MOSS_BLOCK.defaultBlockState();
                    case 1 -> Blocks.MUD.defaultBlockState();
                    case 2 -> Blocks.ROOTED_DIRT.defaultBlockState();
                    default -> Blocks.GRASS_BLOCK.defaultBlockState();
                };

                level.setBlock(pos, floorState, 2);
                level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(pos.above(2), Blocks.AIR.defaultBlockState(), 2);

                double flatDistance = ((double) dx * dx) / ((radiusX - 5.0) * (radiusX - 5.0))
                        + ((double) dz * dz) / ((radiusZ - 5.0) * (radiusZ - 5.0));

                if (flatDistance <= 1.0) {
                    floorSpots.add(pos.immutable());
                }
            }
        }
    }

    private static void placeWallLights(
            WorldGenLevel level,
            BlockPos center,
            int radiusX,
            int radiusY,
            int radiusZ,
            RandomSource random
    ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dy = -radiusY; dy <= radiusY; dy++) {
                for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                    double distance = ellipsoidDistance(dx, dy, dz, radiusX, radiusY, radiusZ);

                    if (distance > 0.86 && distance < 1.0 && random.nextInt(90) == 0) {
                        pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);

                        if (level.isOutsideBuildHeight(pos)) {
                            continue;
                        }

                        if (random.nextBoolean()) {
                            level.setBlock(pos, Blocks.GLOWSTONE.defaultBlockState(), 2);
                        } else {
                            BlockState light = Blocks.LIGHT.defaultBlockState()
                                    .setValue(LightBlock.LEVEL, 13);
                            level.setBlock(pos, light, 2);
                        }
                    }
                }
            }
        }
    }

    private static void placeVegetation(
            WorldGenLevel level,
            RandomSource random,
            List<BlockPos> floorSpots
    ) {
        if (floorSpots.isEmpty()) {
            return;
        }

        int treeCount = Math.min(8, floorSpots.size() / 80);

        for (int i = 0; i < treeCount; i++) {
            BlockPos floor = floorSpots.get(random.nextInt(floorSpots.size()));
            placeSimpleJungleTree(level, floor.above(), random);
        }

        int plantCount = Math.min(250, floorSpots.size() / 3);

        for (int i = 0; i < plantCount; i++) {
            BlockPos floor = floorSpots.get(random.nextInt(floorSpots.size()));
            BlockPos plantPos = floor.above();

            if (!level.getBlockState(plantPos).isAir()) {
                continue;
            }

            BlockState plant = switch (random.nextInt(8)) {
                case 0 -> Blocks.FERN.defaultBlockState();
                case 1 -> Blocks.LARGE_FERN.defaultBlockState();
                case 2 -> Blocks.BAMBOO.defaultBlockState();
                case 3 -> Blocks.JUNGLE_SAPLING.defaultBlockState();
                default -> Blocks.SHORT_GRASS.defaultBlockState();
            };

            level.setBlock(plantPos, plant, 2);
        }
    }

    private static void placeSimpleJungleTree(WorldGenLevel level, BlockPos base, RandomSource random) {
        int height = 5 + random.nextInt(5);

        for (int y = 0; y < height; y++) {
            BlockPos logPos = base.above(y);

            if (level.isOutsideBuildHeight(logPos)) {
                return;
            }

            level.setBlock(logPos, Blocks.JUNGLE_LOG.defaultBlockState(), 2);
        }

        BlockState leaves = Blocks.JUNGLE_LEAVES.defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true);

        BlockPos top = base.above(height);

        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) + Math.abs(dy) > 5) {
                        continue;
                    }

                    BlockPos leafPos = top.offset(dx, dy, dz);

                    if (!level.isOutsideBuildHeight(leafPos) && level.getBlockState(leafPos).isAir()) {
                        level.setBlock(leafPos, leaves, 2);
                    }
                }
            }
        }
    }

    private static void carveEntrance(
            WorldGenLevel level,
            BlockPos center,
            int radiusY,
            RandomSource random
    ) {
        int shaftRadius = 2;
        int startY = center.getY() + radiusY - 1;
        int endY = 30;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = startY; y <= endY; y++) {
            for (int dx = -shaftRadius; dx <= shaftRadius; dx++) {
                for (int dz = -shaftRadius; dz <= shaftRadius; dz++) {
                    if (dx * dx + dz * dz > shaftRadius * shaftRadius) {
                        continue;
                    }

                    pos.set(center.getX() + dx, y, center.getZ() + dz);

                    if (!level.isOutsideBuildHeight(pos)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        BlockPos entranceFloor = center.above(radiusY - 2);
        level.setBlock(entranceFloor, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
    }

    private static double ellipsoidDistance(
            int dx,
            int dy,
            int dz,
            int radiusX,
            int radiusY,
            int radiusZ
    ) {
        return ((double) dx * dx) / (radiusX * radiusX)
                + ((double) dy * dy) / (radiusY * radiusY)
                + ((double) dz * dz) / (radiusZ * radiusZ);
    }
}
