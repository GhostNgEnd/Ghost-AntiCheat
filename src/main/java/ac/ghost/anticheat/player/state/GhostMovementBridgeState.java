package ac.ghost.anticheat.player.state;

import ac.ghost.anticheat.data.EntityDimensions;
import ac.ghost.anticheat.port.movement.PendingPistonMovement;
import ac.ghost.anticheat.prediction.nukkit.component.NukkitItemUseStateComponent;
import ac.ghost.anticheat.prediction.nukkit.component.NukkitSneakInputStateComponent;
import ac.ghost.anticheat.prediction.bds.system.liquid.common.LiquidPhysicsSystem;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.types.AuthInputAction;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;







public final class GhostMovementBridgeState {
    public final PendingPistonMovement pendingPistonMovement =
            new PendingPistonMovement();
    public final NukkitItemUseStateComponent nukkitItemUseStateComponent =
            new NukkitItemUseStateComponent();
    public final NukkitSneakInputStateComponent nukkitSneakInputStateComponent =
            new NukkitSneakInputStateComponent();

    public int ticksSinceCanSlowdown;

    
    
    public Vec3 glideLook = Vec3.ZERO;
    public float glidePitchRadians;
    public long liquidSampledTick = Long.MIN_VALUE;
    public LiquidPhysicsSystem.Sample waterSample = LiquidPhysicsSystem.Sample.EMPTY;
    public LiquidPhysicsSystem.Sample lavaSample = LiquidPhysicsSystem.Sample.EMPTY;
    public boolean nukkitGlideBoostPending;
    public final Set<Long> nukkitGlideBoostEntities = new HashSet<>();
    public int ticksSinceSwimming;
    public int ticksSinceCrawling;
    public boolean downwardLiquidEncountered;
    public boolean wasPredictionSwimming;
    public boolean wasPredictionCrawling;
    public EntityDimensions dimensionsBeforeAuthSwimming;
    public boolean authSwimmingPoseApplied;
    
    public boolean suppressStartGlidingAction;
    public BlockVector3 cachedOnPos;

    





    public boolean predictionHasDigitalDirectionState = true;
    public boolean predictionHasRawMoveVector;
    public long legacyInputTick;
    




    public boolean legacyRespawnTransition;
    private boolean legacyJumping;
    private boolean legacySneaking;
    private final Set<AuthInputAction> legacyPendingInputActions =
            EnumSet.noneOf(AuthInputAction.class);

    public synchronized void queueLegacyInputAction(
            final AuthInputAction action) {
        if (action != null) {
            this.legacyPendingInputActions.add(action);
        }
    }

    public synchronized Set<AuthInputAction> consumeLegacyInputActions() {
        final Set<AuthInputAction> result = this.legacyPendingInputActions
                .isEmpty()
                ? EnumSet.noneOf(AuthInputAction.class)
                : EnumSet.copyOf(this.legacyPendingInputActions);
        this.legacyPendingInputActions.clear();
        return result;
    }

    public synchronized void updateLegacyButtonState(
            final boolean jumping,
            final boolean sneaking) {
        if (jumping && !this.legacyJumping) {
            this.legacyPendingInputActions.add(
                    AuthInputAction.START_JUMPING);
        }
        if (jumping) {
            this.legacyPendingInputActions.add(AuthInputAction.JUMPING);
        }
        if (sneaking != this.legacySneaking) {
            this.legacyPendingInputActions.add(sneaking
                    ? AuthInputAction.START_SNEAKING
                    : AuthInputAction.STOP_SNEAKING);
        }
        if (sneaking) {
            this.legacyPendingInputActions.add(AuthInputAction.SNEAKING);
        }
        this.legacyJumping = jumping;
        this.legacySneaking = sneaking;
    }

