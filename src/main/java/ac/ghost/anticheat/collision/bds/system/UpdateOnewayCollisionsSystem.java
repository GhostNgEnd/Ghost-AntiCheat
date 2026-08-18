package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Box;


public final class UpdateOnewayCollisionsSystem {
    private UpdateOnewayCollisionsSystem() {
    }

    public static void run(final GhostPlayer player) {
        final Box actor = player.entityContext.moveRequestComponent.originalAABB();
        player.entityContext.depenetrationComponent.collisionBoxes().removeIf(
                saved -> saved == null || !saved.intersects(actor));
    }
}
