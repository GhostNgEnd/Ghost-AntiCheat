package ac.ghost.anticheat.prediction.bds.system.travel;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Vec3;




public final class MobTravelIntentSystem {
    private static final float ZERO_VELOCITY_EPSILON = 1.0E-8F;
    private static final float INPUT_DAMPING = 0.98F;

    private MobTravelIntentSystem() {
    }

    public static void tick(final GhostPlayer player) {
        player.entityContext.stateVectorComponent.setDelta(sanitizeVelocity(player.entityContext.stateVectorComponent.getDelta()));
        player.entityContext.mobTravelComponent.setInput(applyInputScale(player, player.entityContext.mobTravelComponent.getInput()));
    }

    public static Vec3 applyInputScale(final GhostPlayer player,
                                       final Vec3 input) {
        return input.multiply(INPUT_DAMPING);
    }

    public static Vec3 sanitizeVelocity(final Vec3 velocity) {
        return new Vec3(
                zeroSmall(velocity.x),
                zeroSmall(velocity.y),
                zeroSmall(velocity.z));
    }

    private static float zeroSmall(final float value) {
        return Math.abs(value) < ZERO_VELOCITY_EPSILON ? 0.0F : value;
    }
}
