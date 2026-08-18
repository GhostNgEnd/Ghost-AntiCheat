package ac.ghost.anticheat.port.nukkit;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.protocol.BedrockProtocolCapabilities;
import ac.ghost.anticheat.util.LatencyUtil;
import cn.nukkit.network.protocol.NetworkStackLatencyPacket;

import java.util.concurrent.atomic.AtomicLong;


public final class NukkitLatencyAdapter {
    private static final AtomicLong LATENCY_ID_COUNTER = new AtomicLong();

    private final GhostPlayer player;
    private final LatencyUtil latencyUtil;

    public NukkitLatencyAdapter(final GhostPlayer player) {
        this.player = player;
        this.latencyUtil = new LatencyUtil(player);
    }

    public LatencyUtil latencyUtil() {
        return this.latencyUtil;
    }

    public void serverTick() {
        if (this.player.isExempted()) {
            this.latencyUtil.reset();
            return;
        }
        if (!this.latencyUtil.hasInFlight()) {
            sendLatencyStack();
            return;
        }

        final long sinceLastAck = System.currentTimeMillis() - this.latencyUtil.prevAcceptedTime;
        if (sinceLastAck > Ghost.getConfig().maxLatencyWait()) {
            this.player.kick("Timed out!");
        }
    }

    public void sendLatencyStack(final Runnable runnable) {
        sendLatencyStack();
        this.latencyUtil.queue(runnable);
    }

    public void sendLatencyStackAfterOutbound(final Runnable runnable) {
        Ghost.getPluginInstance().getServer().getScheduler().scheduleTask(
                Ghost.getPluginInstance(),
                () -> sendLatencyStackForcedFlush(runnable)
        );
    }

    public void sendLatencyStackForcedFlush(final Runnable runnable) {
        sendLatencyStackForcedFlush();
        this.latencyUtil.queue(runnable);
    }

    public void sendLatencyStackForcedFlush() {
        sendLatencyPacket(true);
    }

    public void sendLatencyStack() {
        sendLatencyPacket(false);
    }

    private void sendLatencyPacket(final boolean forceFlush) {
        if (this.player.isExempted()) {
            return;
        }
        
        
        final long id = Math.floorMod(LATENCY_ID_COUNTER.getAndIncrement(), 999_999_999L) + 1L;

        if (!BedrockProtocolCapabilities.hasNetworkStackLatency(
                this.player.getSession().protocol)) {
            
            
            
            
            this.latencyUtil.queue(id, true);
            Ghost.getPluginInstance().getServer().getScheduler().scheduleTask(
                    Ghost.getPluginInstance(),
                    () -> this.latencyUtil.onLocalBarrier(id));
            return;
        }

        final NetworkStackLatencyPacket latencyPacket = new NetworkStackLatencyPacket();
        latencyPacket.timestamp = id;
        latencyPacket.fromServer = false;
        latencyPacket.unknownBool = false;

        this.latencyUtil.queue(id, true);

        if (!forceFlush) {
            this.player.getSession().dataPacket(latencyPacket, true);
            return;
        }

        try {
            this.player.getSession().forceDataPacket(
                    latencyPacket,
                    () -> {
                    },
                    cn.nukkit.network.session.NetworkPlayerSession.ImmediatePacketMode.QUEUED_FLUSH
            );
        } catch (Throwable ignored) {
            this.player.getSession().dataPacket(latencyPacket, true);
        }
    }
}
