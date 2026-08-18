package ac.ghost.anticheat.prediction.bds.system.movement;

import ac.ghost.anticheat.player.GhostPlayer;


public final class CleanupLingeringReplayStateComponents {
    private CleanupLingeringReplayStateComponents() {
    }

    public static void tick(final GhostPlayer player) {
        if (player.entityContext.serverPlayerMovementComponent.isClearHistoryRequested()) {
            player.entityContext.serverPlayerMovementComponent.reset();
            DiscardHistory.tick(player);
            return;
        }
        player.entityContext.serverPlayerMovementComponent.trimToHistorySize(
                player.entityContext.playerMovementSettingsComponent.playerRewindHistorySizeTicks());
    }
}
