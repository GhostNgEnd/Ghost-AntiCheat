package ac.ghost.anticheat.prediction.bds.system.restitution.ApplyRestitutionSystem;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.ApplyRestitutionComponent;
import ac.ghost.anticheat.util.math.Vec3;


public final class ApplyRestitution {
    private ApplyRestitution() {
    }

    public static Vec3 tick(final GhostPlayer player, final Vec3 resetVelocity) {
        final ApplyRestitutionComponent restitution =
                player.entityContext.applyRestitutionComponent;
        if (restitution == null || !restitution.hasRestitution()) {
            return resetVelocity;
        }

        final Vec3 result = resetVelocity.add(restitution.velocity());
        player.ghostMovementBridgeState.debugRestitutionApplied = true;
        player.ghostMovementBridgeState.debugRestitutionVelocity =
                restitution.velocity().clone();
        return result;
    }
}
