package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.RewindCollisionShapesComponent;
import ac.ghost.anticheat.prediction.bds.component.ServerPlayerMovementComponent;
import ac.ghost.anticheat.util.math.Vec3;


public final class CopyCollisionShapesRewindSystem {
    private static final float MAX_ORIGIN_DISTANCE_SQUARED = 4.0F;

    private CopyCollisionShapesRewindSystem() {
    }

    public static void run(final GhostPlayer player) {
        if (!player.entityContext.antiCheatRewindFlagComponent.isPresent()) {
            return;
        }
        final ServerPlayerMovementComponent.HistoryRecord record =
                player.entityContext.serverPlayerMovementComponent.find(
                        player.entityContext.replayStateComponent.getInputTick());
        if (record == null || record.collisionShapes() == null) {
            return;
        }

        final RewindCollisionShapesComponent rewind = record.collisionShapes();
        final Vec3 origin = rewind.origin();
        final Vec3 current = player.entityContext.stateVectorComponent.getPosition();
        final float dx = origin.x - current.x;
        final float dy = origin.y - current.y;
        final float dz = origin.z - current.z;
        if (dx * dx + dy * dy + dz * dz >= MAX_ORIGIN_DISTANCE_SQUARED) {
            return;
        }
        player.entityContext.rewindCollisionShapesComponent = rewind;
    }
}
