package ac.ghost.anticheat.prediction.bds.system.attribute;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MovementAttributesComponent;


public final class UpdateAttributesSystem {
    private UpdateAttributesSystem() {
    }

    public static void tick(final GhostPlayer player,
                            final MovementAttributesComponent attributes) {
        attributes.setMovementSpeed(player.entityContext.attributesComponent.movementSpeed());
    }
}
