package ac.ghost.anticheat.prediction.bds.system.restitution.ApplyRestitutionSystem;

import ac.ghost.anticheat.player.GhostPlayer;


public final class RemoveApplyRestitutionComponent {
    private RemoveApplyRestitutionComponent() {
    }

    public static void tick(final GhostPlayer player) {
        player.entityContext.applyRestitutionComponent = null;
    }
}
