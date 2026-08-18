package ac.ghost.anticheat.prediction.bds.system.player;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.ActorDataFlag;
import ac.ghost.anticheat.prediction.bds.component.PlayerInputRequestComponent;


public final class SetRequestSprintTriggerSystem {
    private SetRequestSprintTriggerSystem() {
    }

    




    public static void tick(final GhostPlayer player) {
        final PlayerInputRequestComponent request =
                player.entityContext.playerInputRequestComponent;
        request.setSprinting(player.entityContext.actorDataFlagComponent
                .has(ActorDataFlag.SPRINTING));
        request.setSprintCanceled(false);
        request.setStopSprinting(false);
    }
}
