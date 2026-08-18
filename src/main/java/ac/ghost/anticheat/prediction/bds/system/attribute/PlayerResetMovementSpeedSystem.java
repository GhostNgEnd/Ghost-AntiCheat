package ac.ghost.anticheat.prediction.bds.system.attribute;

import ac.ghost.anticheat.prediction.bds.component.MovementAttributesComponent;
import ac.ghost.anticheat.prediction.bds.component.MovementSpeedComponent;


public final class PlayerResetMovementSpeedSystem {
    private PlayerResetMovementSpeedSystem() {
    }

    public static void tick(final MovementAttributesComponent attributes,
                            final MovementSpeedComponent movementSpeed) {
        movementSpeed.setValue(attributes.hasMovementSpeed()
                ? attributes.getMovementSpeed()
                : 0.0F);
    }
}
