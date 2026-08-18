package ac.ghost.anticheat.prediction.bds.system.input;

import ac.ghost.anticheat.player.GhostPlayer;
















public final class PlayerInputFilterServerSystem {
    private PlayerInputFilterServerSystem() {
    }

    
    public static void tick(final Iterable<? extends GhostPlayer> entities) {
        if (entities == null) {
            return;
        }
        for (GhostPlayer player : entities) {
            applyIfMatched(player);
        }
    }

    
    public static void tick(final GhostPlayer player) {
        applyIfMatched(player);
    }

    
    public static void onEntityChanged(final GhostPlayer player) {
        applyIfMatched(player);
    }

    private static void applyIfMatched(final GhostPlayer player) {
        if (!matchesQuery(player)) {
            return;
        }
        player.entityContext.playerInputRequestComponent.resetToServerDefaults();
    }

    private static boolean matchesQuery(final GhostPlayer player) {
        return player != null
                && player.entityContext.actorMovementTickNeededComponent != null
                && player.entityContext.actorMovementTickNeededComponent.isPresent()
                && player.entityContext.serverPlayerCurrentMovementComponent != null
                && player.entityContext.playerInputRequestComponent != null;
    }
}