    public synchronized void resetLegacyInputBridge() {
        this.legacyInputTick = 0L;
        this.legacyPendingInputActions.clear();
        this.legacyJumping = false;
        this.legacySneaking = false;
        this.predictionHasDigitalDirectionState = true;
        this.predictionHasRawMoveVector = false;
    }

    
    
    
    public boolean debugSwimStartRequested;
    public boolean debugSwimStartAccepted;
    public boolean debugSwimStopTriggered;
    public boolean debugStandSpeedAlteringApplied;
    public float debugStandSpeedVerticalSpeed;
    public float debugStandSpeedHorizontalScale = 1.0F;
    public boolean debugRestitutionApplied;
    public Vec3 debugRestitutionVelocity = Vec3.ZERO.clone();
    public boolean debugBounceGravityCorrection;
    public float debugBounceRequestedY;
    public float debugBounceResolvedY;
    public float debugBounceAppliedGravity;
    public float debugRestitutionImpactY;
    public int debugRestitutionCollisionCandidateCount;
    public String debugRestitutionSelectedBlock = "null";
    public String debugBdsCorrectionResult = "NOT_RUN";
    public final ElasticPhysicsDebugTrace debugElasticTrace =
            new ElasticPhysicsDebugTrace();
    public ElasticPhysicsDebugTrace debugPreviousElasticTrace =
            new ElasticPhysicsDebugTrace();

    public void resetPredictionDebugTrace(final long inputTick) {
        debugPreviousElasticTrace = debugElasticTrace.copy();
        debugElasticTrace.reset(inputTick);
        debugSwimStartRequested = false;
        debugSwimStartAccepted = false;
        debugSwimStopTriggered = false;
        debugStandSpeedAlteringApplied = false;
        debugStandSpeedVerticalSpeed = 0.0F;
        debugStandSpeedHorizontalScale = 1.0F;
        debugRestitutionApplied = false;
        debugRestitutionVelocity = Vec3.ZERO.clone();
        debugBounceGravityCorrection = false;
        debugBounceRequestedY = 0.0F;
        debugBounceResolvedY = 0.0F;
        debugBounceAppliedGravity = 0.0F;
        debugRestitutionImpactY = 0.0F;
        debugRestitutionCollisionCandidateCount = 0;
        debugRestitutionSelectedBlock = "null";
        debugBdsCorrectionResult = "NOT_RUN";
    }

    





    public static final class ElasticPhysicsDebugTrace {
        public long inputTick = -1L;
        public Vec3 tickStartPosition = Vec3.ZERO.clone();
        public Vec3 carriedStartVelocity = Vec3.ZERO.clone();
        public Vec3 selectedStartVelocity = Vec3.ZERO.clone();
        public boolean onGroundAtTickStart;
        public boolean onGroundBeforeTravel;
        public Vec3 submittedMovement = Vec3.ZERO.clone();
        public Vec3 resolvedMovement = Vec3.ZERO.clone();
        public String originalAabb = "null";
        public String resolvedAabb = "null";
        public Vec3 tickEndPosition = Vec3.ZERO.clone();
        public Vec3 tickEndVelocity = Vec3.ZERO.clone();
        public boolean onGroundAtTickEnd;
        public float restitutionImpactY;
        public int collisionCandidateCount;
        public String actorAabb = "null";
        public String collisionEntries = "[]";
        public String selectedShape = "null";
        public String selectedBlock = "null";
        public float restitutionCoefficient;
        public boolean gravityRequestChecked;
        public float gravityRequestRestitutionY;
        public float gravityRequestMovementY;
        public float gravityRequestResolvedY;
        public boolean gravityRequestCreated;
        public boolean gravityCorrectionApplied;
        public float ordinaryGravityY;
        public float correctionRequestedY;
        public float correctionResolvedY;
        public float gravityMagnitude;
        public float resolvedMagnitude;
        public float radicand;
        public float impactSpeed;
        public float impactFraction;
        public float remainingTick;
        public float appliedGravityY;

