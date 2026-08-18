package ac.ghost.anticheat.packets;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.teleport.data.TeleportData;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.plugin.PluginBase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;








public final class MovementPipelineDebugLogger {
    private static final Object FILE_LOCK = new Object();
    private static final String FILE_NAME = "movement-pipeline-debug.log";

    private MovementPipelineDebugLogger() {
    }

    static void log(final GhostPlayer player,
                    final PlayerAuthInputPacket packet,
                    final String stage,
                    final boolean eventCancelled,
                    final String detail) {
        if (player == null || packet == null
                || Ghost.getConfig() == null
                || !Ghost.getConfig().debugMode()) {
            return;
        }

        try {
            writeToFile(buildLine(player, packet, stage, eventCancelled,
                    detail));
        } catch (Throwable throwable) {
            
            final PluginBase plugin = Ghost.getPluginInstance();
            if (plugin != null) {
                plugin.getLogger().warning(
                        "[MovementPipelineDebug] failed to write trace: "
                                + throwable.getClass().getSimpleName() + ": "
                                + throwable.getMessage());
            }
        }
    }

    public static void logCorrectionTransport(final GhostPlayer player,
                                              final long inputTick,
                                              final String transport) {
        if (player == null
                || Ghost.getConfig() == null
                || !Ghost.getConfig().debugMode()) {
            return;
        }

        try {
            final Vec3 position = player.entityContext.stateVectorComponent
                    .getPosition();
            final Vec3 delta = player.entityContext.stateVectorComponent
                    .getDelta();
            writeToFile(new StringBuilder(320)
                    .append("[MovementPipelineDebug]")
                    .append(" time=").append(Instant.now())
                    .append(" player=").append(safeName(player))
                    .append(" protocol=").append(player.getSession().protocol)
                    .append(" stage=correction-send")
                    .append(" detail=").append(sanitize(transport))
                    .append(" packetTick=").append(Math.max(0L, inputTick))
                    .append(" serverBodyPos=").append(vec(position))
                    .append(" stateDelta=").append(vec(delta))
                    .append(" correctConfigured=").append(Ghost.getConfig()
                            .useCorrectPlayerMovePredictionPacket())
                    .append(" hardTeleportingBefore=").append(player
                            .getTeleportUtil().isHardTeleporting())
                    .append(" teleportQueueBefore=").append(player
                            .getTeleportUtil().getQueuedTeleports().size())
                    .toString());
        } catch (Throwable throwable) {
            
        }
    }

