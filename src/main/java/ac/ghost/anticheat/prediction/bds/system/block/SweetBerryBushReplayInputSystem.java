package ac.ghost.anticheat.prediction.bds.system.block;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.ServerPlayerMovementComponent;


public final class SweetBerryBushReplayInputSystem {
    private SweetBerryBushReplayInputSystem() {
    }

    public static void tick(final GhostPlayer player) {
        if (!player.entityContext.antiCheatRewindFlagComponent.isPresent()) {
            return;
        }
        final ServerPlayerMovementComponent.HistoryRecord record =
                player.entityContext.serverPlayerMovementComponent.find(
                        player.entityContext.replayStateComponent.getInputTick());
        if (record != null && record.insideSlowingSweetBerryBush()) {
            player.entityContext.insideSlowingSweetBerryBushBlockComponent.markPresent();
        }
    }
}
