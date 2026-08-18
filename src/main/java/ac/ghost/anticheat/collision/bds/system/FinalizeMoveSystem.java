package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MoveRequestComponent;
import ac.ghost.anticheat.util.math.Vec3;


public final class FinalizeMoveSystem {
    private FinalizeMoveSystem() {
    }

    public static void run(final GhostPlayer player) {
        final MoveRequestComponent request = player.entityContext.moveRequestComponent;
        final Vec3 submitted = request.movement();
        final Vec3 resolved = request.resolvedMovement();
        final boolean collisionX = Math.abs(submitted.x - resolved.x)
                > ActorMoveSystem.FLOAT_EPSILON;
        final boolean collisionY = Math.abs(submitted.y - resolved.y)
                > ActorMoveSystem.FLOAT_EPSILON;
        final boolean collisionZ = Math.abs(submitted.z - resolved.z)
                > ActorMoveSystem.FLOAT_EPSILON;
        final boolean horizontal = collisionX || collisionZ;
        final boolean collision = horizontal
                || collisionY
                || request.overlapDepth() >= ActorMoveSystem.CONTACT_EPSILON;

        player.entityContext.horizontalCollisionFlagComponent.setPresent(horizontal);
        player.entityContext.verticalCollisionFlagComponent.setPresent(collisionY);
        player.entityContext.collisionFlagComponent.setPresent(collision);

        








        final boolean onGround;
        if (player.entityContext.hasAutoSteppedComponent.isPresent()
                && player.entityContext.onGroundFlagComponent.isPresent()) {
            onGround = true;
        } else if (collisionY) {
            onGround = submitted.y < 0.0F;
        } else {
            onGround = submitted.y == 0.0F && player.entityContext.onGroundFlagComponent.isPresent();
        }
        player.entityContext.onGroundFlagComponent.setPresent(onGround);
    }
}
