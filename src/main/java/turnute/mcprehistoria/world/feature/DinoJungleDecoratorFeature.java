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
import turnute.mcprehistoria.world.biome.ModBiomes;

public class DinoJungleDecoratorFeature extends Feature<NoneFeatureConfiguration> {
    public DinoJungleDecoratorFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        int chunkStartX = Math.floorDiv(origin.getX(), 16) * 16;
        int chunkStartZ = Math.floorDiv(origin.getZ(), 16) * 16;

        boolean touched = false;

        // First pass: create a biome-sized cave volume, but only inside this chunk.
        for (int x = chunkStartX; x < chunkStartX + 16; x++) {
            for (int z = chunkStartZ; z < chunkStartZ + 16; z++) {
                if (!isDinoColumn(level, x, z)) {
                    continue;
                }

                touched = true;
                generateColumn(level, x, z);
            }
        }

        if (!touched) {
            return false;
        }

        // Second pass: local decoration. Kept separate so the air/floor/roof pass
        // exists first, then plants/lights/stalactites are placed into it.
        for (int x = chunkStartX; x < chunkStartX + 16; x++) {
            for (int z = chunkStartZ; z < chunkStartZ + 16; z++) {
                if (!isDinoColumn(level, x, z)) {
                    continue;
                }

                decorateColumn(level, random, chunkStartX, chunkStartZ, x, z);
            }
        }

