package ac.ghost.anticheat.prediction.bds.system.movement;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.protocol.BedrockProtocolCapabilities;
import ac.ghost.anticheat.packets.MovementPipelineDebugLogger;
import ac.ghost.anticheat.prediction.nukkit.NukkitEntityPositionAdapter;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.NukkitAdapter;
import ac.ghost.anticheat.prediction.bds.component.ServerPlayerMovementSyncComponent;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.Vector2f;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.CorrectPlayerMovePredictionPacket;


public final class TickServerPlayerMovementCorrection {
    public enum Result {
        SKIPPED,
        ACCEPTED_CLIENT_POSITION,
        FULL_CORRECTION,
        UNRESOLVED_NATIVE_POLICY
    }

    private static final float ACCEPTED_POSITION_DEPENETRATION_Y = 0.05F;

    private TickServerPlayerMovementCorrection() {
    }

    public static Result tick(final GhostPlayer player) {
        final ServerPlayerMovementSyncComponent sync =
                player.entityContext.serverPlayerMovementSyncComponent;
        try {
            if (player.getTeleportUtil().isTeleporting()
                    || player.entityContext.unloadedChunkTimerComponent.insideUnloadedChunk) {
                return Result.SKIPPED;
            }

            final Vec3 authoritative = player.entityContext.stateVectorComponent.getPosition();
            final Vec3 unvalidated = player.entityContext.serverPlayerCurrentMovementComponent
                    .getUnvalidatedPosition();
            final float distanceSquared = positionDistanceSquared(
                    authoritative, unvalidated);
            if (!Float.isFinite(distanceSquared)) {
                return Result.SKIPPED;
            }

            
            
            if (player.entityContext.clientAcceptanceThresholdsComponent
                    .positionThresholdEnabled()
                    && player.entityContext.clientAcceptanceThresholdsComponent
                    .positionThresholdSquared() >= distanceSquared) {
                acceptClientPosition(player, unvalidated);
                sync.clearCorrectionState();
                return Result.ACCEPTED_CLIENT_POSITION;
            }

            
            if (sync.correctionRequested()) {
                sync.clearCorrectionState();
                sendFullCorrection(player, sync.clientBoundPacketTick());
                return Result.FULL_CORRECTION;
            }

            






            return Result.UNRESOLVED_NATIVE_POLICY;
        } finally {
            
            sync.finishCorrectionTick();
            player.entityContext.clientAcceptanceThresholdsComponent
                    .setPositionThresholdEnabled(false);
        }
    }

    private static float positionDistanceSquared(
            final Vec3 authoritative,
            final Vec3 unvalidated) {
        final float dx = authoritative.x - unvalidated.x;
        final float dy = authoritative.y - unvalidated.y;
        final float dz = authoritative.z - unvalidated.z;
        return dx * dx + dy * dy + dz * dz;
    }

    private static void acceptClientPosition(
            final GhostPlayer player,
            final Vec3 clientPosition) {
        player.entityContext.actorSetPositionRequestComponent.set(clientPosition);

        final Vec3 existing = player.entityContext.depenetrationComponent.useCustomMagnitude()
                ? player.entityContext.depenetrationComponent.customMagnitude()
                : Vec3.ZERO;
        player.entityContext.depenetrationComponent.setCustomMagnitude(new Vec3(
                Math.max(existing.x, 0.0F),
                Math.max(existing.y, ACCEPTED_POSITION_DEPENETRATION_Y),
                Math.max(existing.z, 0.0F)));
    }

    private static void sendFullCorrection(
            final GhostPlayer player,
            final long inputTick) {
        if (!Ghost.getConfig().useCorrectPlayerMovePredictionPacket()
                || !player.getSession().isMovementServerAuthoritative()
                || !BedrockProtocolCapabilities
                .supportsCorrectPlayerMovePredictionSetback(
                player.getSession().protocol)) {
            MovementPipelineDebugLogger.logCorrectionTransport(
                    player, inputTick, "move-player-hard");
            player.getTeleportUtil().correctMovement(
                    player.entityContext.stateVectorComponent.getPosition(),
                    player.entityContext.stateVectorComponent.getDelta(),
                    player.entityContext.onGroundFlagComponent.isPresent(),
                    inputTick);
            return;
        }

        final CorrectPlayerMovePredictionPacket packet =
                new CorrectPlayerMovePredictionPacket();

        final Vec3 packetPosition = player.entityContext.stateVectorComponent.getPosition()
                .add(0.0F, NukkitEntityPositionAdapter.getYOffset(player), 0.0F);
        packet.setPosition(vector(packetPosition));
        packet.setDelta(vector(player.entityContext.stateVectorComponent.getDelta()));
        packet.setOnGround(player.entityContext.onGroundFlagComponent.isPresent());
        packet.setTick(Math.max(0L, inputTick));

        final boolean vehicle = player.entityContext.vehicleComponent.value != null;
        packet.setVehicleRotation(vehicle
                ? new Vector2f(player.entityContext.actorRotationComponent.getPitch(),
                player.entityContext.actorRotationComponent.getYaw())
                : new Vector2f(0.0F, 0.0F));
        packet.setPredictionType(vehicle
                ? CorrectPlayerMovePredictionPacket.PredictionType.VEHICLE
                : CorrectPlayerMovePredictionPacket.PredictionType.PLAYER);

        MovementPipelineDebugLogger.logCorrectionTransport(
                player, inputTick, "correct-player-prediction");
        NukkitAdapter.getPlayer(player).dataPacket(packet);
    }

    private static Vector3f vector(final Vec3 value) {
        return new Vector3f(value.x, value.y, value.z);
    }
}
