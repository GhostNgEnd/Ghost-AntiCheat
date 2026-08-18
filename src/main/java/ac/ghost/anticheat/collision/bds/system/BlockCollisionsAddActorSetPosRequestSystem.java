package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;


public final class BlockCollisionsAddActorSetPosRequestSystem {
    private BlockCollisionsAddActorSetPosRequestSystem() {}
    public static void run(final GhostPlayer player) {
        if (!player.entityContext.blockCollisionResolutionVectorComponent.isPresent()) return;
        player.entityContext.actorSetPositionRequestComponent.set(
                player.entityContext.stateVectorComponent.getPosition().add(
                        player.entityContext.blockCollisionResolutionVectorComponent.value()));
    }
}
