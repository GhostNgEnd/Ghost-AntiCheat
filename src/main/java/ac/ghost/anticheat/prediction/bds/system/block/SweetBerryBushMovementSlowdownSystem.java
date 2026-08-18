package ac.ghost.anticheat.prediction.bds.system.block;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Vec3;


public final class SweetBerryBushMovementSlowdownSystem {
    private static final Vec3 MULTIPLIER = new Vec3(0.8F, 0.75F, 0.8F);

    private SweetBerryBushMovementSlowdownSystem() {
    }

    public static void tick(final GhostPlayer player) {
        if (!player.entityContext.insideSlowingSweetBerryBushBlockComponent.isPresent()) {
            return;
        }
        player.entityContext.blockMovementSlowdownMultiplierComponent.add(MULTIPLIER);
        player.entityContext.blockMovementSlowdownAppliedComponent.markApplied();
    }
}
