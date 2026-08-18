package ac.ghost.anticheat.prediction.bds.system.liquid.lava;

import ac.ghost.anticheat.player.GhostPlayer;

public final class LavaResetFallDistanceSystem {
    private LavaResetFallDistanceSystem() {
    }

    public static void tick(final GhostPlayer player) {
        if ((player.entityContext.serverPlayerMovementComponent.getCurrentInputTick() != 1L && player.ghostMovementBridgeState.lavaSample.touching())) {
            player.entityContext.fallDistanceComponent.setValue(0.0F);
        }
    }
}
