package ac.ghost.anticheat.prediction.bds.system.glide;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.math.BdsTrigMath;
import ac.ghost.anticheat.util.math.Vec3;














public final class GlideMoveSystem {
    private static final float LOOK_LENGTH_SCALE = 0.4F;
    private static final float LIFT_FACTOR = 0.75F;
    private static final float FALL_CONVERSION = -0.1F;
    private static final float DIVE_CONVERSION = 0.04F;
    private static final float DIVE_VERTICAL_FACTOR = 3.2F;
    private static final float STEERING_FACTOR = 0.1F;
    private static final float BOOST_TARGET_SPEED = 1.5F;
    private static final float BOOST_BLEND = 0.5F;
    private static final float BOOST_FORWARD = 0.1F;
    private static final float HORIZONTAL_DRAG = 0.99F;
    private static final float VERTICAL_DRAG = 0.98F;

    private GlideMoveSystem() {
    }

    public static Vec3 tick(final GhostPlayer player,
                            final Vec3 startVelocity) {
        final Vec3 velocity = startVelocity.clone();
        final Vec3 look = player.ghostMovementBridgeState.glideLook;
        final float pitchRadians = player.ghostMovementBridgeState.glidePitchRadians;

        final float horizontalLook = (float) Math.sqrt(
                look.x * look.x + look.z * look.z);
        final float horizontalSpeed = (float) Math.sqrt(
                velocity.x * velocity.x + velocity.z * velocity.z);
        final float lookLength = (float) Math.sqrt(
                look.x * look.x + look.y * look.y + look.z * look.z);

        final float pitchCos = BdsTrigMath.cos(pitchRadians);
        final float liftShape = pitchCos * pitchCos
                * Math.min(1.0F, lookLength / LOOK_LENGTH_SCALE);

        
        
        final float gravity = player.entityContext.mobEffectsComponent.effectiveGravity(startVelocity);
        velocity.y += gravity * (LIFT_FACTOR * liftShape - 1.0F);

        if (velocity.y < 0.0F && horizontalLook > 0.0F) {
            final float converted = velocity.y * FALL_CONVERSION * liftShape;
            velocity.x += look.x * converted / horizontalLook;
            velocity.y += converted;
            velocity.z += look.z * converted / horizontalLook;
        }

        if (pitchRadians < 0.0F && horizontalLook > 0.0F) {
            final float converted = horizontalSpeed
                    * -BdsTrigMath.sin(pitchRadians)
                    * DIVE_CONVERSION;
            velocity.x -= look.x * converted / horizontalLook;
            velocity.y += converted * DIVE_VERTICAL_FACTOR;
            velocity.z -= look.z * converted / horizontalLook;
        }

        if (horizontalLook > 0.0F) {
            velocity.x += (look.x / horizontalLook * horizontalSpeed
                    - velocity.x) * STEERING_FACTOR;
            velocity.z += (look.z / horizontalLook * horizontalSpeed
                    - velocity.z) * STEERING_FACTOR;
        }

        if (player.entityContext.movementEffectsComponent.glideBoostTicks > 0) {
            velocity.x += (look.x * BOOST_TARGET_SPEED - velocity.x)
                    * BOOST_BLEND + look.x * BOOST_FORWARD;
            velocity.y += (look.y * BOOST_TARGET_SPEED - velocity.y)
                    * BOOST_BLEND + look.y * BOOST_FORWARD;
            velocity.z += (look.z * BOOST_TARGET_SPEED - velocity.z)
                    * BOOST_BLEND + look.z * BOOST_FORWARD;
        }

        velocity.x *= HORIZONTAL_DRAG;
        velocity.y *= VERTICAL_DRAG;
        velocity.z *= HORIZONTAL_DRAG;
        return velocity;
    }
}
