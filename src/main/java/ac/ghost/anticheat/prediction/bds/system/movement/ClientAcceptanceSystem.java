package ac.ghost.anticheat.prediction.bds.system.movement;

import ac.ghost.anticheat.player.GhostPlayer;


public final class ClientAcceptanceSystem {
    private ClientAcceptanceSystem() {
    }

    public static void tick(final GhostPlayer player) {
        player.entityContext.clientAcceptanceThresholdsComponent.setPositionThreshold(
                player.entityContext.playerMovementSettingsComponent
                        .playerPositionAcceptanceThreshold());
    }
}
