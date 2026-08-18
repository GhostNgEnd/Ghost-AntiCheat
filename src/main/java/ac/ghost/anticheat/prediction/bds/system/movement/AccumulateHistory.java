package ac.ghost.anticheat.prediction.bds.system.movement;

import ac.ghost.anticheat.player.GhostPlayer;


public final class AccumulateHistory {
    private AccumulateHistory() {
    }

    public static void tick(final GhostPlayer player) {
        player.entityContext.replayStateTrackerComponent.accumulate(player);
    }
}
