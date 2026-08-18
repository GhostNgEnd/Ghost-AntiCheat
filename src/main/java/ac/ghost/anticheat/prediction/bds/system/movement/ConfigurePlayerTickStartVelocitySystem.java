package ac.ghost.anticheat.prediction.bds.system.movement;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.PlayerTickStartVelocityComponent;


public final class ConfigurePlayerTickStartVelocitySystem {
    private ConfigurePlayerTickStartVelocitySystem() {
    }

    public static PlayerTickStartVelocityComponent.Candidates tick(
            final GhostPlayer player) {
        final PlayerTickStartVelocityComponent.Candidates candidates =
                player.entityContext.playerTickStartVelocityComponent.begin(
                        player.entityContext.stateVectorComponent.getDelta());
        player.entityContext.stateVectorComponent.setDelta(
                candidates.ordinary().velocity());
        return candidates;
    }
}
