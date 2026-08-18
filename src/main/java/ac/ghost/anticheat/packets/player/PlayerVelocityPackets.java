package ac.ghost.anticheat.packets.player;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.protocol.BedrockProtocolCapabilities;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityElytraFirework;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.RemoveEntityPacket;
import cn.nukkit.network.protocol.SetEntityMotionPacket;
import cn.nukkit.network.protocol.MovementEffectPacket;
import cn.nukkit.network.protocol.types.MovementEffectType;

public class PlayerVelocityPackets implements Listener {
    private static final ThreadLocal<Boolean> RESENDING_SERVER_MOTION =
            ThreadLocal.withInitial(() -> false);
    
    
    
    private static final int GEYSER_GLIDE_BOOST_DURATION = 1_000_000;

    private void attachNativeFirework(final EntityElytraFirework firework) {
        final Player following = firework.getFollowingPlayer();
        if (following == null || firework.isClosed()) {
            return;
        }

        final GhostPlayer player = Ghost.getInstance().getPlayerManager().get(following);
        if (player == null || player.isExempted()
                || !BedrockProtocolCapabilities.hasMovementEffect(following.protocol)) {
            return;
        }

        player.ghostMovementBridgeState.nukkitGlideBoostPending = false;
        if (!player.ghostMovementBridgeState.nukkitGlideBoostEntities.add(
                firework.getId())) {
            return;
        }
        sendGeyserGlideBoost(player, GEYSER_GLIDE_BOOST_DURATION);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPacket(final DataPacketSendEvent event) {
        final Player nukkitPlayer = event.getPlayer();
        final GhostPlayer player = Ghost.getInstance().getPlayerManager().get(nukkitPlayer);
        if (player == null || player.isExempted()) {
            return;
        }

        




        if (event.getPacket() instanceof AddEntityPacket packet) {
            final Entity entity = nukkitPlayer.getLevel().getEntity(
                    packet.entityRuntimeId);
            if (entity instanceof EntityElytraFirework firework
                    && firework.getFollowingPlayer() == nukkitPlayer) {
                attachNativeFirework(firework);
            }
            return;
        }

        if (event.getPacket() instanceof RemoveEntityPacket packet) {
            if (player.ghostMovementBridgeState.nukkitGlideBoostEntities.remove(
                    packet.eid)
                    && player.ghostMovementBridgeState.nukkitGlideBoostEntities.isEmpty()
                    && !player.ghostMovementBridgeState.nukkitGlideBoostPending) {
                sendGeyserGlideBoost(player, 0);
            }
            return;
        }

        if (event.getPacket() instanceof SetEntityMotionPacket packet) {
            if (packet.eid != player.runtimeEntityId) {
                return;
            }

            
            
            if (RESENDING_SERVER_MOTION.get()) {
                return;
            }

            if (isNativeFireworkMotionCall()
                    && BedrockProtocolCapabilities.hasMovementEffect(nukkitPlayer.protocol)) {
                
                
                
                
                
                
                
                event.setCancelled(true);
                return;
            }

            
            
            
            packet.tick = 0L;
            final Vec3 serverMotion = new Vec3(packet.motionX, packet.motionY, packet.motionZ);
            player.ghostMovementBridgeState.lastServerMotion = serverMotion.clone();
            player.ghostMovementBridgeState.lastServerMotionTick = packet.tick;
            player.ghostMovementBridgeState.lastServerMotionWallClock = System.currentTimeMillis();
            event.setCancelled(true);

            RESENDING_SERVER_MOTION.set(true);
            try {
                player.getSession().dataPacket(packet, true);
                player.entityContext.forceSendMotionPacketComponent.request(player, serverMotion);
            } finally {
                RESENDING_SERVER_MOTION.remove();
            }
            return;
        }

        if (event.getPacket() instanceof MovementEffectPacket packet) {
            if (packet.targetRuntimeID != player.runtimeEntityId
                    || packet.effectType != MovementEffectType.GLIDE_BOOST) {
                return;
            }

            
            
            
            packet.tick = player.entityContext.serverPlayerMovementSyncComponent.clientBoundPacketTick();

            if (player.entityContext.movementEffectsComponent.glideBoostTicks == 0
                    && packet.effectDuration == 0
                    || packet.effectDuration == Integer.MAX_VALUE) {
                player.entityContext.movementEffectsComponent.glideBoostTicks = 1;
                return;
            }
            player.entityContext.movementEffectsComponent.glideBoostTicks =
                    Math.max(1, packet.effectDuration / 2);
        }
    }

    private void sendGeyserGlideBoost(final GhostPlayer player, final int duration) {
        if (!BedrockProtocolCapabilities.hasMovementEffect(
                player.getSession().protocol)) {
            return;
        }
        player.getSession().dataPacket(new MovementEffectPacket(
                player.runtimeEntityId,
                MovementEffectType.GLIDE_BOOST,
                duration,
                player.entityContext.serverPlayerMovementSyncComponent.clientBoundPacketTick()
        ));
    }

    private boolean isNativeFireworkMotionCall() {
        for (final StackTraceElement frame : Thread.currentThread().getStackTrace()) {
            if (frame.getClassName().equals(EntityElytraFirework.class.getName())
                    && frame.getMethodName().equals("onUpdate")) {
                return true;
            }
        }
        return false;
    }
}
