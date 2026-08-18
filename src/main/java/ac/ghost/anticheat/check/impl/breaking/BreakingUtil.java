package ac.ghost.anticheat.check.impl.breaking;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.types.AuthInputAction;
import cn.nukkit.network.protocol.types.GameType;
import cn.nukkit.network.protocol.types.InputMode;
import cn.nukkit.network.protocol.types.PlayerActionType;
import cn.nukkit.network.protocol.types.PlayerBlockActionData;
import cn.nukkit.network.protocol.v113.RemoveBlockPacket_v113;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BreakingUtil {
    private static final float EYE_UNCERTAINTY = 0.15F;

    private BreakingUtil() {
    }

    enum Kind {
        START,
        CONTINUE,
        FINISH,
        CANCEL
    }

    static Kind kind(final PlayerActionType type) {
        if (type == PlayerActionType.START_DESTROY_BLOCK) {
            return Kind.START;
        }
        if (type == PlayerActionType.CONTINUE_DESTROY_BLOCK) {
            return Kind.CONTINUE;
        }
        if (type == PlayerActionType.PREDICT_DESTROY_BLOCK) {
            return Kind.FINISH;
        }
        if (type == PlayerActionType.ABORT_DESTROY_BLOCK
                || type == PlayerActionType.STOP_DESTROY_BLOCK) {
            return Kind.CANCEL;
        }
        return null;
    }

    static Kind legacyKind(final int action) {
        if (action == PlayerActionPacket.ACTION_START_BREAK) {
            return Kind.START;
        }
        if (action == PlayerActionPacket.ACTION_CONTINUE_BREAK
                || action == PlayerActionPacket.ACTION_CONTINUE_DESTROY_BLOCK) {
            return Kind.CONTINUE;
        }
        if (action == PlayerActionPacket.ACTION_ABORT_BREAK
                || action == PlayerActionPacket.ACTION_STOP_BREAK) {
            return Kind.CANCEL;
        }
        if (action == PlayerActionPacket.ACTION_PREDICT_DESTROY_BLOCK) {
            return Kind.FINISH;
        }
        return null;
    }

    static boolean isLegacyBreakPacket(final Object packet) {
        return packet instanceof PlayerActionPacket
                || packet instanceof RemoveBlockPacket_v113;
    }

    static Kind legacyKind(final Object packet) {
        if (packet instanceof PlayerActionPacket actionPacket) {
            return legacyKind(actionPacket.action);
        }
        if (packet instanceof RemoveBlockPacket_v113) {
            return Kind.FINISH;
        }
        return null;
    }

    static BlockVector3 legacyPosition(final Object packet) {
        if (packet instanceof PlayerActionPacket actionPacket) {
            return legacyPosition(actionPacket);
        }
        if (packet instanceof RemoveBlockPacket_v113 removeBlock) {
            return new BlockVector3(removeBlock.x, removeBlock.y, removeBlock.z);
        }
        return null;
    }

    static BlockVector3 legacyPosition(final PlayerActionPacket packet) {
        return new BlockVector3(packet.x, packet.y, packet.z);
    }

    static boolean hasBreakActions(final PlayerAuthInputPacket packet) {
        if (packet == null
                || packet.getBlockActionData() == null
                || packet.getBlockActionData().isEmpty()) {
            return false;
        }
        for (final PlayerActionType type : packet.getBlockActionData().keySet()) {
            if (kind(type) != null) {
                return true;
            }
        }
        return false;
    }

    static boolean isBreakProgressAction(final Kind kind) {
        return kind == Kind.START || kind == Kind.CONTINUE || kind == Kind.FINISH;
    }

    static boolean isSwingRelevantAction(final Kind kind) {
        
        
        return kind == Kind.START || kind == Kind.FINISH;
    }

    static void removeAction(final PlayerAuthInputPacket packet,
                             final PlayerActionType type) {
        if (packet == null || type == null || packet.getBlockActionData() == null) {
            return;
        }
        final Map<PlayerActionType, PlayerBlockActionData> sanitized =
                new LinkedHashMap<>(packet.getBlockActionData());
        sanitized.remove(type);
        packet.setBlockActionData(sanitized);
        if (sanitized.isEmpty()) {
            packet.getInputData().remove(AuthInputAction.PERFORM_BLOCK_ACTIONS);
        }
    }

    static Box blockBox(final BlockVector3 position) {
        return new Box(position.getX(), position.getY(), position.getZ(),
                position.getX() + 1.0F, position.getY() + 1.0F,
                position.getZ() + 1.0F);
    }

    static Box possibleEyeBox(final GhostPlayer player) {
        return possibleEyeBox(player, null);
    }

    static Box possibleEyeBox(final GhostPlayer player,
                              final PlayerAuthInputPacket packet) {
        final float eyeHeight = player.entityContext.aabbShapeComponent
                .getDimensions().eyeHeight();
        final Vec3 previous = player.entityContext.stateVectorComponent
                .getPreviousPosition().add(0.0F, eyeHeight, 0.0F);
        final Vec3 current = player.entityContext.stateVectorComponent
                .getPosition().add(0.0F, eyeHeight, 0.0F);

        float minX = Math.min(previous.x, current.x);
        float minY = Math.min(previous.y, current.y);
        float minZ = Math.min(previous.z, current.z);
        float maxX = Math.max(previous.x, current.x);
        float maxY = Math.max(previous.y, current.y);
        float maxZ = Math.max(previous.z, current.z);

        final Vec3 raw = plausiblePacketEye(packet, current);
        if (raw != null) {
            minX = Math.min(minX, raw.x);
            minY = Math.min(minY, raw.y);
            minZ = Math.min(minZ, raw.z);
            maxX = Math.max(maxX, raw.x);
            maxY = Math.max(maxY, raw.y);
            maxZ = Math.max(maxZ, raw.z);
        }

        return new Box(minX, minY, minZ, maxX, maxY, maxZ)
                .expand(EYE_UNCERTAINTY);
    }

    static float distanceToBlock(final GhostPlayer player,
                                 final BlockVector3 position) {
        return distanceToBlock(player, position, null);
    }

    public static float distanceToBlock(final GhostPlayer player,
                                        final BlockVector3 position,
                                        final PlayerAuthInputPacket packet) {
        final Box box = blockBox(position);
        final float eyeHeight = player.entityContext.aabbShapeComponent
                .getDimensions().eyeHeight();
        final Vec3 previous = player.entityContext.stateVectorComponent
                .getPreviousPosition().add(0.0F, eyeHeight, 0.0F);
        final Vec3 current = player.entityContext.stateVectorComponent
                .getPosition().add(0.0F, eyeHeight, 0.0F);
        float best = Math.min(distanceToBox(previous, box),
                distanceToBox(current, box));
        final Vec3 raw = plausiblePacketEye(packet, current);
        if (raw != null) {
            best = Math.min(best, distanceToBox(raw, box));
        }
        return best;
    }

    private static Vec3 plausiblePacketEye(final PlayerAuthInputPacket packet,
                                           final Vec3 authoritativeEye) {
        if (packet == null) {
            return null;
        }
        final Vector3f raw = packet.getPosition();
        if (raw == null || !Float.isFinite(raw.getX())
                || !Float.isFinite(raw.getY()) || !Float.isFinite(raw.getZ())) {
            return null;
        }
        final Vec3 candidate = new Vec3(raw.getX(), raw.getY(), raw.getZ());
        
        
        
        if (candidate.distanceTo(authoritativeEye) > 1.0F) {
            return null;
        }
        return candidate;
    }

    private static float distanceToBox(final Vec3 point, final Box box) {
        final float dx = axisGap(point.x, box.minX, box.maxX);
        final float dy = axisGap(point.y, box.minY, box.maxY);
        final float dz = axisGap(point.z, box.minZ, box.maxZ);
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static float axisGap(final float value, final float min,
                                 final float max) {
        if (value < min) {
            return min - value;
        }
        if (value > max) {
            return value - max;
        }
        return 0.0F;
    }

    public static float maxBlockReach(final GhostPlayer player) {
        final GameType gameType = player.entityContext.actorGameTypeComponent.value;
        if (gameType == GameType.SPECTATOR) {
            return Float.NaN;
        }
        
        
        
        return gameType == GameType.CREATIVE ? 13.0F : 7.0F;
    }

    public static boolean inVehicle(final GhostPlayer player) {
        return player.entityContext.vehicleComponent.value != null;
    }

    static InputMode inputMode(final GhostPlayer player) {
        final InputMode mode = player.entityContext.playerInputModeComponent
                .getProtocolValue();
        return mode == null ? InputMode.UNDEFINED : mode;
    }

    public static String pos(final BlockVector3 position) {
        if (position == null) {
            return "null";
        }
        return position.getX() + "," + position.getY() + "," + position.getZ();
    }
}
