package ac.ghost.anticheat.prediction.bds.system.travel;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.StateVectorComponent;
import ac.ghost.anticheat.util.math.Vec3;


public final class MobMovementDrag {
    private static final float ZERO = Float.intBitsToFloat(0x00000000);
    private static final float ONE = Float.intBitsToFloat(0x3F800000);
    private static final float AUTO_CLIMB_DRAG =
            Float.intBitsToFloat(0x3F68F5C3); 
    private static final float PLAYER_FLYING_DRAG =
            Float.intBitsToFloat(0x3ECCCCCD);

    private MobMovementDrag() {
    }


    





    public static void tickGroundOrAir(final StateVectorComponent stateVector) {
        final Vec3 velocity = stateVector.getDelta();
        velocity.x = velocity.x * AUTO_CLIMB_DRAG;
        velocity.y = velocity.y * AUTO_CLIMB_DRAG;
        velocity.z = velocity.z * AUTO_CLIMB_DRAG;
    }

    public static void tickPlayerFlying(final GhostPlayer player,
                                        final StateVectorComponent stateVector) {
        float verticalFactor = ONE - PLAYER_FLYING_DRAG;
        if (verticalFactor < ZERO) {
            verticalFactor = ZERO;
        } else if (verticalFactor > ONE) {
            verticalFactor = ONE;
        }

        final Vec3 velocity = stateVector.getDelta();
        velocity.y = velocity.y * verticalFactor;
        player.entityContext.fallDistanceComponent.setValue(ZERO);
    }
}
