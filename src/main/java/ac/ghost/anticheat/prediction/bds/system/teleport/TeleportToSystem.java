package ac.ghost.anticheat.prediction.bds.system.teleport;

import ac.ghost.anticheat.prediction.nukkit.NukkitEntityPositionAdapter;
import ac.ghost.anticheat.collision.bds.system.ActorSetPosSystem;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.teleport.data.TeleportData;
import ac.ghost.anticheat.util.math.Vec3;


public final class TeleportToSystem {
    private TeleportToSystem() {
    }

    public static void tick(final GhostPlayer player, final TeleportData data) {
        final Vec3 footTarget = data.getPosition().down(NukkitEntityPositionAdapter.getYOffset(player));
        ActorSetPosSystem.setImmediate(player, footTarget, true);

        if (!data.isKeepVelocity()) {
            player.entityContext.stateVectorComponent.setDelta(Vec3.ZERO.clone());
        }

        TeleportInterpolatorResetSystem.tick(player, data, footTarget);
        player.entityContext.onGroundFlagComponent.setPresent(data.isOnGround());
        player.entityContext.collisionFlagComponent.setPresent(false);
        player.entityContext.horizontalCollisionFlagComponent.setPresent(false);
        player.entityContext.verticalCollisionFlagComponent.setPresent(false);
        player.entityContext.currentlyStandingOnBlockComponent.set(null, null);
        player.entityContext.rewindCollisionShapesComponent = null;
        player.entityContext.moveRequestComponent.clear();
        player.entityContext.depenetrationComponent.setFlags(0);
        player.entityContext.depenetrationComponent.setMagnitude(Vec3.ZERO.clone());
        player.entityContext.depenetrationComponent.collisionBoxes().clear();
        player.entityContext.depenetrationComponent.clearCustomMagnitude();
        player.entityContext.customDepenetrationMagnitudeComponent.clear();
        player.entityContext.actorSetPositionRequestComponent.clear();
        player.entityContext.fallDistanceComponent.setValue(0.0F);
        player.entityContext.forceSendMotionPacketComponent.reset(player);
        player.entityContext.serverPlayerMovementSyncComponent.reset();
        player.getTeleportUtil().acceptPredictedMovementPosition(footTarget);
        player.entityContext.hasTeleportedFlagComponent.setPresent(true);
        player.entityContext.playerPositionModeComponent.setMode(data.getMode());
    }
}
