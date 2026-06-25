package turnute.mcprehistoria.world.structure;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import turnute.mcprehistoria.MinecraftPrehistoria;

public final class ModStructures {
    private ModStructures() {}

    public static final StructureType<DinoJungleCavernStructure> DINO_JUNGLE_CAVERN =
            () -> DinoJungleCavernStructure.CODEC;

    public static final StructurePieceType DINO_JUNGLE_CAVERN_PIECE = DinoJungleCavernPiece::new;

    public static void register() {
        Registry.register(
                BuiltInRegistries.STRUCTURE_TYPE,
                MinecraftPrehistoria.id("dino_jungle_cavern"),
                DINO_JUNGLE_CAVERN
        );

        Registry.register(
                BuiltInRegistries.STRUCTURE_PIECE,
                MinecraftPrehistoria.id("dino_jungle_cavern_piece"),
                DINO_JUNGLE_CAVERN_PIECE
        );
    }
}
