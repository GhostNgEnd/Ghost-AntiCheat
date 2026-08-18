package ac.ghost.anticheat.prediction.bds.system.liquid.lava;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Vec3;


public final class LavaTravelSystem {
    private LavaTravelSystem() {
    }

    public static Vec3 tick(final GhostPlayer player, final Vec3 velocity) {
        return LavaMoveSystem.tick(
                player, velocity, player.entityContext.attributesComponent.lavaMovementSpeed());
    }
}
