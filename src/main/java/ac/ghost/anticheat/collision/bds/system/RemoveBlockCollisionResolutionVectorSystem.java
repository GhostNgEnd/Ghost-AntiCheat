package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;


public final class RemoveBlockCollisionResolutionVectorSystem {
    private RemoveBlockCollisionResolutionVectorSystem() {}
    public static void run(final GhostPlayer player) {
        player.entityContext.blockCollisionResolutionVectorComponent.clear();
    }
}
