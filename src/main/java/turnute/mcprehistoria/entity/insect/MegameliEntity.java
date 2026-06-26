package turnute.mcprehistoria.entity.insect;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import turnute.mcprehistoria.entity.aquatic.GiantSquidEntity;

import java.util.UUID;

public class MegameliEntity extends Bee implements GeoEntity {
    private static final RawAnimation FLY_ANIMATION = RawAnimation.begin().thenLoop("megameli_fly");

    private static final double RIDDEN_VERTICAL_PITCH_STRENGTH = 0.65D;
    private static final double RIDDEN_IDLE_VERTICAL_INPUT = 0.0D;
    private static final float RIDDEN_VERTICAL_DEADZONE_DEGREES = 6.0F;

    private static final float PITCH_MULTIPLIER = 0.45F;

    private static final String TAMED_KEY = "Tamed";
    private static final String OWNER_UUID_KEY = "OwnerUUID";

    private static final int TAMING_CHANCE = 3;

    private static final EntityDataAccessor<Boolean> DATA_TAMED = SynchedEntityData.defineId(
            MegameliEntity.class,
            EntityDataSerializers.BOOLEAN
    );

    private UUID ownerUUID;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public MegameliEntity(EntityType<? extends MegameliEntity> entityType, Level level) {
        super(entityType, level);
    }


    public static AttributeSupplier.Builder createAttributes() {
        return Bee.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.FLYING_SPEED, 0.75D);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(test -> test.setAndContinue(FLY_ANIMATION)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * PITCH_MULTIPLIER;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (isAncientFlower(stack) && !this.isTamed()) {
            if (!this.level().isClientSide()) {
                tryTameWithAncientFlower(player, stack);
            }

            return InteractionResult.SUCCESS;
        }

        if (this.isTamed()) {
            if (!this.level().isClientSide()) {
                if (this.isOwnedBy(player)) {
                    player.startRiding(this);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity passenger = this.getFirstPassenger();
        return passenger instanceof LivingEntity ? (LivingEntity) passenger : null;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty()
                && passenger instanceof Player player
                && this.isTamed()
                && this.isOwnedBy(player);
    }

    @Override
    protected void tickRidden(Player player, Vec3 movementInput) {
        super.tickRidden(player, movementInput);

        this.setYRot(player.getYRot());
        this.setXRot(player.getXRot() * 0.5F);

        this.yRotO = this.getYRot();
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 movementInput) {
        float sideways = player.xxa * 0.5F;
        float forward = player.zza;

        if (forward <= 0.0F) {
            forward *= 0.25F;
        }

        double upward = getPitchBasedVerticalInput(player, forward);

        return new Vec3(sideways, upward, forward);
    }

    private double getPitchBasedVerticalInput(Player player, float forwardInput) {
        if (forwardInput <= 0.0F) {
            return RIDDEN_IDLE_VERTICAL_INPUT;
        }

        float pitch = player.getXRot();

        if (Math.abs(pitch) < RIDDEN_VERTICAL_DEADZONE_DEGREES) {
            return RIDDEN_IDLE_VERTICAL_INPUT;
        }

        double pitchRadians = Math.toRadians(pitch);

        return -Math.sin(pitchRadians) * RIDDEN_VERTICAL_PITCH_STRENGTH;
    }

    @Override
    public boolean isNoGravity() {
        return this.isVehicle() || super.isNoGravity();
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float) this.getAttributeValue(Attributes.FLYING_SPEED) * 0.45F;
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity passenger) {
        Vec3 basePos = super.getPassengerRidingPosition(passenger);
        Vec3 backwardOffset = Vec3.directionFromRotation(0.0F, this.getYRot()).scale(-0.55D);

        return basePos.add(backwardOffset).add(0.0D, 0.1D, 0.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TAMED, false);
    }

    public boolean isTamed() {
        return this.getEntityData().get(DATA_TAMED);
    }

    private void setTamed(boolean tamed) {
        this.getEntityData().set(DATA_TAMED, tamed);

        if (tamed) {
            this.setPersistenceRequired();
        }
    }

    private boolean isOwnedBy(Player player) {
        return this.ownerUUID != null && this.ownerUUID.equals(player.getUUID());
    }

    private void tame(Player player) {
        this.ownerUUID = player.getUUID();
        this.setTamed(true);
        this.setPersistenceRequired();
    }

    private boolean isAncientFlower(ItemStack stack) {
        return stack.is(Items.TORCHFLOWER) || stack.is(Items.PITCHER_PLANT);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);

        output.putBoolean(TAMED_KEY, this.isTamed());

        if (this.ownerUUID != null) {
            output.putString(OWNER_UUID_KEY, this.ownerUUID.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        this.setTamed(input.getBooleanOr(TAMED_KEY, false));

        String ownerUuidString = input.getStringOr(OWNER_UUID_KEY, "");

        if (!ownerUuidString.isEmpty()) {
            try {
                this.ownerUUID = UUID.fromString(ownerUuidString);
            } catch (IllegalArgumentException ignored) {
                this.ownerUUID = null;
            }
        }
    }

    private void tryTameWithAncientFlower(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        if (this.random.nextInt(TAMING_CHANCE) == 0) {
            this.tame(player);
            this.level().broadcastEntityEvent(this, (byte) 7);
            this.playSound(SoundEvents.BEEHIVE_ENTER, 1.0F, 1.2F);
        } else {
            this.level().broadcastEntityEvent(this, (byte) 6);
            this.playSound(SoundEvents.BEE_HURT, 0.5F, 1.4F);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 7) {
            spawnTamingParticles(true);
        } else if (id == 6) {
            spawnTamingParticles(false);
        } else {
            super.handleEntityEvent(id);
        }
    }

    private void spawnTamingParticles(boolean success) {
        for (int i = 0; i < 7; i++) {
            double dx = this.random.nextGaussian() * 0.02D;
            double dy = this.random.nextGaussian() * 0.02D;
            double dz = this.random.nextGaussian() * 0.02D;

            this.level().addParticle(
                    success ? ParticleTypes.HEART : ParticleTypes.SMOKE,
                    this.getRandomX(1.0D),
                    this.getRandomY() + 0.5D,
                    this.getRandomZ(1.0D),
                    dx,
                    dy,
                    dz
            );
        }
    }

    @Override
    public boolean canFallInLove() {
        return false;
    }

    @Override
    public void setInLove(@Nullable Player player) {
    }
}