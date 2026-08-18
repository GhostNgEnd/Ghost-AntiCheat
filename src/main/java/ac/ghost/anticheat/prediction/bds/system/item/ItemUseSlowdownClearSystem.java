package ac.ghost.anticheat.prediction.bds.system.item;

import ac.ghost.anticheat.player.GhostPlayer;


public final class ItemUseSlowdownClearSystem {
    private ItemUseSlowdownClearSystem() {
    }

    public static void tick(final GhostPlayer player) {
        player.entityContext.itemUseSlowdownModifierComponent.clear();
    }
}
