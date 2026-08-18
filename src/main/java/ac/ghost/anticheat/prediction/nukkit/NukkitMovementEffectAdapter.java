package ac.ghost.anticheat.prediction.nukkit;

import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.item.ItemFirework;


public final class NukkitMovementEffectAdapter {
    private NukkitMovementEffectAdapter() {
    }

    



    public static void beginGlideBoost(final GhostPlayer player,
                                       final ItemFirework firework) {
        final int flight = Math.max(1, firework.getFlight());
        final int provisionalMaxLifetime = 10 * (flight + 1) + 11;
        player.ghostMovementBridgeState.nukkitGlideBoostPending = true;
        player.entityContext.movementEffectsComponent.glideBoostTicks = Math.max(
                player.entityContext.movementEffectsComponent.glideBoostTicks,
                provisionalMaxLifetime);
    }
}
