package ac.ghost.anticheat.check.impl.elytra;

import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.types.AuthInputAction;


public final class ElytraActionUtil {
    private ElytraActionUtil() {
    }

    




    public static boolean hasEffectiveStartGliding(final PlayerAuthInputPacket packet) {
        boolean starting = false;
        for (final AuthInputAction action : packet.getInputData()) {
            if (action == AuthInputAction.START_GLIDING) {
                starting = true;
            } else if (action == AuthInputAction.STOP_GLIDING) {
                starting = false;
            }
        }
        return starting;
    }

    
    public static boolean isFreshInput(final GhostPlayer player,
                                       final PlayerAuthInputPacket packet) {
        final long tick = packet.getTick();
        return tick >= 0L
                && tick > player.entityContext.serverPlayerMovementComponent
                .getLastReceivedInputTick();
    }

    
    public static boolean hasJumpIntent(final PlayerAuthInputPacket packet) {
        for (final AuthInputAction action : packet.getInputData()) {
            if (action == null) {
                continue;
            }
            final String name = action.name();
            if (name.equals("START_JUMPING")
                    || name.equals("JUMPING")
                    || name.equals("JUMP_DOWN")) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasStartGliding(final GhostPlayer player,
                                          final DataPacketReceiveEvent event) {
        final Object raw = event.getPacket();
        if (raw instanceof PlayerAuthInputPacket packet) {
            return isFreshInput(player, packet)
                    && hasEffectiveStartGliding(packet);
        }
        return raw instanceof PlayerActionPacket packet
                && !player.getSession().isMovementServerAuthoritative()
                && packet.action == PlayerActionPacket.ACTION_START_GLIDE;
    }

    public static boolean hasJumpIntent(final GhostPlayer player,
                                        final DataPacketReceiveEvent event) {
        final Object raw = event.getPacket();
        if (raw instanceof PlayerAuthInputPacket packet) {
            return isFreshInput(player, packet) && hasJumpIntent(packet);
        }
        return raw instanceof PlayerActionPacket packet
                && !player.getSession().isMovementServerAuthoritative()
                && packet.action == PlayerActionPacket.ACTION_JUMP;
    }

    public static long actionTick(final GhostPlayer player,
                                  final DataPacketReceiveEvent event) {
        if (event.getPacket() instanceof PlayerAuthInputPacket packet) {
            return packet.getTick();
        }
        return player.ghostMovementBridgeState.legacyInputTick;
    }

    public static void suppressStartGliding(final GhostPlayer player,
                                            final DataPacketReceiveEvent event) {
        if (event.getPacket() instanceof PlayerAuthInputPacket) {
            requestSuppressStartGliding(player);
        } else {
            event.setCancelled(true);
        }
    }

    




    public static void requestSuppressStartGliding(final GhostPlayer player) {
        player.ghostMovementBridgeState.suppressStartGlidingAction = true;
    }

    public static void beginPacket(final GhostPlayer player) {
        player.ghostMovementBridgeState.suppressStartGlidingAction = false;
    }

    public static void finishPacket(final GhostPlayer player,
                                    final PlayerAuthInputPacket packet) {
        if (player.ghostMovementBridgeState.suppressStartGlidingAction) {
            packet.getInputData().remove(AuthInputAction.START_GLIDING);
        }
        player.ghostMovementBridgeState.suppressStartGlidingAction = false;
    }
}
