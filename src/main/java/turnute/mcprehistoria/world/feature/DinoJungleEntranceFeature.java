package turnute.mcprehistoria.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import turnute.mcprehistoria.world.biome.ModBiomes;

public class DinoJungleEntranceFeature extends Feature<NoneFeatureConfiguration> {
    public DinoJungleEntranceFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        int chunkStartX = Math.floorDiv(origin.getX(), 16) * 16;
        int chunkStartZ = Math.floorDiv(origin.getZ(), 16) * 16;
        int centerX = chunkStartX + 8;
        int centerZ = chunkStartZ + 8;

        if (!isDinoColumn(level, centerX, centerZ)) {
            return false;
        }

        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, centerX, centerZ);
        int roofY = DinoJungleMath.roofY(centerX, centerZ);
        int floorY = DinoJungleMath.floorY(centerX, centerZ);

        if (surfaceY <= DinoJungleMath.MAX_Y + 12) {
            return false;
        }

        carveShaft(level, centerX, centerZ, floorY + 2, roofY + 12);
        decorateShaft(level, centerX, centerZ, roofY + 2, surfaceY + 2);
        placeEntranceLake(level, random, chunkStartX, chunkStartZ, centerX, centerZ, floorY);

        return true;
    }

    private static void carveShaft(WorldGenLevel level, int centerX, int centerZ, int minY, int maxY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = minY; y <= maxY; y++) {
            int radius = y < DinoJungleMath.MAX_Y + 6 ? 4 : 3;

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz > radius * radius) {
                        continue;
                    }

                    pos.set(centerX + dx, y, centerZ + dz);

                    if (!level.isOutsideBuildHeight(pos)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void decorateShaft(WorldGenLevel level, int centerX, int centerZ, int minY, int maxY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = minY; y <= maxY; y++) {
            if (DinoJungleMath.chance(centerX, y, centerZ, 101, 19)) {
                int side = DinoJungleMath.hashRange(centerX, y + centerZ, 102, 4);
                int dx = side == 0 ? 3 : side == 1 ? -3 : 0;
                int dz = side == 2 ? 3 : side == 3 ? -3 : 0;

                pos.set(centerX + dx, y, centerZ + dz);

                if (!level.isOutsideBuildHeight(pos)) {
                    level.setBlock(pos, Blocks.GLOWSTONE.defaultBlockState(), 2);
                }
            }
        }
    }

    private static void placeEntranceLake(
            WorldGenLevel level,
            RandomSource random,
            int chunkStartX,
            int chunkStartZ,
            int centerX,
            int centerZ,
            int floorY
    ) {
        int radiusX = 5 + random.nextInt(3);
        int radiusZ = 5 + random.nextInt(3);
        int waterY = floorY;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = centerX - radiusX - 2; x <= centerX + radiusX + 2; x++) {
            for (int z = centerZ - radiusZ - 2; z <= centerZ + radiusZ + 2; z++) {
                if (x < chunkStartX || x >= chunkStartX + 16 || z < chunkStartZ || z >= chunkStartZ + 16) {
                    continue;
                }

                double dx = (double) (x - centerX) / radiusX;
                double dz = (double) (z - centerZ) / radiusZ;
                double edgeNoise = DinoJungleMath.hashRange(x, z, 111, 100) / 500.0D;
                double distance = dx * dx + dz * dz + edgeNoise;

                int localFloor = DinoJungleMath.floorY(x, z);

                if (distance <= 1.0D) {
                    for (int y = localFloor - 2; y <= waterY; y++) {
                        pos.set(x, y, z);

                        if (level.isOutsideBuildHeight(pos)) {
                            continue;
                        }

                        if (y <= localFloor) {
                            level.setBlock(pos, lakeBottomState(x, y, z), 2);
                        } else {
                            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
                        }
                    }
                } else if (distance <= 1.40D) {
                    pos.set(x, localFloor, z);

                    if (!level.isOutsideBuildHeight(pos)) {
                        level.setBlock(pos, lakeShoreState(x, z), 2);
                    }
                }
            }
        }

        BlockPos lightPos = new BlockPos(centerX, waterY + 1, centerZ);
        if (!level.isOutsideBuildHeight(lightPos)) {
            level.setBlock(lightPos, Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }
    }

    private static BlockState lakeBottomState(int x, int y, int z) {
        return switch (DinoJungleMath.hashRange(x, z ^ y, 121, 6)) {
            case 0 -> Blocks.CLAY.defaultBlockState();
            case 1 -> Blocks.SHROOMLIGHT.defaultBlockState();
            default -> Blocks.SAND.defaultBlockState();
        };
    }

    private static BlockState lakeShoreState(int x, int z) {
        return switch (DinoJungleMath.hashRange(x, z, 122, 5)) {
            case 0 -> Blocks.MUD.defaultBlockState();
            case 1 -> Blocks.CLAY.defaultBlockState();
            default -> Blocks.SAND.defaultBlockState();
        };
    }

    private static boolean isDinoColumn(WorldGenLevel level, int x, int z) {
        for (int y = DinoJungleMath.MIN_Y + 2; y <= DinoJungleMath.MAX_Y - 2; y += 12) {
            if (level.getBiome(new BlockPos(x, y, z)).is(ModBiomes.DINO_JUNGLE)) {
                return true;
            }
        }

        return false;
    }
}
