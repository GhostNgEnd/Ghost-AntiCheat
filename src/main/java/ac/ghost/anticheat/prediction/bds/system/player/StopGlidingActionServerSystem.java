package ac.ghost.anticheat.prediction.bds.system.player;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.ActorDataFlag;
import ac.ghost.anticheat.prediction.bds.component.PlayerActionComponent;


public final class StopGlidingActionServerSystem {
    private StopGlidingActionServerSystem() {
    }

    public static void tick(final GhostPlayer player) {
        if (player.entityContext.playerActionComponent.has(PlayerActionComponent.STOP_GLIDING)) {
            player.entityContext.actorDataFlagComponent.set(ActorDataFlag.GLIDING, false);
        }
    }
}
