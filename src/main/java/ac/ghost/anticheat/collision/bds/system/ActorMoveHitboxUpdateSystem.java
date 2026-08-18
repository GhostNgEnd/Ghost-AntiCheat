package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.prediction.bds.entity.EntityContext;


public final class ActorMoveHitboxUpdateSystem {
    private ActorMoveHitboxUpdateSystem() {
    }

    public static void tick(final EntityContext entity) {
        if (entity.vehicleComponent.isPresent()) {
            return;
        }
        entity.aabbShapeComponent.updateAt(
                entity.stateVectorComponent.getPosition());
    }
}
