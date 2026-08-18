package ac.ghost.anticheat.prediction.bds.system.block;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.BlockMovementSlowdownAppliedComponent;
import ac.ghost.anticheat.prediction.bds.component.BlockMovementSlowdownMultiplierComponent;
import ac.ghost.anticheat.prediction.bds.component.FallDistanceComponent;
import ac.ghost.anticheat.prediction.bds.component.MoveRequestComponent;
import ac.ghost.anticheat.prediction.bds.component.StateVectorComponent;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.potion.Effect;


public final class BlockMovementSlowdownMultiplierSystem {
    private static final Vec3 WEAVING_WEB_MULTIPLIER =
            new Vec3(0.5F, 0.25F, 0.5F);

    private BlockMovementSlowdownMultiplierSystem() {
    }

    



    public static void adjustFallDistance(
            final BlockMovementSlowdownAppliedComponent applied,
            final FallDistanceComponent fallDistance) {
        if (applied == null || fallDistance == null || !applied.isPresent()) {
            return;
        }
        fallDistance.setValue(0.0F);
    }

    





    public static void applySlowdownOnMove(
            final BlockMovementSlowdownMultiplierComponent multiplier,
            final MoveRequestComponent moveRequest,
            final StateVectorComponent stateVector) {
        apply(multiplier, moveRequest, stateVector);
    }

    private static void apply(
            final BlockMovementSlowdownMultiplierComponent multiplier,
            final MoveRequestComponent moveRequest,
            final StateVectorComponent stateVector) {
        if (moveRequest == null || multiplier == null || !multiplier.isPresent()) {
            return;
        }

        moveRequest.multiplyMovement(multiplier.value());
        if (stateVector != null) {
            stateVector.setDelta(Vec3.ZERO.clone());
        }
        multiplier.clear();
    }

    
    public static void cleanupSystem(
            final BlockMovementSlowdownAppliedComponent applied) {
        if (applied != null) {
            applied.clear();
        }
    }

    
    public static void immunePlayer(final GhostPlayer player) {
        if (player.entityContext.movementAbilitiesComponent.isFlying()
                && player.entityContext.movementAbilitiesComponent.isInstabuild()) {
            clearSlowdown(
                    player.entityContext.blockMovementSlowdownMultiplierComponent,
                    player.entityContext.blockMovementSlowdownAppliedComponent);
        }
    }

    
    public static void resistantMob(final GhostPlayer player) {
        if (!player.entityContext.insideWebBlockComponent.isPresent()
                || !player.entityContext.mobEffectsComponent.has(Effect.WEAVING)
                || !player.entityContext.insidePowderSnowBlockComponent.isEmpty()
                || !player.entityContext.insideSweetBerryBushBlockComponent.isEmpty()) {
            return;
        }
        player.entityContext.blockMovementSlowdownMultiplierComponent.set(
                WEAVING_WEB_MULTIPLIER);
        player.entityContext.blockMovementSlowdownAppliedComponent.markApplied();
    }

    private static void clearSlowdown(
            final BlockMovementSlowdownMultiplierComponent multiplier,
            final BlockMovementSlowdownAppliedComponent applied) {
        multiplier.clear();
        applied.clear();
    }
}
