package ac.ghost.anticheat.prediction.nukkit;

import ac.ghost.anticheat.compensated.cache.entity.EntityCache;
import ac.ghost.anticheat.player.GhostPlayer;







public final class NukkitEntityPositionAdapter {
    private NukkitEntityPositionAdapter() {
    }

    public static float getYOffset(final GhostPlayer player) {
        if (player.entityContext.vehicleComponent.value != null) {
            final EntityCache cache = player.entityRegistry.getEntity(
                    player.entityContext.vehicleComponent.value.vehicleRuntimeId);
            if (cache != null && cache.isBoat()) {
                
                
                return 0.375F;
            }

            return 0;
        }

        
        return 1.62001F;
    }
}
