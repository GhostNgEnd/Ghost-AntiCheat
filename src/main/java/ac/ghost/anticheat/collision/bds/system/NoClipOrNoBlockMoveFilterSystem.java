package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MoveRequestComponent;


public final class NoClipOrNoBlockMoveFilterSystem {
    private NoClipOrNoBlockMoveFilterSystem() {
    }

    public static boolean run(final GhostPlayer player) {
        final boolean noClip = player.entityContext.movementAbilitiesComponent.isNoClip();
        final MoveRequestComponent request = player.entityContext.moveRequestComponent;
        if (noClip) {
            request.setOrdinaryResult(
                    request.movement(),
                    request.originalAABB().offset(request.movement()));
            request.setResolvedResult(
                    request.movement(),
                    request.originalAABB().offset(request.movement()));
            request.setCollisionResponse(false);
        }
        return noClip;
    }
}
