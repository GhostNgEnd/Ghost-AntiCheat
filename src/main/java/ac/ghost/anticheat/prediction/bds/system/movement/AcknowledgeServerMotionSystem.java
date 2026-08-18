package ac.ghost.anticheat.prediction.bds.system.movement;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Vec3;


public final class AcknowledgeServerMotionSystem {
    private AcknowledgeServerMotionSystem() {
    }

    public static void uncertain(final GhostPlayer player,
                                 final Vec3 velocity) {
        player.entityContext.playerTickStartVelocityComponent
                .acknowledgeUncertain(velocity);
    }

    public static void promote(final GhostPlayer player) {
        player.entityContext.playerTickStartVelocityComponent
                .promoteUncertain();
    }
}
