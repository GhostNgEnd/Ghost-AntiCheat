package ac.ghost.anticheat.prediction.bds.system.teleport;

import ac.ghost.anticheat.data.input.PredictionData;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.teleport.data.TeleportData;
import ac.ghost.anticheat.util.math.Vec3;


public final class TeleportInterpolatorResetSystem {
    private TeleportInterpolatorResetSystem() {}

    public static void tick(final GhostPlayer player, final TeleportData data,
                            final Vec3 footTarget) {
        player.entityContext.stateVectorComponent.setPreviousPosition(footTarget.clone());
        player.entityContext.actorRotationComponent.snap(
                data.getPitch(), data.getYaw(), data.getHeadYaw());

        player.entityContext.serverPlayerCurrentMovementComponent.setUnvalidatedPosition(
                footTarget.clone());
        player.entityContext.serverPlayerCurrentMovementComponent.setPreviousUnvalidatedPosition(
                footTarget.clone());
        player.entityContext.serverPlayerCurrentMovementComponent.setUnvalidatedTickEnd(
                Vec3.ZERO.clone());
        player.entityContext.serverPlayerCurrentMovementComponent.setPreviousUnvalidatedTickEnd(
                Vec3.ZERO.clone());
        player.entityContext.serverPlayerCurrentMovementComponent.setBeforeCollision(
                Vec3.ZERO.clone());
        player.entityContext.serverPlayerCurrentMovementComponent.setAfterCollision(
                Vec3.ZERO.clone());
        player.entityContext.serverPlayerCurrentMovementComponent.setLastTickFinalVelocity(
                Vec3.ZERO.clone());
        player.entityContext.serverPlayerCurrentMovementComponent.setPredictionResult(
                new PredictionData(Vec3.ZERO, Vec3.ZERO, Vec3.ZERO));
    }
}
