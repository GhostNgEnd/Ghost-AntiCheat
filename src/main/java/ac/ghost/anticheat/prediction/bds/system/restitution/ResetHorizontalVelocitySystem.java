package ac.ghost.anticheat.prediction.bds.system.restitution;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MoveRequestComponent;
import ac.ghost.anticheat.util.math.Vec3;


public final class ResetHorizontalVelocitySystem {
    private static final float FLOAT_EPSILON = 1.1920929E-7F;

    private ResetHorizontalVelocitySystem() {
    }

    public static Vec3 tick(final GhostPlayer player, final Vec3 velocity) {
        if (player == null || velocity == null) {
            return velocity;
        }
        final MoveRequestComponent request = player.entityContext.moveRequestComponent;
        final Vec3 submitted = request.movement();
        final Vec3 resolved = request.resolvedMovement();
        return new Vec3(
                Math.abs(submitted.x - resolved.x) > FLOAT_EPSILON ? 0.0F : velocity.x,
                velocity.y,
                Math.abs(submitted.z - resolved.z) > FLOAT_EPSILON ? 0.0F : velocity.z
        );
    }
}
