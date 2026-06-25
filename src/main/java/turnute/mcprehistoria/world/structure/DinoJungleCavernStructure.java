package turnute.mcprehistoria.world.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class DinoJungleCavernStructure extends Structure {
    public static final MapCodec<DinoJungleCavernStructure> CODEC = simpleCodec(DinoJungleCavernStructure::new);

    private static final int MIN_SURFACE_ABOVE_SEA = 10;
    private static final int MIN_ROOF_CLEARANCE = 44;
    private static final int MIN_BOTTOM_PADDING = 10;

    public DinoJungleCavernStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        ChunkGenerator chunkGenerator = context.chunkGenerator();
        int surfaceY = chunkGenerator.getFirstOccupiedHeight(
                centerX,
                centerZ,
                Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(),
                context.randomState()
        );

        if (surfaceY < chunkGenerator.getSeaLevel() + MIN_SURFACE_ABOVE_SEA) {
            return Optional.empty();
        }

        RandomSource random = RandomSource.create(mixSeed(context.seed(), chunkPos.x(), chunkPos.z()));

        int radiusX = 58 + random.nextInt(15); // 58..72 blocks: wider overworld slice.
        int radiusY = 31 + random.nextInt(6);  // 31..36 blocks high enough for trees, not a void sphere.
        int radiusZ = 54 + random.nextInt(17); // 54..70 blocks.

        int levelMinY = context.heightAccessor().getMinY();
        int wantedDepth = 96 + random.nextInt(25);
        int centerY = Math.min(surfaceY - wantedDepth, -38 - random.nextInt(14));
        centerY = Math.max(centerY, levelMinY + radiusY / 2 + MIN_BOTTOM_PADDING);

        int estimatedRoofY = centerY + radiusY + 4;
        if (estimatedRoofY + MIN_ROOF_CLEARANCE >= surfaceY) {
            return Optional.empty();
        }

        if (centerY - radiusY / 2 - MIN_BOTTOM_PADDING < levelMinY) {
            return Optional.empty();
        }

        int entranceX = centerX + random.nextInt(radiusX / 2 + 1) - radiusX / 4;
        int entranceZ = centerZ + random.nextInt(radiusZ / 2 + 1) - radiusZ / 4;
        BlockPos entrance = new BlockPos(entranceX, surfaceY, entranceZ);
        BlockPos center = new BlockPos(centerX, centerY, centerZ);

        return Optional.of(new GenerationStub(center, pieces -> pieces.addPiece(
                new DinoJungleCavernPiece(center, entrance, surfaceY, radiusX, radiusY, radiusZ)
        )));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.DINO_JUNGLE_CAVERN;
    }

    private static long mixSeed(long worldSeed, int chunkX, int chunkZ) {
        long seed = worldSeed;
        seed ^= (long) chunkX * 341873128712L;
        seed ^= (long) chunkZ * 132897987541L;
        seed ^= 0x51D10D1C0A7E5L;
        return seed;
    }
}
