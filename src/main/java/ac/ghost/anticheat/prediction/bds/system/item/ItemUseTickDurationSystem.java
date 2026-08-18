package ac.ghost.anticheat.prediction.bds.system.item;

import ac.ghost.anticheat.player.GhostPlayer;


public final class ItemUseTickDurationSystem {
    private ItemUseTickDurationSystem() {
    }

    public static void tick(final GhostPlayer player) {
        if (!player.entityContext.itemInUseComponent.isPresent()) {
            return;
        }
        final long serverTick = player.getSession().getServer().getTick();
        if (player.entityContext.itemInUseTicksDuringMovementComponent.wasTicked(serverTick)) {
            return;
        }
        ItemUseTickDurationMovementSystem.tickUse(player);
        player.entityContext.itemInUseTicksDuringMovementComponent.mark(serverTick);
    }
}
