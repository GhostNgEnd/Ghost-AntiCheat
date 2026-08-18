package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.RewindCollisionShapesComponent;
import ac.ghost.anticheat.prediction.bds.component.ServerPlayerMovementComponent;


public final class CollisionShapesCopySystem {
    private CollisionShapesCopySystem() {
    }

    public static void run(final GhostPlayer player) {
        final RewindCollisionShapesComponent shapes =
                player.entityContext.rewindCollisionShapesComponent;
        if (shapes == null || shapes.sourceTick() != player.entityContext.serverPlayerMovementComponent.getCurrentInputTick()) {
            return;
        }
        final ServerPlayerMovementComponent.HistoryRecord record =
                player.entityContext.serverPlayerMovementComponent.processingRecord();
        if (record != null) {
            record.setCollisionShapes(shapes);
        }
    }
}