        public void reset(final long tick) {
            inputTick = tick;
            tickStartPosition = Vec3.ZERO.clone();
            carriedStartVelocity = Vec3.ZERO.clone();
            selectedStartVelocity = Vec3.ZERO.clone();
            onGroundAtTickStart = false;
            onGroundBeforeTravel = false;
            submittedMovement = Vec3.ZERO.clone();
            resolvedMovement = Vec3.ZERO.clone();
            originalAabb = "null";
            resolvedAabb = "null";
            tickEndPosition = Vec3.ZERO.clone();
            tickEndVelocity = Vec3.ZERO.clone();
            onGroundAtTickEnd = false;
            restitutionImpactY = 0.0F;
            collisionCandidateCount = 0;
            actorAabb = "null";
            collisionEntries = "[]";
            selectedShape = "null";
            selectedBlock = "null";
            restitutionCoefficient = 0.0F;
            gravityRequestChecked = false;
            gravityRequestRestitutionY = 0.0F;
            gravityRequestMovementY = 0.0F;
            gravityRequestResolvedY = 0.0F;
            gravityRequestCreated = false;
            gravityCorrectionApplied = false;
            ordinaryGravityY = 0.0F;
            correctionRequestedY = 0.0F;
            correctionResolvedY = 0.0F;
            gravityMagnitude = 0.0F;
            resolvedMagnitude = 0.0F;
            radicand = 0.0F;
            impactSpeed = 0.0F;
            impactFraction = 0.0F;
            remainingTick = 0.0F;
            appliedGravityY = 0.0F;
        }

        public ElasticPhysicsDebugTrace copy() {
            final ElasticPhysicsDebugTrace result = new ElasticPhysicsDebugTrace();
            result.inputTick = inputTick;
            result.tickStartPosition = tickStartPosition.clone();
            result.carriedStartVelocity = carriedStartVelocity.clone();
            result.selectedStartVelocity = selectedStartVelocity.clone();
            result.onGroundAtTickStart = onGroundAtTickStart;
            result.onGroundBeforeTravel = onGroundBeforeTravel;
            result.submittedMovement = submittedMovement.clone();
            result.resolvedMovement = resolvedMovement.clone();
            result.originalAabb = originalAabb;
            result.resolvedAabb = resolvedAabb;
            result.tickEndPosition = tickEndPosition.clone();
            result.tickEndVelocity = tickEndVelocity.clone();
            result.onGroundAtTickEnd = onGroundAtTickEnd;
            result.restitutionImpactY = restitutionImpactY;
            result.collisionCandidateCount = collisionCandidateCount;
            result.actorAabb = actorAabb;
            result.collisionEntries = collisionEntries;
            result.selectedShape = selectedShape;
            result.selectedBlock = selectedBlock;
            result.restitutionCoefficient = restitutionCoefficient;
            result.gravityRequestChecked = gravityRequestChecked;
            result.gravityRequestRestitutionY = gravityRequestRestitutionY;
            result.gravityRequestMovementY = gravityRequestMovementY;
            result.gravityRequestResolvedY = gravityRequestResolvedY;
            result.gravityRequestCreated = gravityRequestCreated;
            result.gravityCorrectionApplied = gravityCorrectionApplied;
            result.ordinaryGravityY = ordinaryGravityY;
            result.correctionRequestedY = correctionRequestedY;
            result.correctionResolvedY = correctionResolvedY;
            result.gravityMagnitude = gravityMagnitude;
            result.resolvedMagnitude = resolvedMagnitude;
            result.radicand = radicand;
            result.impactSpeed = impactSpeed;
            result.impactFraction = impactFraction;
            result.remainingTick = remainingTick;
            result.appliedGravityY = appliedGravityY;
            return result;
        }
    }

    
    
    
    
    public Vec3 lastServerMotion = Vec3.ZERO.clone();
    public long lastServerMotionTick = -1L;
    public long lastServerMotionWallClock = -1L;
}
