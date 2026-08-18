package ac.ghost.anticheat.prediction.bds.system.restitution;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.ApplyRestitutionComponent;
import ac.ghost.anticheat.prediction.bds.component.BounceGravityCorrectionComponent;
import ac.ghost.anticheat.prediction.bds.component.MoveRequestComponent;


public final class RequestGravityCorrectionSystem {
    private static final float FLOAT_EPSILON = 1.1920929E-7F;

    private RequestGravityCorrectionSystem() {
    }

    





    public static void tick(final GhostPlayer player) {
        player.ghostMovementBridgeState.debugElasticTrace.gravityRequestChecked = true;
        final ApplyRestitutionComponent restitution =
                player.entityContext.applyRestitutionComponent;
        player.ghostMovementBridgeState.debugElasticTrace.gravityRequestRestitutionY =
                restitution == null ? 0.0F : restitution.velocity().y;
        if (restitution == null
                || Math.abs(restitution.velocity().y) <= FLOAT_EPSILON) {
            return;
        }

        final MoveRequestComponent request = player.entityContext.moveRequestComponent;
        player.ghostMovementBridgeState.debugElasticTrace.gravityRequestMovementY =
                request.movement().y;
        final float resolvedY = request.resolvedMovement().y;
        player.ghostMovementBridgeState.debugElasticTrace.gravityRequestResolvedY =
                resolvedY;
        if (Math.abs(resolvedY) <= FLOAT_EPSILON) {
            return;
        }

        player.entityContext.bounceGravityCorrection =
                new BounceGravityCorrectionComponent(
                        request.movement().y, resolvedY);
        player.ghostMovementBridgeState.debugElasticTrace.gravityRequestCreated = true;
    }
}
