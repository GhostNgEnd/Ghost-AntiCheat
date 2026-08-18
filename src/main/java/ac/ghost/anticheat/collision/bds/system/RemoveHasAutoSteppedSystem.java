package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;


public final class RemoveHasAutoSteppedSystem {
    private RemoveHasAutoSteppedSystem() {
    }

    public static void run(final GhostPlayer player) {
        player.entityContext.hasAutoSteppedComponent.setPresent(false);
    }
}
