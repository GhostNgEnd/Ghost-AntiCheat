package ac.ghost.anticheat.prediction.bds.system.travel;

import ac.ghost.anticheat.prediction.bds.component.StateVectorComponent;
import ac.ghost.anticheat.util.math.Vec3;


public final class LevitateSystem {
    private static final float PREVIOUS_VELOCITY_FACTOR =
            Float.intBitsToFloat(0x3F4CCCCD);
    private static final float LEVEL_ACCELERATION =
            Float.intBitsToFloat(0x3C23D70A);

    private LevitateSystem() {
    }

    public static void tick(final StateVectorComponent stateVector,
                            final int amplifier) {
        final Vec3 velocity = stateVector.getDelta();
        float vertical = velocity.y;
        vertical = vertical * PREVIOUS_VELOCITY_FACTOR;
        final int level = amplifier + 1;
        final float levelAsFloat = (float) level;
        final float acceleration = levelAsFloat * LEVEL_ACCELERATION;
        vertical = vertical + acceleration;
        velocity.y = vertical;
    }
}
