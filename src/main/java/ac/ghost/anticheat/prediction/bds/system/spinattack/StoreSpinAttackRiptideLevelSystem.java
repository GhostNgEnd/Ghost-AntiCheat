package ac.ghost.anticheat.prediction.bds.system.spinattack;

import ac.ghost.anticheat.player.GhostPlayer;






public final class StoreSpinAttackRiptideLevelSystem {
    private StoreSpinAttackRiptideLevelSystem() {
    }

    public static void tick(final GhostPlayer player) {
        if (!player.entityContext.serverPlayerInventoryTransactionComponent.hasPendingRiptide()) {
            return;
        }

        
        
        final int level = player.entityContext.serverPlayerInventoryTransactionComponent.consumePendingRiptideLevel();

        if (level <= 0) {
            player.entityContext.riptideTridentSpinAttackComponent.clear();
            return;
        }

        player.entityContext.riptideTridentSpinAttackComponent.setRiptideLevel(level);
    }
}
