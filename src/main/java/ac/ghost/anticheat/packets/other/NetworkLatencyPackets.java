package ac.ghost.anticheat.packets.other;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.protocol.BedrockProtocolCapabilities;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.network.protocol.NetworkStackLatencyPacket;

public class NetworkLatencyPackets implements Listener {
    public static final long LATENCY_MAGNITUDE = 1_000_000L;
    public static final long PS5_LATENCY_MAGNITUDE = 1_000L;
    private static final int DEVICE_OS_PLAYSTATION = 11;

    @EventHandler
    public void onPacketSend(final DataPacketSendEvent event) {
        if (!(event.getPacket() instanceof NetworkStackLatencyPacket packet)) {
            return;
        }

        final Player nukkitPlayer = event.getPlayer();
        final GhostPlayer player = Ghost.getInstance().getPlayerManager().get(nukkitPlayer);
        if (player == null || player.isExempted()) {
            return;
        }

        final boolean alreadyQueuedAsOurs = player.latencyAdapter.latencyUtil()
                .hasPending(packet.timestamp, true);

        if (!packet.fromServer) {
            packet.fromServer = true;

            
            
            
            
            if (!alreadyQueuedAsOurs) {
                player.latencyAdapter.latencyUtil().queue(packet.timestamp, false);
            }
            return;
        }

        player.latencyAdapter.latencyUtil().queue(packet.timestamp, false);
    }

    @EventHandler
    public void onPacket(final DataPacketReceiveEvent event) {
        if (!(event.getPacket() instanceof NetworkStackLatencyPacket packet)) {
            return;
        }

        final Player nukkitPlayer = event.getPlayer();
        final GhostPlayer player = Ghost.getInstance().getPlayerManager().get(nukkitPlayer);
        if (player == null || player.isExempted()) {
            return;
        }

        if (usesOrderedResponse(player.getSession().protocol)) {
            
            
            event.setCancelled(player.latencyAdapter.latencyUtil()
                    .onOrderedResponse());
            return;
        }

        
        
        final long id = resolveModernResponseId(packet.timestamp,
                getLatencyMagnitude(nukkitPlayer));
        event.setCancelled(player.latencyAdapter.latencyUtil().onResponse(id));
    }

    static boolean usesOrderedResponse(final int protocol) {
        return !BedrockProtocolCapabilities.hasHandleTeleportAuthInput(protocol);
    }

    static long resolveModernResponseId(final long wireTimestamp,
                                        final long platformMagnitude) {
        return wireTimestamp / platformMagnitude;
    }

    private static long getLatencyMagnitude(final Player player) {
        try {
            if (player.getLoginChainData() != null
                    && player.getLoginChainData().getDeviceOS()
                    == DEVICE_OS_PLAYSTATION) {
                return PS5_LATENCY_MAGNITUDE;
            }
        } catch (Exception ignored) {
        }

        return LATENCY_MAGNITUDE;
    }
}
