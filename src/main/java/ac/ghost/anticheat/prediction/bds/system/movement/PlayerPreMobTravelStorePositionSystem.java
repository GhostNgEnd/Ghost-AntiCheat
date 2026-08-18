package ac.ghost.anticheat.prediction.bds.system.movement;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;


public final class PlayerPreMobTravelStorePositionSystem {
    private PlayerPreMobTravelStorePositionSystem() {
    }

    public static Snapshot tick(final GhostPlayer player) {
        return new Snapshot(player.entityContext.stateVectorComponent.getPosition().clone(), player.entityContext.aabbShapeComponent.getAABB().clone());
    }

    public record Snapshot(Vec3 position,
                           Box boundingBox) {
    }
}
