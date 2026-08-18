package ac.ghost.anticheat.prediction.bds.system.travel;

import ac.ghost.anticheat.player.GhostPlayer;


public final class DecrementNoJumpDelaySystem {
    private DecrementNoJumpDelaySystem() {
    }

    public static void tick(final GhostPlayer player) {
        final int delay = player.entityContext.mobJumpComponent.getNoJumpDelay();
        if (delay > 0) {
            player.entityContext.mobJumpComponent.setNoJumpDelay(delay - 1);
        }
    }
}