        return true;
    }

    private static void generateColumn(WorldGenLevel level, int x, int z) {
        int floorY = DinoJungleMath.floorY(x, z);
        int roofY = DinoJungleMath.roofY(x, z);
        boolean boundary = isHorizontalBiomeBoundary(level, x, z);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = DinoJungleMath.MIN_Y; y <= DinoJungleMath.MAX_Y; y++) {
            pos.set(x, y, z);

            if (level.isOutsideBuildHeight(pos) || !isDinoBiome(level, pos)) {
                continue;
            }

            if (boundary || y <= DinoJungleMath.MIN_Y + 1 || y >= DinoJungleMath.MAX_Y - 1) {
                level.setBlock(pos, Blocks.BEDROCK.defaultBlockState(), 2);
                continue;
            }

            if (y < floorY - 2) {
                level.setBlock(pos, Blocks.BEDROCK.defaultBlockState(), 2);
            } else if (y < floorY) {
                level.setBlock(pos, fillerState(x, y, z), 2);
            } else if (y == floorY) {
                level.setBlock(pos, floorState(x, z), 2);
            } else if (y < roofY) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            } else if (y == roofY) {
                level.setBlock(pos, ceilingState(x, z), 2);
            } else {
                level.setBlock(pos, Blocks.BEDROCK.defaultBlockState(), 2);
            }
        }
    }

    private static void decorateColumn(WorldGenLevel level, RandomSource random, int chunkStartX, int chunkStartZ, int x, int z) {
        int floorY = DinoJungleMath.floorY(x, z);
        int roofY = DinoJungleMath.roofY(x, z);

        placeStalactite(level, x, z, roofY);
        placeCeilingLight(level, x, z, roofY);
        placeWallOrFloorLight(level, x, z, floorY, roofY);
        placePlants(level, random, x, z, floorY);
        placeTree(level, random, chunkStartX, chunkStartZ, x, z, floorY, roofY);
    }

    private static void placeStalactite(WorldGenLevel level, int x, int z, int roofY) {
        if (!DinoJungleMath.chance(x, roofY, z, 31, 9)) {
            return;
        }

        int length = 1 + DinoJungleMath.hashRange(x, z, 32, 5);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int i = 1; i <= length; i++) {
            pos.set(x, roofY - i, z);

            if (level.isOutsideBuildHeight(pos) || !level.getBlockState(pos).isAir()) {
                break;
            }

            level.setBlock(pos, Blocks.DRIPSTONE_BLOCK.defaultBlockState(), 2);
        }
    }

    private static void placeCeilingLight(WorldGenLevel level, int x, int z, int roofY) {
        if (!DinoJungleMath.chance(x, roofY, z, 41, 7)) {
            return;
        }

        BlockPos lightPos = new BlockPos(x, roofY - 1, z);
        BlockPos ceilingPos = new BlockPos(x, roofY, z);

        if (level.isOutsideBuildHeight(lightPos) || !level.getBlockState(lightPos).isAir()) {
            return;
        }

        BlockState glow = DinoJungleMath.chance(x, roofY, z, 42, 2)
                ? Blocks.GLOWSTONE.defaultBlockState()
                : Blocks.SHROOMLIGHT.defaultBlockState();

        level.setBlock(ceilingPos, glow, 2);
        level.setBlock(lightPos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), 2);
    }

    private static void placeWallOrFloorLight(WorldGenLevel level, int x, int z, int floorY, int roofY) {
        if (!DinoJungleMath.chance(x, floorY, z, 51, 28)) {
            return;
        }

        int y = floorY + 2 + DinoJungleMath.hashRange(x, z, 52, Math.max(1, roofY - floorY - 5));
        BlockPos pos = new BlockPos(x, y, z);

        if (level.isOutsideBuildHeight(pos) || !level.getBlockState(pos).isAir()) {
            return;
        }

        level.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 13), 2);
    }

    private static void placePlants(WorldGenLevel level, RandomSource random, int x, int z, int floorY) {
        BlockPos plantPos = new BlockPos(x, floorY + 1, z);

        if (level.isOutsideBuildHeight(plantPos) || !level.getBlockState(plantPos).isAir() || !level.getFluidState(plantPos).isEmpty()) {
            return;
        }

        int roll = DinoJungleMath.hashRange(x, z, 61, 100);
        BlockState plant = null;

        if (roll < 18) {
            plant = Blocks.SHORT_GRASS.defaultBlockState();
        } else if (roll < 27) {
            plant = Blocks.FERN.defaultBlockState();
        } else if (roll < 34) {
            plant = Blocks.BAMBOO.defaultBlockState();
        } else if (roll < 41) {
            plant = Blocks.JUNGLE_SAPLING.defaultBlockState();
        } else if (roll < 48) {
            plant = Blocks.MOSS_CARPET.defaultBlockState();
        }

        if (plant != null) {
            level.setBlock(plantPos, plant, 2);
        }
    }

    private static void placeTree(WorldGenLevel level, RandomSource random, int chunkStartX, int chunkStartZ, int x, int z, int floorY, int roofY) {
        if (!DinoJungleMath.chance(x, floorY, z, 71, 70)) {
            return;
        }

        int height = 5 + DinoJungleMath.hashRange(x, z, 72, 5);

        if (floorY + height + 4 >= roofY) {
            return;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = 1; y <= height; y++) {
            pos.set(x, floorY + y, z);

            if (!level.getBlockState(pos).isAir()) {
                return;
            }
        }

        for (int y = 1; y <= height; y++) {
            pos.set(x, floorY + y, z);
            level.setBlock(pos, Blocks.JUNGLE_LOG.defaultBlockState(), 2);
        }

        BlockState leaves = Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        int topY = floorY + height;

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) + Math.abs(dy) > 4) {
                        continue;
                    }

                    int leafX = x + dx;
                    int leafZ = z + dz;

                    // Do not let decoration write into neighbouring chunks.
                    if (leafX < chunkStartX || leafX >= chunkStartX + 16 || leafZ < chunkStartZ || leafZ >= chunkStartZ + 16) {
                        continue;
                    }

                    pos.set(leafX, topY + dy, leafZ);

                    if (!level.isOutsideBuildHeight(pos) && level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, leaves, 2);
                    }
                }
            }
        }
    }

    private static BlockState floorState(int x, int z) {
        return switch (DinoJungleMath.hashRange(x, z, 10, 7)) {
            case 0 -> Blocks.MUD.defaultBlockState();
            case 1 -> Blocks.DIRT.defaultBlockState();
            case 2 -> Blocks.COARSE_DIRT.defaultBlockState();
            case 3 -> Blocks.MOSS_BLOCK.defaultBlockState();
            default -> Blocks.GRASS_BLOCK.defaultBlockState();
        };
    }

    private static BlockState fillerState(int x, int y, int z) {
        return switch (DinoJungleMath.hashRange(x, z ^ y, 11, 4)) {
            case 0 -> Blocks.MUD.defaultBlockState();
            case 1 -> Blocks.COARSE_DIRT.defaultBlockState();
            default -> Blocks.DIRT.defaultBlockState();
        };
    }

    private static BlockState ceilingState(int x, int z) {
        return DinoJungleMath.hashRange(x, z, 12, 5) == 0
                ? Blocks.DRIPSTONE_BLOCK.defaultBlockState()
                : Blocks.BEDROCK.defaultBlockState();
    }

    private static boolean isHorizontalBiomeBoundary(WorldGenLevel level, int x, int z) {
        return !isDinoColumn(level, x + 1, z)
                || !isDinoColumn(level, x - 1, z)
                || !isDinoColumn(level, x, z + 1)
                || !isDinoColumn(level, x, z - 1);
    }

    private static boolean isDinoColumn(WorldGenLevel level, int x, int z) {
        for (int y = DinoJungleMath.MIN_Y + 2; y <= DinoJungleMath.MAX_Y - 2; y += 12) {
            if (isDinoBiome(level, new BlockPos(x, y, z))) {
                return true;
            }
        }

        return false;
    }

    private static boolean isDinoBiome(WorldGenLevel level, BlockPos pos) {
        return level.getBiome(pos).is(ModBiomes.DINO_JUNGLE);
    }
}