    private static String buildLine(final GhostPlayer player,
                                    final PlayerAuthInputPacket packet,
                                    final String stage,
                                    final boolean eventCancelled,
                                    final String detail) {
        final TeleportData teleportHead = player.getTeleportUtil()
                .getQueuedTeleports().peek();
        final Vec3 teleportHeadPosition = teleportHead == null
                ? null : teleportHead.getPosition();
        final Vec3 statePosition = player.entityContext.stateVectorComponent
                .getPosition();
        final Vec3 unvalidatedPosition = player.entityContext
                .serverPlayerCurrentMovementComponent
                .getUnvalidatedPosition();
        final Vec3 previousUnvalidatedPosition = player.entityContext
                .serverPlayerCurrentMovementComponent
                .getPreviousUnvalidatedPosition();

        return new StringBuilder(900)
                .append("[MovementPipelineDebug]")
                .append(" time=").append(Instant.now())
                .append(" player=").append(safeName(player))
                .append(" protocol=").append(player.getSession().protocol)
                .append(" stage=").append(sanitize(stage))
                .append(" detail=").append(sanitize(detail))
                .append(" packetTick=").append(packet.getTick())
                .append(" currentInputTick=").append(player.entityContext
                        .serverPlayerMovementComponent.getCurrentInputTick())
                .append(" eventCancelled=").append(eventCancelled)
                .append(" serverAuthoritative=").append(player.getSession()
                        .isMovementServerAuthoritative())
                .append(" clientNetworkPos=").append(vec(packet.getPosition()))
                .append(" clientDelta=").append(vec(packet.getDelta()))
                .append(" serverBodyPos=").append(vec(statePosition))
                .append(" unvalidatedBodyPos=").append(vec(unvalidatedPosition))
                .append(" previousUnvalidatedBodyPos=")
                .append(vec(previousUnvalidatedPosition))
                .append(" unvalidatedTickEnd=").append(vec(player.entityContext
                        .serverPlayerCurrentMovementComponent
                        .getUnvalidatedTickEnd()))
                .append(" stateDelta=").append(vec(player.entityContext
                        .stateVectorComponent.getDelta()))
                .append(" predictionPresent=").append(player.entityContext
                        .serverPlayerCurrentMovementComponent
                        .getPredictionResult() != null)
                .append(" movementExempted=").append(player.isMovementExempted())
                .append(" sleeping=").append(player.getSession().isSleeping())
                .append(" riding=").append(player.getSession().riding != null)
                .append(" trackedVehicle=").append(player.entityContext
                        .vehicleComponent.value != null)
                .append(" loadingScreenActive=").append(player.entityContext
                        .playerLoadingScreenComponent.active)
                .append(" loadingScreenTicks=").append(player.entityContext
                        .playerLoadingScreenComponent.ticksSinceChange)
                .append(" hardTeleporting=").append(player.getTeleportUtil()
                        .isHardTeleporting())
                .append(" teleporting=").append(player.getTeleportUtil()
                        .isTeleporting())
                .append(" teleportQueue=").append(player.getTeleportUtil()
                        .getQueuedTeleports().size())
                .append(" teleportHeadAccepted=")
                .append(teleportHead != null && teleportHead.isAccepted())
                .append(" teleportHeadNetworkPos=")
                .append(vec(teleportHeadPosition))
                .append(" teleportHeadDistance=")
                .append(teleportHeadDistance(
                        teleportHeadPosition, packet.getPosition()))
                .append(" hasRawMoveVector=").append(player
                        .ghostMovementBridgeState.predictionHasRawMoveVector)
                .append(" hasDigitalDirection=").append(player
                        .ghostMovementBridgeState
                        .predictionHasDigitalDirectionState)
                .append(" inputMode=").append(packet.getInputMode())
                .append(" actions=").append(packet.getInputData())
                .toString();
    }

    private static float teleportHeadDistance(final Vec3 target,
                                              final Vector3f clientPosition) {
        if (target == null || clientPosition == null) {
            return Float.NaN;
        }
        final float dx = clientPosition.getX() - target.x;
        final float dy = clientPosition.getY() - target.y;
        final float dz = clientPosition.getZ() - target.z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static String vec(final Vec3 value) {
        if (value == null) {
            return "null";
        }
        return '(' + Float.toString(value.x) + ',' + Float.toString(value.y)
                + ',' + Float.toString(value.z) + ')';
    }

    private static String vec(final Vector3f value) {
        if (value == null) {
            return "null";
        }
        return '(' + Float.toString(value.getX()) + ','
                + Float.toString(value.getY()) + ','
                + Float.toString(value.getZ()) + ')';
    }

    private static String safeName(final GhostPlayer player) {
        try {
            return sanitize(player.getSession().getName());
        } catch (Throwable ignored) {
            return "unknown";
        }
    }

    private static String sanitize(final String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.replace(' ', '_')
                .replace('\n', '_')
                .replace('\r', '_');
    }

    private static void writeToFile(final String line) throws IOException {
        final PluginBase plugin = Ghost.getPluginInstance();
        if (plugin == null || plugin.getDataFolder() == null) {
            return;
        }
        final Path directory = plugin.getDataFolder().toPath();
        final Path file = directory.resolve(FILE_NAME);
        synchronized (FILE_LOCK) {
            Files.createDirectories(directory);
            Files.write(file,
                    (line + System.lineSeparator())
                            .getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND);
        }
    }
}
