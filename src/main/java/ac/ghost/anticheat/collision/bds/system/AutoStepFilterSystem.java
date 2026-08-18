package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MoveRequestComponent;
import ac.ghost.anticheat.util.math.Vec3;


public final class AutoStepFilterSystem {
    private AutoStepFilterSystem() {
    }

    public static void run(final GhostPlayer player) {
        final MoveRequestComponent request = player.entityContext.moveRequestComponent;
        final Vec3 submitted = request.movement();
        final Vec3 ordinary = request.ordinaryMovement();
        final boolean horizontalCollision = submitted.x != ordinary.x
                || submitted.z != ordinary.z;
        final boolean downwardVerticalCollision = submitted.y < 0.0F
                && submitted.y != ordinary.y;
        final boolean enabled = player.entityContext.maxAutoStepComponent.value() > 0.0F
                && horizontalCollision
                && (player.entityContext.canAlwaysAutoStepFlagComponent.isPresent()
                || player.entityContext.onGroundFlagComponent.isPresent()
                || downwardVerticalCollision);
        player.entityContext.autoStepRequestFlagComponent.setPresent(enabled);
    }
}
