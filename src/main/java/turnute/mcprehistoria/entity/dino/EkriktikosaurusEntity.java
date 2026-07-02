package turnute.mcprehistoria.entity.dino;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.EnumSet;

public class EkriktikosaurusEntity extends Monster implements GeoEntity {
    private static final RawAnimation IDLE_ANIMATION =
            RawAnimation.begin().thenLoop("ekriktikosaurus_idle");

    private static final RawAnimation WALK_ANIMATION =
            RawAnimation.begin().thenLoop("ekriktikosaurus_walk");

    private static final RawAnimation SPARK_ANIMATION =
            RawAnimation.begin().thenLoop("ekriktikosaurus_spark");

    private static final EntityDataAccessor<Boolean> DATA_SWELLING = SynchedEntityData.defineId(
            EkriktikosaurusEntity.class,
            EntityDataSerializers.BOOLEAN
    );

    private static final String SWELL_TICKS_KEY = "SwellTicks";
    private static final String EXPLOSION_COOLDOWN_KEY = "ExplosionCooldown";

    // Creeper vanilla ≈ 30 ticks. Ici 80 ticks = 4 secondes.
    private static final int MAX_SWELL_TICKS = 50;

    // Pause après explosion pour éviter qu'il explose en boucle.
    private static final int POST_EXPLOSION_COOLDOWN_TICKS = 30;

    // Creeper vanilla ≈ 3. Ici dégâts/rayon réduits.
    private static final float EXPLOSION_POWER = 2.0F;

    // Distance au carré. 10.5 ≈ 3.2 blocs.
    private static final double SWELL_DISTANCE_SQR = 10.5D;

    // Vitesse de poursuite. Il continue d'avancer même pendant la charge.
    private static final double CHASE_SPEED = 1.15D;

    private int swellTicks;
    private int explosionCooldown;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public EkriktikosaurusEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 34.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.45D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)

                // Il ne tape pas physiquement.
                // On garde l'attribut à 0 pour éviter toute surprise.
                .add(Attributes.ATTACK_DAMAGE, 0.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));

        // Remplace le MeleeAttackGoal.
        // Il poursuit sa cible, mais ne frappe jamais.
        this.goalSelector.addGoal(2, new ExplosionChaseGoal(this, CHASE_SPEED));

        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        // S'il est attaqué par un chien, golem, joueur, etc.,
        // il prend cette entité comme cible et se défend à l'explosion.
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));

        // Il reste hostile envers les joueurs naturellement.
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SWELLING, false);
    }

    public boolean isSwelling() {
        return this.getEntityData().get(DATA_SWELLING);
    }

    private void setSwelling(boolean swelling) {
        this.getEntityData().set(DATA_SWELLING, swelling);
    }

    /**
     * Utilisé côté renderer pour alterner entre texture normale et texture blanche.
     */
    public boolean shouldFlashWhite() {
        return this.isSwelling() && (this.tickCount / 4) % 2 == 0;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide()) {
            return;
        }

        if (this.explosionCooldown > 0) {
            this.explosionCooldown--;
            this.setSwelling(false);
            this.swellTicks = Math.max(0, this.swellTicks - 2);
            return;
        }

        LivingEntity target = this.getTarget();

        boolean shouldSwell = target != null
                && target.isAlive()
                && this.distanceToSqr(target) <= SWELL_DISTANCE_SQR
                && this.hasLineOfSight(target);

        if (shouldSwell) {
            tickExplosionCharge();
        } else {
            calmDown();
        }
    }

    private void tickExplosionCharge() {
        if (this.swellTicks == 0) {
            this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.65F);
        }

        this.setSwelling(true);

        // Important : on ne stoppe plus la navigation.
        // Avant tu avais this.getNavigation().stop(), ce qui l'immobilisait.
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            this.getNavigation().moveTo(target, CHASE_SPEED);
        }

        this.swellTicks++;

        if (this.swellTicks >= MAX_SWELL_TICKS) {
            explodeWithoutDying();
        }
    }

    private void calmDown() {
        this.setSwelling(false);

        if (this.swellTicks > 0) {
            this.swellTicks = Math.max(0, this.swellTicks - 2);
        }
    }

    private void explodeWithoutDying() {
        this.swellTicks = 0;
        this.setSwelling(false);
        this.explosionCooldown = POST_EXPLOSION_COOLDOWN_TICKS;

        float healthBeforeExplosion = this.getHealth();
        boolean wasInvulnerable = this.isInvulnerable();

        // Sécurité : il ne doit jamais mourir de sa propre explosion.
        this.setInvulnerable(true);

        this.level().explode(
                this,
                this.getX(),
                this.getY(),
                this.getZ(),
                EXPLOSION_POWER,
                false,
                Level.ExplosionInteraction.NONE
        );

        this.setInvulnerable(wasInvulnerable);

        if (this.isAlive() && this.getHealth() < healthBeforeExplosion) {
            this.setHealth(healthBeforeExplosion);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        boolean hurt = super.hurtServer(level, damageSource, amount);

        if (hurt && damageSource.getEntity() instanceof LivingEntity attacker && attacker != this) {
            this.setTarget(attacker);

            // S'il se fait attaquer au corps-à-corps, il commence à charger
            // dès que l'attaquant est assez proche.
            if (this.explosionCooldown <= 0 && this.distanceToSqr(attacker) <= SWELL_DISTANCE_SQR) {
                this.setSwelling(true);
            }
        }

        return hurt;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(state -> {
            if (this.isSwelling()) {
                return state.setAndContinue(SPARK_ANIMATION);
            }

            if (state.isMoving()) {
                return state.setAndContinue(WALK_ANIMATION);
            }

            // Pas d'idle dédié pour l'instant.
            return state.setAndContinue(IDLE_ANIMATION);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(SWELL_TICKS_KEY, this.swellTicks);
        output.putInt(EXPLOSION_COOLDOWN_KEY, this.explosionCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.swellTicks = input.getIntOr(SWELL_TICKS_KEY, 0);
        this.explosionCooldown = input.getIntOr(EXPLOSION_COOLDOWN_KEY, 0);
    }

    public static boolean canSpawn(
            EntityType<EkriktikosaurusEntity> entityType,
            ServerLevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random
    ) {
        return level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && level.getFluidState(pos).isEmpty()
                && random.nextInt(8) == 0;
    }

    private static class ExplosionChaseGoal extends Goal {
        private final EkriktikosaurusEntity mob;
        private final double speedModifier;
        private int repathCooldown;

        private ExplosionChaseGoal(EkriktikosaurusEntity mob, double speedModifier) {
            this.mob = mob;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();

            return target != null
                    && target.isAlive()
                    && this.mob.explosionCooldown <= 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void stop() {
            this.mob.getNavigation().stop();
            this.repathCooldown = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();

            if (target == null) {
                return;
            }

            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (--this.repathCooldown <= 0) {
                this.repathCooldown = 4 + this.mob.getRandom().nextInt(7);
                this.mob.getNavigation().moveTo(target, this.speedModifier);
            }
        }
    }
}