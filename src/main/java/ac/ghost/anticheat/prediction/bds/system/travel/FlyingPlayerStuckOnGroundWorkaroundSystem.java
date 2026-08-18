package ac.ghost.anticheat.prediction.bds.system.travel;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.StateVectorComponent;
import ac.ghost.anticheat.util.math.Vec3;


public final class FlyingPlayerStuckOnGroundWorkaroundSystem {
    private static final float MINIMUM_NON_ZERO_VERTICAL_MOTION =
            Float.intBitsToFloat(0x00000001);

    private FlyingPlayerStuckOnGroundWorkaroundSystem() {
    }

    





    public static void tick(final GhostPlayer player,
                            final StateVectorComponent stateVector) {
        if (!player.entityContext.movementAbilitiesComponent.isFlying()
                || !player.entityContext.onGroundFlagComponent.isPresent()) {
            return;
        }

        final Vec3 velocity = stateVector.getDelta();
        if (velocity.y == 0.0F) {
            velocity.y = MINIMUM_NON_ZERO_VERTICAL_MOTION;
        }
    }
}
