package ac.ghost.anticheat.check.api.impl;

import ac.ghost.anticheat.check.api.Check;
import ac.ghost.anticheat.player.GhostPlayer;

public class OffsetHandlerCheck extends Check {
    public OffsetHandlerCheck(GhostPlayer player) {
        super(player);
    }

    public void onPredictionComplete(float offset) {
    }
}
