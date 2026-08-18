package ac.ghost.anticheat.prediction.bds.system.teleport;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.PlayerPositionModeComponent;


public final class TeleportPositionModeEventSystem {
    private TeleportPositionModeEventSystem() {}

    public static void onOutbound(final GhostPlayer player, final int mode) {
        player.entityContext.playerPositionModeComponent.setMode(mode);
        if (mode != PlayerPositionModeComponent.TELEPORT) {
            return;
        }
        player.entityContext.isBeingTeleportedFlagComponent.setPresent(true);
    }
}
