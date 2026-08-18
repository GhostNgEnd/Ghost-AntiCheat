package ac.ghost.anticheat.prediction.bds.system.spinattack;

import ac.ghost.anticheat.player.GhostPlayer;


public final class RemoveSpinAttackResultsComponentSystem {
    private RemoveSpinAttackResultsComponentSystem() {
    }

    public static void tick(final GhostPlayer player) {
        player.entityContext.spinAttackResultsComponent.clear();
    }
}
