package ac.ghost.anticheat.prediction.bds.system.movement;

import ac.ghost.anticheat.player.GhostPlayer;


public final class DiscardHistory {
    private DiscardHistory() {
    }

    public static void tick(final GhostPlayer player) {
        player.entityContext.replayStateTrackerComponent.discardHistory(player);
    }
}
