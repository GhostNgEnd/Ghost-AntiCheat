package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Vec3;


public final class ActorSetPosSystem {
    private ActorSetPosSystem() {
    }

    public static void run(final GhostPlayer player) {
        if (!player.entityContext.actorSetPositionRequestComponent.isPresent()) {
            return;
        }
        setImmediate(player, player.entityContext.actorSetPositionRequestComponent.position(), false);
        player.entityContext.actorSetPositionRequestComponent.clear();
    }

    public static void setImmediate(final GhostPlayer player,
                                    final Vec3 position,
                                    final boolean updatePreviousPosition) {
        if (updatePreviousPosition) {
            player.entityContext.stateVectorComponent.setPreviousPosition(
                    player.entityContext.stateVectorComponent.getPosition());
        }
        player.entityContext.stateVectorComponent.setPosition(position);
        ActorMoveHitboxUpdateSystem.tick(player.entityContext);
    }
}
