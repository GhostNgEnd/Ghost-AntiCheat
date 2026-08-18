package ac.ghost.anticheat.prediction.bds.system.player;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.PlayerActionComponent;
import ac.ghost.anticheat.prediction.bds.component.PlayerInputRequestComponent;


public final class IntentSprintTriggerSystem {
    private IntentSprintTriggerSystem() {
    }

    



    public static void tick(final GhostPlayer player) {
        final PlayerActionComponent actions = player.entityContext.playerActionComponent;
        final PlayerInputRequestComponent request =
                player.entityContext.playerInputRequestComponent;

        if (actions.has(PlayerActionComponent.START_SPRINTING)) {
            request.setSprinting(true);
        }
        if (actions.has(PlayerActionComponent.STOP_SPRINTING)) {
            request.setSprinting(false);
            request.setStopSprinting(true);
        }
    }
}
