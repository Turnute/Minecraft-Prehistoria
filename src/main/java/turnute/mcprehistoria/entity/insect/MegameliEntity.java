package turnute.mcprehistoria.entity.insect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MegameliEntity extends Bee implements GeoEntity {
    private static final RawAnimation FLY_ANIMATION = RawAnimation.begin().thenLoop("megameli_fly");

    private static final double RIDDEN_VERTICAL_PITCH_STRENGTH = 0.65D;
    private static final double RIDDEN_IDLE_VERTICAL_INPUT = 0.0D;
    private static final float RIDDEN_VERTICAL_DEADZONE_DEGREES = 6.0F;

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
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if(!this.level().isClientSide()) {
            player.startRiding(this);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity passenger = this.getFirstPassenger();
        return passenger instanceof LivingEntity ? (LivingEntity) passenger : null;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty() && passenger instanceof Player;
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
}