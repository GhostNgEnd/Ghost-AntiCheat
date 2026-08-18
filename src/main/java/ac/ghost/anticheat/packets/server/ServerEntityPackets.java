package ac.ghost.anticheat.packets.server;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.compensated.cache.entity.EntityCache;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.AddPlayerPacket;
import cn.nukkit.network.protocol.MoveEntityAbsolutePacket;
import cn.nukkit.network.protocol.MoveEntityDeltaPacket;
import cn.nukkit.network.protocol.MovePlayerPacket;
import cn.nukkit.network.protocol.RemoveEntityPacket;

public class ServerEntityPackets implements Listener {

    @EventHandler
    public void onPacket(final DataPacketSendEvent event) {
        final Player nukkitPlayer = event.getPlayer();
        if (nukkitPlayer == null) {
            return;
        }

        final GhostPlayer player = Ghost.getInstance().getPlayerManager().get(nukkitPlayer);
        if (player == null) {
            return;
        }

        if (event.getPacket() instanceof RemoveEntityPacket packet) {
            player.latencyAdapter.sendLatencyStack();
            player.latencyAdapter.sendLatencyStack(() -> {
                if (player.entityContext.vehicleComponent.value != null && player.entityContext.vehicleComponent.value.vehicleRuntimeId == packet.eid) {
                    player.entityContext.vehicleComponent.value = null;
                }

                player.entityRegistry.removeEntity(packet.eid);
            });
        }

        if (event.getPacket() instanceof AddEntityPacket packet) {
            final EntityCache entity = player.entityRegistry.addToCache(
                    packet.entityRuntimeId,
                    packet.entityUniqueId,
                    packet.type,
                    packet.id,
                    packet.metadata,
                    false
            );
            if (entity == null) {
                return;
            }

            
            
            
            final Vec3 position = new Vec3(packet.x, packet.y, packet.z)
                    .down(entity.getYOffset());
            entity.setServerPosition(position);
            entity.init();
            entity.interpolate(position, false);
            entity.applyMetadata(packet.metadata);
        }

        if (event.getPacket() instanceof AddPlayerPacket packet) {
            final EntityCache entity = player.entityRegistry.addToCache(
                    packet.entityRuntimeId,
                    packet.entityUniqueId,
                    EntityHuman.NETWORK_ID,
                    "minecraft:player",
                    packet.metadata,
                    true
            );
            if (entity == null) {
                return;
            }

            final Vec3 position = new Vec3(packet.x, packet.y, packet.z);
            entity.setServerPosition(position);
            entity.init();
            entity.interpolate(position, false);
            entity.applyMetadata(packet.metadata);
        }

        if (event.getPacket() instanceof MoveEntityDeltaPacket packet) {
            final EntityCache entity = player.entityRegistry.getEntity(packet.eid);
            if (entity == null) {
                return;
            }

            final boolean hasX = (packet.flags & MoveEntityDeltaPacket.FLAG_HAS_X) != 0;
            final boolean hasY = (packet.flags & MoveEntityDeltaPacket.FLAG_HAS_Y) != 0;
            final boolean hasZ = (packet.flags & MoveEntityDeltaPacket.FLAG_HAS_Z) != 0;
            if (!hasX && !hasY && !hasZ) {
                return;
            }

            final float x = hasX ? packet.x : entity.getServerPosition().x;
            final float y = hasY ? packet.y : entity.getServerPosition().y;
            final float z = hasZ ? packet.z : entity.getServerPosition().z;

            this.queuePositionUpdate(player, entity, new Vec3(x, y, z), true);
        }

        if (event.getPacket() instanceof MoveEntityAbsolutePacket packet) {
            final EntityCache entity = player.entityRegistry.getEntity(packet.eid);
            if (entity == null) {
                return;
            }

            this.queuePositionUpdate(player, entity,
                    new Vec3((float) packet.x, (float) packet.y, (float) packet.z), false);
        }

        if (event.getPacket() instanceof MovePlayerPacket packet) {
            if (packet.eid == player.runtimeEntityId) {
                return;
            }

            final EntityCache entity = player.entityRegistry.getEntity(packet.eid);
            if (entity == null) {
                return;
            }

            final Vec3 rawPosition = new Vec3(packet.x, packet.y, packet.z);
            this.queuePositionUpdate(player, entity,
                    this.normalizeMovePlayerPosition(nukkitPlayer, entity, rawPosition),
                    packet.mode == MovePlayerPacket.MODE_NORMAL,
                    false);
        }
    }

    






    private Vec3 normalizeMovePlayerPosition(final Player viewer,
                                             final EntityCache entity,
                                             final Vec3 raw) {
        final float offset = entity.getYOffset();
        if (offset == 0F) {
            return raw;
        }

        final Entity live = viewer.getLevel().getEntity(entity.getRuntimeId());
        if (live instanceof Player) {
            final float footY = (float) live.y;
            final float rawDistance = Math.abs(raw.y - footY);
            final float offsetDistance = Math.abs((raw.y - offset) - footY);
            return rawDistance <= offsetDistance ? raw : raw.down(offset);
        }

        
        return raw.down(offset);
    }

    private void queuePositionUpdate(final GhostPlayer player,
                                     final EntityCache entity,
                                     final Vec3 raw,
                                     final boolean lerp) {
        this.queuePositionUpdate(player, entity, raw, lerp, true);
    }

    private void queuePositionUpdate(final GhostPlayer player,
                                     final EntityCache entity,
                                     final Vec3 raw,
                                     final boolean lerp,
                                     final boolean applyYOffset) {
        final Vec3 position = applyYOffset ? raw.down(entity.getYOffset()) : raw;
        final float distance = entity.getServerPosition().squaredDistanceTo(position);
        if (distance < 1.0E-15F) {
            return;
        }

        entity.setServerPosition(position);
        final long updateSequence = entity.nextPositionUpdateSequence();

        
        
        
        
        player.latencyAdapter.sendLatencyStack();
        player.latencyAdapter.latencyUtil().queue(() -> entity.interpolate(
                position, lerp && distance < 4096, updateSequence));
        player.latencyAdapter.sendLatencyStackAfterOutbound(
                () -> entity.clearPastForPositionUpdate(updateSequence));
    }
}
