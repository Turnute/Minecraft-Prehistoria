package turnute.mcprehistoria.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HatchingEggBlock extends Block {
    public static final IntegerProperty HATCH = IntegerProperty.create("hatch", 0, 2);

    private static final VoxelShape SHAPE = Block.box(
            3.0D, 0.0D, 3.0D,
            13.0D, 12.0D, 13.0D
    );

    private final EntityType<? extends Mob> entityType;

    public HatchingEggBlock(EntityType<? extends Mob> entityType, Properties properties) {
        super(properties);
        this.entityType = entityType;
        this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Ralentit un peu l'éclosion. Plus le chiffre est haut, plus c'est lent.
        if (random.nextInt(3) != 0) {
            return;
        }

        int hatch = state.getValue(HATCH);

        if (hatch < 2) {
            level.setBlock(pos, state.setValue(HATCH, hatch + 1), 2);
            level.playSound(
                    null,
                    pos,
                    SoundEvents.TURTLE_EGG_CRACK,
                    SoundSource.BLOCKS,
                    0.8F,
                    0.9F + random.nextFloat() * 0.2F
            );
        } else {
            hatch(level, pos, state);
        }
    }

    private void hatch(ServerLevel level, BlockPos pos, BlockState state) {
        level.playSound(
                null,
                pos,
                SoundEvents.TURTLE_EGG_HATCH,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        level.levelEvent(2001, pos, Block.getId(state));
        level.destroyBlock(pos, false);

        Mob mob = this.entityType.spawn(
                level,
                pos,
                EntitySpawnReason.MOB_SUMMONED
        );

        if (mob == null) {
            return;
        }

        // Important pour ton cas : on force l'entité à être adulte si elle supporte les âges.
        if (mob instanceof AgeableMob ageableMob) {
            ageableMob.setBaby(false);
        }

        mob.setPersistenceRequired();
    }
}