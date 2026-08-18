package ac.ghost.anticheat.prediction.bds.system.liquid.water;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.GhostTrigMath;
import ac.ghost.anticheat.util.MathUtil;
import ac.ghost.anticheat.util.math.Vec3;


public final class WaterMoveSystem {
    private static final float MIN_INPUT_LENGTH_SQUARED = 0.0001F;

    private WaterMoveSystem() {
    }

    public static Vec3 tick(final GhostPlayer player,
                            final Vec3 velocity,
                            final float travelStrength) {
        final Vec3 input = player.entityContext.mobTravelComponent.getInput();
        final float lengthSquared = input.lengthSquared();
        if (lengthSquared < MIN_INPUT_LENGTH_SQUARED) {
            return velocity;
        }

        final float length = (float) Math.sqrt(lengthSquared);
        final float scale = travelStrength / Math.max(length, 1.0F);
        final Vec3 local = input.multiply(scale);
        final float yaw = player.entityContext.actorRotationComponent.getYaw() * MathUtil.DEGREE_TO_RAD;
        final float sin = GhostTrigMath.sin(yaw);
        final float cos = GhostTrigMath.cos(yaw);
        return velocity.add(
                local.x * cos - local.z * sin,
                local.y,
                local.z * cos + local.x * sin);
    }
}
