package ac.ghost.anticheat.prediction.bds.system.spinattack;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.SpinAttackResultsComponent;
import ac.ghost.anticheat.prediction.bds.system.movement.UpdateHorizontalPoseSystem;
import ac.ghost.anticheat.prediction.bds.system.movement.PlayerBoundingBoxStateUpdateSystem;
import cn.nukkit.entity.Entity;


public final class StoreSpinAttackResultSystem {
    private StoreSpinAttackResultSystem() {
    }

    public static void tick(final GhostPlayer player) {
        final SpinAttackResultsComponent result = player.entityContext.spinAttackResultsComponent;
        if (result.hitNearbyMob()) {
            player.entityContext.riptideTridentSpinAttackComponent.setStopRequested(true);
        } else if (!result.hasCandidateEntities() && result.hasHorizontalCollision()) {
            stop(player);
            return;
        }

        if (player.entityContext.riptideTridentSpinAttackComponent.getRemainingTicks() <= 0) {
            stop(player);
        }
    }

    public static void stop(final GhostPlayer player) {
        player.entityContext.serverPlayerInventoryTransactionComponent.clearPendingRiptide();
        player.entityContext.riptideTridentSpinAttackComponent.clear();
        player.entityContext.spinAttackResultsComponent.clear();
        player.entityContext.shouldUpdateBoundingBoxRequestComponent.clear();
        player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_SPIN_ATTACK, false);
        UpdateHorizontalPoseSystem.tick(player.entityContext);
        PlayerBoundingBoxStateUpdateSystem.tick(player.entityContext);
    }
}
