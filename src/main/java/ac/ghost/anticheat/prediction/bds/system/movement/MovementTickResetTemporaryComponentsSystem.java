package ac.ghost.anticheat.prediction.bds.system.movement;

import ac.ghost.anticheat.collision.bds.system.RemoveHasAutoSteppedSystem;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.system.block.EntityInsideSystem;
import ac.ghost.anticheat.prediction.bds.system.block.SetEntityInsideSystem;
import ac.ghost.anticheat.prediction.bds.system.block.SweetBerryBushReplayInputSystem;


public final class MovementTickResetTemporaryComponentsSystem {
    private MovementTickResetTemporaryComponentsSystem() {
    }

    public static void tick(final GhostPlayer player) {
        player.ghostMovementBridgeState.cachedOnPos = null;
        player.ghostMovementBridgeState.downwardLiquidEncountered = false;
        player.entityContext.rewindCollisionShapesComponent = null;
        player.entityContext.autoClimbTravelFlagComponent.setPresent(false);
        player.entityContext.autoStepRequestFlagComponent.setPresent(false);
        player.entityContext.collisionFlagComponent.setPresent(false);
        player.entityContext.horizontalCollisionFlagComponent.setPresent(false);
        player.entityContext.verticalCollisionFlagComponent.setPresent(false);
        RemoveHasAutoSteppedSystem.run(player);
        SetEntityInsideSystem.cleanupSystem(player);

        SweetBerryBushReplayInputSystem.tick(player);
        EntityInsideSystem.restoreReplayInput(player);
        EntityInsideSystem.applyReplayMovementSlowdown(player);
    }
}
