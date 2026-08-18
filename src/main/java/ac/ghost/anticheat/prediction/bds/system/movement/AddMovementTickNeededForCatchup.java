package ac.ghost.anticheat.prediction.bds.system.movement;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.PlayerInputTick;


public final class AddMovementTickNeededForCatchup {
    private AddMovementTickNeededForCatchup() {
    }

    public static void tick(final GhostPlayer player,
                            final long inputTick) {
        player.entityContext.actorMovementTickNeededComponent.set(
                new PlayerInputTick(inputTick));
    }
}
