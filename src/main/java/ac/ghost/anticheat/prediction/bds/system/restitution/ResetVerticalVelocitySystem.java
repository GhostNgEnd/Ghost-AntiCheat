package ac.ghost.anticheat.prediction.bds.system.restitution;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Vec3;


public final class ResetVerticalVelocitySystem {
    private ResetVerticalVelocitySystem() {
    }

    public static Vec3 tick(final GhostPlayer player, final Vec3 velocity) {
        if (player == null || velocity == null) {
            return velocity;
        }
        return new Vec3(
                velocity.x,
                player.entityContext.verticalCollisionFlagComponent.isPresent() ? 0.0F : velocity.y,
                velocity.z
        );
    }
}
