package turnute.mcprehistoria.entity.aquatic;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class GiantSquidEntity extends Squid {
    private static final float MIN_SIZE_MULTIPLIER = 4.0f;
    private static final float MAX_SIZE_MULTIPLIER = 12.0f;

    private static final double BASE_HEALTH = 10.0;

    private static final String SIZE_MULTIPLIER_KEY = "SizeMultiplier";
    private static final String SIZE_INITIALIZED_KEY = "SizeInitialized";

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
}