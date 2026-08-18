package ac.ghost.anticheat.prediction.bds.system.glide;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.math.BdsTrigMath;
import ac.ghost.anticheat.util.MathUtil;
import ac.ghost.anticheat.util.math.Vec3;









public final class GlideInputSystem {
    private static final float PI = (float) Math.PI;

    private GlideInputSystem() {
    }

    public static void tick(final GhostPlayer player) {
        final float pitch = unwrapDegrees(player.entityContext.actorRotationComponent.getPreviousPitch(),
                player.entityContext.actorRotationComponent.getPitch());
        final float yaw = unwrapDegrees(player.entityContext.actorRotationComponent.getPreviousYaw(),
                player.entityContext.actorRotationComponent.getYaw());
        final float pitchRadians = pitch * MathUtil.DEGREE_TO_RAD;

        
        
        
        final float negativePitchRadians = pitch * -MathUtil.DEGREE_TO_RAD;
        final float shiftedYawRadians = yaw * -MathUtil.DEGREE_TO_RAD - PI;
        final float negativePitchCos = -BdsTrigMath.cos(negativePitchRadians);
        final Vec3 look = new Vec3(
                negativePitchCos * BdsTrigMath.sin(shiftedYawRadians),
                BdsTrigMath.sin(negativePitchRadians),
                negativePitchCos * BdsTrigMath.cos(shiftedYawRadians)
        );

        player.ghostMovementBridgeState.glideLook = look;
        player.ghostMovementBridgeState.glidePitchRadians = pitchRadians;
    }

    private static float unwrapDegrees(final float previous,
                                       final float current) {
        return previous + positiveModulo(current - previous + 180.0F, 360.0F)
                - 180.0F;
    }

    private static float positiveModulo(final float value,
                                        final float modulus) {
        final float remainder = value % modulus;
        return remainder < 0.0F ? remainder + modulus : remainder;
    }
}
