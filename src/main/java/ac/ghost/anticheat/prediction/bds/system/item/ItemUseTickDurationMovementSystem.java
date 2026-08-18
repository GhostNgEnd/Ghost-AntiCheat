package ac.ghost.anticheat.prediction.bds.system.item;

import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.item.ItemID;


public final class ItemUseTickDurationMovementSystem {
    private ItemUseTickDurationMovementSystem() {
    }

    public static void tick(final GhostPlayer player) {
        if (!player.entityContext.itemInUseComponent.isPresent()) {
            return;
        }
        final long serverTick = player.getSession().getServer().getTick();
        if (player.entityContext.itemInUseTicksDuringMovementComponent.wasTicked(serverTick)) {
            return;
        }

        tickUse(player);
        player.entityContext.itemInUseTicksDuringMovementComponent.mark(serverTick);
    }

    static void tickUse(final GhostPlayer player) {
        player.entityContext.itemInUseComponent.tickDuration();
        if (player.entityContext.itemInUseComponent.getItemId() == ItemID.TRIDENT) {
            player.entityContext.itemInUseComponent.incrementTridentUseTicks();
        }
    }
}
