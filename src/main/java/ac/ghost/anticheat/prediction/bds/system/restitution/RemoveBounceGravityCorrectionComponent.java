package ac.ghost.anticheat.prediction.bds.system.restitution;

import ac.ghost.anticheat.player.GhostPlayer;


public final class RemoveBounceGravityCorrectionComponent {
    private RemoveBounceGravityCorrectionComponent() {
    }

    public static void tick(final GhostPlayer player) {
        player.entityContext.bounceGravityCorrection = null;
    }
}
