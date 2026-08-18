package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;


public final class FlagPlayersForCollisionSystem {
    private FlagPlayersForCollisionSystem() {
    }

    public static void run(final GhostPlayer player) {
        CollidableMobNotifierSystem.run(player);
    }
}
