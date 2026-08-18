package ac.ghost.anticheat.prediction.nukkit;

import ac.ghost.anticheat.compensated.cache.entity.EntityCache;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.system.effect.TickMobEffectsSystem;
import ac.ghost.anticheat.prediction.bds.system.item.ItemUseTickDurationSystem;
import ac.ghost.anticheat.prediction.nukkit.system.NukkitItemUseStateSystem;







public final class NukkitPlayerTickAdapter {
    private NukkitPlayerTickAdapter() {
    }

    public static void onAcceptedAuthInput(final GhostPlayer player) {
        TickMobEffectsSystem.tick(player);

        try {
            for (final EntityCache cache : player.entityRegistry.entities().values()) {
                if (cache.getPast() != null) {
                    cache.getPast().tick();
                }

                if (cache.getCurrent() != null) {
                    cache.getCurrent().tick();
                }
            }
        } catch (Exception ignored) {
        }

        ItemUseTickDurationSystem.tick(player);
    }

    public static void afterMovement(final GhostPlayer player) {
        player.entityContext.movementEffectsComponent.glideBoostTicks--;
        if (player.ghostDebugState.pistonTicks > 0) {
            player.ghostDebugState.pistonTicks--;
        }
        if (player.ghostDebugState.lavaTicks > 0) {
            player.ghostDebugState.lavaTicks--;
        }
        NukkitItemUseStateSystem.postTick(player);
    }
}
