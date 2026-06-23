package turnute.mcprehistoria.entity.aquatic;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class GiantSquidEntity extends Squid {
    private static final float MIN_SIZE_MULTIPLIER = 4.0f;
    private static final float MAX_SIZE_MULTIPLIER = 12.0f;

    private static final double BASE_HEALTH = 10.0;

    private static final String SIZE_MULTIPLIER_KEY = "SizeMultiplier";
    private static final String SIZE_INITIALIZED_KEY = "SizeInitialized";

    public static final int SPAWN_WEIGHT = 2;
    public static final int SPAWN_MIN_GROUP = 1;
    public static final int SPAWN_MAX_GROUP = 1;

    private static final EntityDataAccessor<Float> DATA_SIZE_MULTIPLIER = SynchedEntityData.defineId(
            GiantSquidEntity.class,
            EntityDataSerializers.FLOAT
    );

    private static final EntityDataAccessor<Boolean> DATA_SIZE_INITIALIZED = SynchedEntityData.defineId(
            GiantSquidEntity.class,
            EntityDataSerializers.BOOLEAN
    );

    public GiantSquidEntity(EntityType<? extends Squid> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Squid.createAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SIZE_MULTIPLIER, 1.0f);
        builder.define(DATA_SIZE_INITIALIZED, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && !this.isSizeInitialized()) {
            this.initializeRandomSize();
        }
    }

    private void initializeRandomSize() {
        float size = MIN_SIZE_MULTIPLIER + this.random.nextFloat() * (MAX_SIZE_MULTIPLIER - MIN_SIZE_MULTIPLIER);

        this.setSizeMultiplier(size);
        this.setSizeInitialized(true);

        this.setHealth(this.getMaxHealth());
    }

    public float getSizeMultiplier() {
        return this.getEntityData().get(DATA_SIZE_MULTIPLIER);
    }

    private void setSizeMultiplier(float sizeMultiplier) {
        float clampedSize = Mth.clamp(sizeMultiplier, MIN_SIZE_MULTIPLIER, MAX_SIZE_MULTIPLIER);

        this.getEntityData().set(DATA_SIZE_MULTIPLIER, clampedSize);
        this.refreshDimensions();
        this.applySizeStats();
    }

    private boolean isSizeInitialized() {
        return this.getEntityData().get(DATA_SIZE_INITIALIZED);
    }

    private void setSizeInitialized(boolean initialized) {
        this.getEntityData().set(DATA_SIZE_INITIALIZED, initialized);
    }

    private void applySizeStats() {
        if (this.level().isClientSide()) {
            return;
        }

        AttributeInstance maxHealth = this.getAttribute(Attributes.MAX_HEALTH);

        if (maxHealth != null) {
            double scaledHealth = BASE_HEALTH * this.getSizeMultiplier();
            maxHealth.setBaseValue(scaledHealth);
        }
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale(this.getSizeMultiplier());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);

        if (DATA_SIZE_MULTIPLIER.equals(accessor)) {
            this.refreshDimensions();
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);

        output.putFloat(SIZE_MULTIPLIER_KEY, this.getSizeMultiplier());
        output.putBoolean(SIZE_INITIALIZED_KEY, this.isSizeInitialized());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        float sizeMultiplier = input.getFloatOr(SIZE_MULTIPLIER_KEY, MIN_SIZE_MULTIPLIER);
        boolean sizeInitialized = input.getBooleanOr(SIZE_INITIALIZED_KEY, true);

        this.setSizeMultiplier(sizeMultiplier);
        this.setSizeInitialized(sizeInitialized);

        if (!this.level().isClientSide()) {
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public boolean canBeLeashed() { return false; }

    @Override
    protected float getSoundVolume() {
        return 0.6F;
    }

    public static boolean canSpawn(
            EntityType<GiantSquidEntity> type,
            ServerLevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random
    ) {
        boolean inWater = level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos.above()).is(FluidTags.WATER);

        if (!inWater) {
            return false;
        }

        int seaLevel = level.getSeaLevel();
        int oceanFloorY = level.getHeight(
                Heightmap.Types.OCEAN_FLOOR,
                pos.getX(),
                pos.getZ()
        );

        boolean actuallyDeepOcean = seaLevel - oceanFloorY >= 30;
        boolean notNearSurface = pos.getY() <= seaLevel - 10;

        return actuallyDeepOcean && notNearSurface;
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            SpawnGroupData spawnGroupData
    ) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);

        if(spawnReason == EntitySpawnReason.NATURAL || spawnReason == EntitySpawnReason.CHUNK_GENERATION) {
            spawnCompanions(level);
        }

        return data;
    }

    private void spawnCompanions(ServerLevelAccessor level) {
        RandomSource random = this.getRandom();

        int nautilusCount = 10 + random.nextInt(15);
        int babySquidCount = 8 + random.nextInt(8);

        for (int i = 0; i < nautilusCount; i++) {
            spawnAround(level, EntityType.NAUTILUS, false);
        }

        for (int i = 0; i < babySquidCount; i++) {
            spawnAround(level, EntityType.SQUID, true);
        }
    }

    private <T extends Entity> void spawnAround(
            ServerLevelAccessor level,
            EntityType<T> entityType,
            boolean makeBaby
    ) {
        RandomSource random = this.getRandom();

        for (int attempt = 0 ; attempt < 12 ; attempt++) {
            double dx = (random.nextDouble() - 0.5D) * 12.0D;
            double dy = (random.nextDouble() - 0.5D) * 6.0D;
            double dz = (random.nextDouble() - 0.5D) * 12.0D;

            BlockPos pos = BlockPos.containing(this.getX() + dx, this.getY() + dy, this.getZ() + dz);

            boolean validWater = level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos.above()).is(FluidTags.WATER);

            if(!validWater) {continue;}

            T entity = entityType.spawn(
                    level.getLevel(),
                    pos,
                    EntitySpawnReason.MOB_SUMMONED
            );

            if (entity instanceof AgeableMob ageable && makeBaby) {
                ageable.setBaby(true);
            }

            return;
        }
    }
}