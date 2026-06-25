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
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;

public class GiantSquidEntity extends Squid {
    private static final float MIN_SIZE_MULTIPLIER = 4.0f;
    private static final float MAX_SIZE_MULTIPLIER = 12.0f;

    private static final double BASE_HEALTH = 10.0;

    private static final String SIZE_MULTIPLIER_KEY = "SizeMultiplier";
    private static final String SIZE_INITIALIZED_KEY = "SizeInitialized";

    private static final int SPAWN_CLEARANCE_RADIUS = 5;
    private static final int SPAWN_CLEARANCE_UP = 7;
    private static final int SPAWN_CLEARANCE_DOWN = 3;

    public static final boolean DEBUG_NATURAL_SPAWNS = true;

    public static final int SPAWN_WEIGHT = DEBUG_NATURAL_SPAWNS ? 25 : 4; // rareté spawn
    public static final int SPAWN_MIN_GROUP = 1;
    public static final int SPAWN_MAX_GROUP = 1;

    private static final double MAX_WATER_SPEED = 0.285D;

    private static final double STUCK_FORWARD_IMPULSE_STRENGTH = 0.01D;
    private static final int STUCK_IMPULSE_INTERVAL = 80;
    private static final int SPAWN_SWIM_START_TICKS = 20;

    private boolean initialSwimDirectionSet = false;

    private static final float PITCH_MULTIPLIER = 0.45F;                // plus bas = plus grave
    private static final float BASE_GIANT_SOUND_VOLUME = 1.35F;
    private static final float SIZE_SOUND_VOLUME_BONUS = 0.12F;

    private static final int EXTRA_INK_CLOUDS = 50;                      // plus haut = + paquets encre
    private static final int EXTRA_INK_PARTICLES_PER_CLOUD = 80;
    private static final double EXTRA_INK_SPEED = 0.08D;

