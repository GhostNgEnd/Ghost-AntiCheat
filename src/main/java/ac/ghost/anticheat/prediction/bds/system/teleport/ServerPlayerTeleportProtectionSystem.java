package ac.ghost.anticheat.prediction.bds.system.teleport;

import ac.ghost.anticheat.prediction.nukkit.NukkitEntityPositionAdapter;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Vec3;





public final class ServerPlayerTeleportProtectionSystem {
    private ServerPlayerTeleportProtectionSystem() {}

    public static void tick(final GhostPlayer player, final Vec3 networkTarget) {
        if (networkTarget == null) {
            player.entityContext.serverPlayerTeleportingFlagComponent.clear();
            return;
        }
        final Vec3 footTarget = networkTarget.down(NukkitEntityPositionAdapter.getYOffset(player));
        player.entityContext.serverPlayerTeleportingFlagComponent.setPresent(
                !player.entityContext.blockSource.isChunkLoadedAt(footTarget.x, footTarget.z));
    }
}
