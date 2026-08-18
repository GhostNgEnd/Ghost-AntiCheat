package ac.ghost.anticheat.prediction.bds.system.restitution;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.BounceGravityCorrectionComponent;
import ac.ghost.anticheat.prediction.bds.component.StateVectorComponent;
import ac.ghost.anticheat.prediction.bds.math.BdsMovementMath;
import ac.ghost.anticheat.prediction.bds.system.block.ScaffoldingActionSystem;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.entity.Entity;


public final class ApplyGravityWithBounceCorrection {
    private ApplyGravityWithBounceCorrection() {
    }

    public static void tick(final GhostPlayer player,
                            final StateVectorComponent stateVector,
                            final float ordinaryGravityDeltaY) {
        final Vec3 velocity = stateVector.getDelta();
        final float resolvedGravity = resolveGravityDelta(player, ordinaryGravityDeltaY);
        velocity.y = velocity.y + resolvedGravity;
    }

    public static float resolveGravityDelta(final GhostPlayer player,
                                            final float ordinaryGravityDeltaY) {
        final BounceGravityCorrectionComponent correction =
                player.entityContext.bounceGravityCorrection;
        player.entityContext.bounceGravityCorrection = null;
        player.ghostMovementBridgeState.debugElasticTrace.ordinaryGravityY =
                ordinaryGravityDeltaY;
        if (correction == null) {
            return recordResolvedGravity(player, ordinaryGravityDeltaY);
        }

        final float requestedY = correction.requestedY();
        final float resolvedY = correction.resolvedY();
        player.ghostMovementBridgeState.debugBounceGravityCorrection = true;
        player.ghostMovementBridgeState.debugBounceRequestedY = requestedY;
        player.ghostMovementBridgeState.debugBounceResolvedY = resolvedY;
        player.ghostMovementBridgeState.debugElasticTrace.gravityCorrectionApplied = true;
        player.ghostMovementBridgeState.debugElasticTrace.correctionRequestedY =
                requestedY;
        player.ghostMovementBridgeState.debugElasticTrace.correctionResolvedY =
                resolvedY;

        final float resolvedMagnitude = Math.abs(resolvedY);
        player.ghostMovementBridgeState.debugElasticTrace.resolvedMagnitude =
                resolvedMagnitude;
        if (resolvedMagnitude <= BdsMovementMath.FLOAT_EPSILON) {
            return recordResolvedGravity(player, ordinaryGravityDeltaY);
        }

        final float gravityMagnitude = Math.abs(ordinaryGravityDeltaY);
        player.ghostMovementBridgeState.debugElasticTrace.gravityMagnitude =
                gravityMagnitude;
        if (gravityMagnitude <= BdsMovementMath.FLOAT_EPSILON) {
            return recordResolvedGravity(player, ordinaryGravityDeltaY);
        }

        final float requestedSquared = requestedY * requestedY;
        final float twiceGravity = gravityMagnitude + gravityMagnitude;
        final float radicand = twiceGravity * resolvedMagnitude + requestedSquared;
        final float impactSpeed = BdsMovementMath.sqrtf(radicand);
        final float impactDifference = Math.abs(requestedY) - impactSpeed;
        final float impactFraction = impactDifference / ordinaryGravityDeltaY;
        final float remainingTick = BdsMovementMath.ONE - Math.abs(impactFraction);

        player.ghostMovementBridgeState.debugElasticTrace.radicand = radicand;
        player.ghostMovementBridgeState.debugElasticTrace.impactSpeed = impactSpeed;
        player.ghostMovementBridgeState.debugElasticTrace.impactFraction =
                impactFraction;
        player.ghostMovementBridgeState.debugElasticTrace.remainingTick =
                remainingTick;

        
        final float requestedSign = requestedY > 0.0F
                ? 1.0F : requestedY < 0.0F ? -1.0F : 0.0F;
        return recordResolvedGravity(
                player, gravityMagnitude * remainingTick * requestedSign);
    }

    private static float recordResolvedGravity(final GhostPlayer player,
                                               final float value) {
        player.ghostMovementBridgeState.debugBounceAppliedGravity = value;
        player.ghostMovementBridgeState.debugElasticTrace.appliedGravityY = value;
        return value;
    }

    
    public static boolean shouldApplyGroundOrAir(final GhostPlayer player) {
        
        
        
        

        final boolean descendThroughBlock = player.entityContext.actorDataFlagComponent.has(
                ScaffoldingActionSystem.DESCEND_THROUGH_BLOCK_FLAG);
        final boolean inScaffolding = player.entityContext.actorDataFlagComponent.has(
                Entity.DATA_FLAG_IN_SCAFFOLDING);
        final boolean overScaffolding = player.entityContext.actorDataFlagComponent.has(
                Entity.DATA_FLAG_OVER_SCAFFOLDING);

        
        
        
        
        
        return !(descendThroughBlock && (inScaffolding || overScaffolding));
    }

    public static void clear(final GhostPlayer player) {
        player.entityContext.bounceGravityCorrection = null;
    }
}