    private static final int BASE_NAUTILUS_COUNT = 30;
    private static final int ADDITIONAL_NAUTILUS_COUNT = 30;
    private static final int BASE_BABY_COUNT = 20;
    private static final int ADDITIONAL_BABY_COUNT = 30;

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
        builder.define(DATA_SIZE_MULTIPLIER, MIN_SIZE_MULTIPLIER);
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
        return BASE_GIANT_SOUND_VOLUME + this.getSizeMultiplier() * SIZE_SOUND_VOLUME_BONUS;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * PITCH_MULTIPLIER;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            initializeSwimDirectionAfterSpawn();
            nudgeIfBarelyMoving();
        }

        updateVisualAnimation();
    }

    private void initializeSwimDirectionAfterSpawn() {
        if (this.initialSwimDirectionSet) {
            return;
        }

        if (!this.isInWater()) {
            return;
        }

        if (this.tickCount < SPAWN_SWIM_START_TICKS) {
            return;
        }

        this.initialSwimDirectionSet = true;

        float yaw = this.random.nextFloat() * 360.0F;

        this.setYRot(yaw);
        this.yRotO = yaw;

        Vec3 forward = Vec3.directionFromRotation(0.0F, yaw).scale(MAX_WATER_SPEED * 0.55D);

        this.setDeltaMovement(
                forward.x,
                (this.random.nextDouble() - 0.5D) * 0.015D,
                forward.z
        );
    }

    private void nudgeIfBarelyMoving() {
        if (!this.isInWater()) {
            return;
        }

        if (this.tickCount < SPAWN_SWIM_START_TICKS + STUCK_IMPULSE_INTERVAL) {
            return;
        }

        if (this.tickCount % STUCK_IMPULSE_INTERVAL != 0) {
            return;
        }

        Vec3 velocity = this.getDeltaMovement();

        if (velocity.horizontalDistanceSqr() > 0.0004D) {
            return;
        }

        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot());

        if (forward.horizontalDistanceSqr() <= 1.0E-7D) {
            float fallbackYaw = this.random.nextFloat() * 360.0F;
            this.setYRot(fallbackYaw);
            this.yRotO = fallbackYaw;
            forward = Vec3.directionFromRotation(0.0F, fallbackYaw);
        }

        Vec3 impulse = forward.normalize().scale(STUCK_FORWARD_IMPULSE_STRENGTH);

        double verticalDrift = Mth.clamp(
                velocity.y * 0.35D + (this.random.nextDouble() - 0.5D) * 0.01D,
                -0.015D,
                0.015D
        );

        this.setDeltaMovement(
                velocity.x + impulse.x,
                verticalDrift,
                velocity.z + impulse.z
        );
    }

    private static final float VISUAL_ANIMATION_LERP = 0.10F;

    private boolean visualAnimationInitialized = false;
    private float visualTentacleAngle;
    private float visualXBodyRot;
    private float visualZBodyRot;

    private void updateVisualAnimation() {
        if (!visualAnimationInitialized) {
            visualTentacleAngle = this.tentacleAngle;
            visualXBodyRot = this.xBodyRot;
            visualZBodyRot = this.zBodyRot;
            visualAnimationInitialized = true;
            return;
        }

        visualTentacleAngle = Mth.lerp(VISUAL_ANIMATION_LERP, visualTentacleAngle, this.tentacleAngle);
        visualXBodyRot = Mth.lerp(VISUAL_ANIMATION_LERP, visualXBodyRot, this.xBodyRot);
        visualZBodyRot = Mth.lerp(VISUAL_ANIMATION_LERP, visualZBodyRot, this.zBodyRot);
    }

    public float getVisualTentacleAngle() {
        return visualTentacleAngle;
    }

    public float getVisualXBodyRot() {
        return visualXBodyRot;
    }

    public float getVisualZBodyRot() {
        return visualZBodyRot;
    }

    @Override
    public void travel(Vec3 movementInput) {
        super.travel(movementInput);

        if (this.isInWater()) {
            Vec3 velocity = this.getDeltaMovement();

            if (velocity.lengthSqr() > MAX_WATER_SPEED * MAX_WATER_SPEED) {
                this.setDeltaMovement(velocity.normalize().scale(MAX_WATER_SPEED));
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        boolean hurt = super.hurtServer(level, damageSource, amount);

        if (hurt && damageSource.getEntity() instanceof LivingEntity) {
            this.spawnExtraInk(level);
        }

        return hurt;
    }

    private void spawnExtraInk(ServerLevel level) {
        ParticleOptions inkParticle = this.getInkParticle();
        RandomSource random = this.getRandom();

        float sizeFactor = Mth.clamp(this.getSizeMultiplier() / 6.0F, 1.0F, 2.2F);

        int cloudCount = Math.round(EXTRA_INK_CLOUDS * sizeFactor);
        int particlesPerCloud = Math.round(EXTRA_INK_PARTICLES_PER_CLOUD * sizeFactor);

        double width = this.getBbWidth();
        double height = this.getBbHeight();

        for (int i = 0; i < cloudCount; i++) {
            double x = this.getX() + (random.nextDouble() - 0.5D) * width * 0.9D;
            double y = this.getY() + height * (0.25D + random.nextDouble() * 0.55D);
            double z = this.getZ() + (random.nextDouble() - 0.5D) * width * 0.9D;

            double spreadX = width * 0.35D;
            double spreadY = height * 0.18D;
            double spreadZ = width * 0.35D;

            level.sendParticles(
                    inkParticle,
                    x,
                    y,
                    z,
                    particlesPerCloud,
                    spreadX,
                    spreadY,
                    spreadZ,
                    EXTRA_INK_SPEED
            );
        }
    }

    // =============
    // SPAWNING

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

        int requiredDepth = DEBUG_NATURAL_SPAWNS ? 18 : 30;
        int requiredSurfaceDistance = DEBUG_NATURAL_SPAWNS ? 5 : 10;

        boolean actuallyDeepOcean = seaLevel - oceanFloorY >= requiredDepth;
        boolean notNearSurface = pos.getY() <= seaLevel - requiredSurfaceDistance;
        boolean notScrapingFloor = pos.getY() >= oceanFloorY + SPAWN_CLEARANCE_DOWN + 1;

        return actuallyDeepOcean && notNearSurface && notScrapingFloor && hasWaterClearance(level, pos);
    }

    private static boolean hasWaterClearance(ServerLevelAccessor level, BlockPos center) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -SPAWN_CLEARANCE_RADIUS; x <= SPAWN_CLEARANCE_RADIUS; x++) {
            for (int z = -SPAWN_CLEARANCE_RADIUS; z <= SPAWN_CLEARANCE_RADIUS; z++) {
                if (x * x + z * z > SPAWN_CLEARANCE_RADIUS * SPAWN_CLEARANCE_RADIUS) {
                    continue;
                }

                for (int y = -SPAWN_CLEARANCE_DOWN; y <= SPAWN_CLEARANCE_UP; y++) {
                    mutable.set(center.getX() + x, center.getY() + y, center.getZ() + z);

                    if (!level.getFluidState(mutable).is(FluidTags.WATER)) {
                        return false;
                    }

                    if (!level.getBlockState(mutable).getCollisionShape(level, mutable).isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            SpawnGroupData spawnGroupData
    ) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);

        if (!this.isSizeInitialized()) {
            this.initializeRandomSize();
        }

        if(spawnReason == EntitySpawnReason.NATURAL || spawnReason == EntitySpawnReason.CHUNK_GENERATION) {
            spawnCompanions(level);
        }

        this.setPersistenceRequired();

        return data;
    }

    private void spawnCompanions(ServerLevelAccessor level) {
        RandomSource random = this.getRandom();

        int nautilusCount = BASE_NAUTILUS_COUNT + random.nextInt(ADDITIONAL_NAUTILUS_COUNT);
        int babySquidCount = BASE_BABY_COUNT + random.nextInt(ADDITIONAL_BABY_COUNT);

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

    @Override
    public boolean removeWhenFarAway(double distanceSquared) {
        return false;
    }
}