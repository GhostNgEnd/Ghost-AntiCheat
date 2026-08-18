package ac.ghost.anticheat.prediction.bds.system.movement;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.system.spinattack.RemoveSpinAttackResultsComponentSystem;
import ac.ghost.anticheat.prediction.bds.system.spinattack.SpinAttackFetchNearbyMobsSystem;
import ac.ghost.anticheat.prediction.bds.system.spinattack.StoreSpinAttackResultSystem;


public final class ActorPostAiStepTickSystem {
    private ActorPostAiStepTickSystem() {
    }

    public static void tick(
            final GhostPlayer player,
            final PlayerPreMobTravelStorePositionSystem.Snapshot beforeTravel) {
        if (player.entityContext.riptideTridentSpinAttackComponent.getRemainingTicks() <= 0) {
            return;
        }

        player.entityContext.riptideTridentSpinAttackComponent.decrementRemainingTicks();
        RemoveSpinAttackResultsComponentSystem.tick(player);
        SpinAttackFetchNearbyMobsSystem.tick(
                player, beforeTravel.boundingBox(), player.entityContext.aabbShapeComponent.getAABB());
        StoreSpinAttackResultSystem.tick(player);
    }
}
