package ac.ghost.anticheat.prediction.bds.system.movement;

import ac.ghost.anticheat.prediction.nukkit.NukkitEntityPositionAdapter;
import ac.ghost.anticheat.check.api.impl.OffsetHandlerCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.PredictedMovementComponent;
import ac.ghost.anticheat.prediction.nukkit.NukkitPlayerTickAdapter;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.types.AuthInputAction;

public final class TickServerPlayerMovementFinalSystem {
    private TickServerPlayerMovementFinalSystem() {
    }

    public static void updateUnvalidatedPosition(
            final GhostPlayer player,
            final PlayerAuthInputPacket packet) {
        player.entityContext.serverPlayerCurrentMovementComponent.setPreviousUnvalidatedPosition(
                player.entityContext.serverPlayerCurrentMovementComponent
                        .getUnvalidatedPosition().clone());
        final Vector3f pos = packet.getPosition();
        player.entityContext.serverPlayerCurrentMovementComponent.setUnvalidatedPosition(
                new Vec3(pos.getX(), pos.getY() - NukkitEntityPositionAdapter.getYOffset(player), pos.getZ()));
        player.entityContext.serverPlayerCurrentMovementComponent.setPreviousUnvalidatedTickEnd(
                player.entityContext.serverPlayerCurrentMovementComponent
                        .getUnvalidatedTickEnd());
        player.entityContext.serverPlayerCurrentMovementComponent.setUnvalidatedTickEnd(
                new Vec3(packet.getDelta()));
    }

    public static void tick(
            final GhostPlayer player,
            final PlayerAuthInputPacket packet) {
        NukkitPlayerTickAdapter.afterMovement(player);
        if (player.isExempted()) {
            player.entityContext.serverPlayerMovementSyncComponent.finishCorrectionTick();
            player.entityContext.serverPlayerMovementSyncComponent.clearCorrectionState();
            player.entityContext.forceSendMotionPacketComponent.clear();
            CleanupLingeringReplayStateComponents.tick(player);
            player.entityContext.stateVectorComponent.setPreviousPosition(
                    player.entityContext.stateVectorComponent.getPosition());
            return;
        }

        final long processedInputTick = player.entityContext.serverPlayerMovementComponent
                .getProcessingInputTick() == null
                ? player.entityContext.serverPlayerMovementComponent.getCurrentInputTick()
                : player.entityContext.serverPlayerMovementComponent
                        .getProcessingInputTick().value();

        final PredictedMovementComponent predictedMovement =
                PredictedMovementSystem.tick(player, processedInputTick);
        player.getTeleportUtil().acceptPredictedMovementPosition(
                player.entityContext.stateVectorComponent.getPosition().clone());
        ValidateClientPredictionSystem.tick(player, predictedMovement);

        final float offset = player.entityContext.stateVectorComponent.getPosition().distanceTo(
                player.entityContext.serverPlayerCurrentMovementComponent.getUnvalidatedPosition());

        








        ClientAcceptanceSystem.tick(player);
        final TickServerPlayerMovementCorrection.Result correctionResult =
                TickServerPlayerMovementCorrection.tick(player);
        player.ghostMovementBridgeState.debugBdsCorrectionResult = correctionResult.name();

        if (correctionResult == TickServerPlayerMovementCorrection.Result.UNRESOLVED_NATIVE_POLICY) {
            for (final var check : player.getCheckHolder().values()) {
                if (check instanceof OffsetHandlerCheck offsetCheck) {
                    offsetCheck.onPredictionComplete(offset);
                }
            }

            






            if (player.entityContext.serverPlayerMovementSyncComponent.correctionRequested()) {
                TickServerPlayerMovementCorrection.tick(player);
            }
        }
        player.entityContext.forceSendMotionPacketComponent.clear();
        CleanupLingeringReplayStateComponents.tick(player);

        






        writeAuthoritativeStateToPacket(player, packet);
        player.entityContext.stateVectorComponent.setPreviousPosition(
                player.entityContext.stateVectorComponent.getPosition());
    }

    public static void writeAuthoritativeStateToPacket(
            final GhostPlayer player,
            final PlayerAuthInputPacket packet) {
        if (player.isMovementExempted()) {
            return;
        }

        
        
        packet.getInputData().remove(AuthInputAction.HORIZONTAL_COLLISION);
        packet.getInputData().remove(AuthInputAction.VERTICAL_COLLISION);

        if (player.entityContext.horizontalCollisionFlagComponent.isPresent()) {
            packet.getInputData().add(AuthInputAction.HORIZONTAL_COLLISION);
        }
        if (player.entityContext.verticalCollisionFlagComponent.isPresent()) {
            packet.getInputData().add(AuthInputAction.VERTICAL_COLLISION);
        }

        packet.setDelta(player.entityContext.stateVectorComponent.getDelta().toVector3f());
    }
}
