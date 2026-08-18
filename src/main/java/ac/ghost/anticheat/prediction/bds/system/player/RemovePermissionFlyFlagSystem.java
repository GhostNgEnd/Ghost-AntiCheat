package ac.ghost.anticheat.prediction.bds.system.player;

import ac.ghost.anticheat.player.GhostPlayer;


public final class RemovePermissionFlyFlagSystem {
    private RemovePermissionFlyFlagSystem() {
    }

    public static void tick(final GhostPlayer player) {
        player.entityContext.permissionFlyFlagComponent.clear();
    }
}
