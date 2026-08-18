package ac.ghost.anticheat.prediction.bds.system.travel;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Vec3;


public final class TravelMoveRequestSystem {
    private TravelMoveRequestSystem() {
    }

    



    public static void tick(final GhostPlayer player,
                            final Vec3 movementRequest) {
        final Vec3 requested = sanitize(movementRequest);
        player.entityContext.moveRequestComponent.begin(requested, player.entityContext.aabbShapeComponent.getAABB());
    }

    private static Vec3 sanitize(final Vec3 movement) {
        if (movement == null
                || !Float.isFinite(movement.x)
                || !Float.isFinite(movement.y)
                || !Float.isFinite(movement.z)) {
            return Vec3.ZERO.clone();
        }
        return movement.clone();
    }
}
