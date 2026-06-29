package turnute.mcprehistoria.entity.dino;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import turnute.mcprehistoria.registry.ModItems;

public class OdontheriumEntity extends TamableAnimal implements GeoEntity {
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("odontherium_idle");
    private static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenLoop("odontherium_walk");
    private static final RawAnimation SIT_ANIMATION = RawAnimation.begin().thenLoop("odontherium_sit");
    private static final RawAnimation SLEEP_ANIMATION = RawAnimation.begin().thenLoop("odontherium_sleep");

    private static final int TAMING_CHANCE = 3;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public OdontheriumEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 36.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.15D, 10.0F, 2.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F) {
            @Override
            public boolean canUse() {
                return !OdontheriumEntity.this.isOdontheriumSleeping()
                        && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !OdontheriumEntity.this.isOdontheriumSleeping()
                        && super.canContinueToUse();
            }
        });

        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return !OdontheriumEntity.this.isOdontheriumSleeping()
                        && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !OdontheriumEntity.this.isOdontheriumSleeping()
                        && super.canContinueToUse();
            }
        });

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));

        // Tant qu'il est sauvage, il attaque les joueurs.
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                return !OdontheriumEntity.this.isTame() && super.canUse();
            }
        });
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!this.isTame() && stack.is(ModItems.ANCIENT_BONE)) {
            if (!this.level().isClientSide()) {
                tryTame(player, stack);
            }

            return InteractionResult.SUCCESS;
        }

        if (this.isTame() && this.isOwnedBy(player)) {
            if (!this.level().isClientSide()) {
                boolean shouldSit = !this.isOrderedToSit();

                this.setOrderedToSit(shouldSit);
                this.setInSittingPose(shouldSit);

                if (shouldSit) {
                    this.getNavigation().stop();
                    this.setTarget(null);
                }
            }

            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    private static SoundEvent wolfAmbient(WolfSoundVariants.SoundSet soundSet) {
        WolfSoundVariant.WolfSoundSet sounds = SoundEvents.WOLF_SOUNDS
                .get(soundSet)
                .adultSounds();

        return sounds.ambientSound().value();
    }

    private static SoundEvent wolfGrowl(WolfSoundVariants.SoundSet soundSet) {
        WolfSoundVariant.WolfSoundSet sounds = SoundEvents.WOLF_SOUNDS
                .get(soundSet)
                .adultSounds();

        return sounds.growlSound().value();
    }

    private void tryTame(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        if (this.random.nextInt(TAMING_CHANCE) == 0) {
            this.tame(player);
            this.setHealth(this.getMaxHealth());
            this.getNavigation().stop();
            this.setTarget(null);
            this.setOrderedToSit(true);
            this.setInSittingPose(true);
            this.setTarget(null);
            this.setAggressive(false);
            this.level().broadcastEntityEvent(this, (byte) 7);
            this.playSound(wolfAmbient(WolfSoundVariants.SoundSet.BIG), 1.0F, 1.2F);
        } else {
            this.level().broadcastEntityEvent(this, (byte) 6);
            this.playSound(wolfGrowl(WolfSoundVariants.SoundSet.ANGRY), 0.8F, 1.0F);
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (this.isTame() && this.isOwnedBy(target)) {
            return false;
        }

        return super.canAttack(target);
    }

    @Override
    public boolean canFallInLove() {
        return false;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(state -> {
            boolean sitting = this.isInSittingPose();

            if (this.isOdontheriumSleeping()) {
                return state.setAndContinue(SLEEP_ANIMATION);
            }

            if (sitting) {
                return state.setAndContinue(SIT_ANIMATION);
            }

            if (state.isMoving()) {
                return state.setAndContinue(WALK_ANIMATION);
            }

            return state.setAndContinue(IDLE_ANIMATION);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public static boolean canSpawn(
            EntityType<OdontheriumEntity> entityType,
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.entity.EntitySpawnReason spawnReason,
            net.minecraft.core.BlockPos pos,
            RandomSource random
    ) {
        return level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && level.getFluidState(pos).isEmpty()
                && random.nextInt(6) == 0;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            LivingEntity target = this.getTarget();

            this.setAggressive(!this.isTame() && target instanceof Player);
        }
    }

    private boolean isOdontheriumSleeping() {
        return this.isInSittingPose() && this.level().isDarkOutside();
    }
}