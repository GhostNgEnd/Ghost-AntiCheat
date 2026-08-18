package ac.ghost.anticheat.check.api.impl;

import ac.ghost.anticheat.check.api.Check;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.LatencyUtil;

public class PingBasedCheck extends Check {
    public PingBasedCheck(GhostPlayer player) {
        super(player);
    }

    public void onLatencyAccepted(LatencyUtil.Latency latency) {
    }
}
