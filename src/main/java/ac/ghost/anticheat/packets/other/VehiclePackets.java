package ac.ghost.anticheat.packets.other;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.compensated.cache.entity.EntityCache;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.player.data.VehicleData;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.network.protocol.InteractPacket;
import cn.nukkit.network.protocol.SetEntityLinkPacket;
import cn.nukkit.network.protocol.types.EntityLink;

public class VehiclePackets implements Listener {

    @EventHandler
    public void onPacketReceive(final DataPacketReceiveEvent event) {
        final Player nukkitPlayer = event.getPlayer();
        final GhostPlayer player = Ghost.getInstance().getPlayerManager().get(nukkitPlayer);
        if (player == null || !(event.getPacket() instanceof InteractPacket packet)) {
            return;
        }

        if (packet.target == player.runtimeEntityId && packet.action == InteractPacket.ACTION_VEHICLE_EXIT) {
            player.entityContext.vehicleComponent.value = null;
        }
    }

    @EventHandler
    public void onPacketSend(final DataPacketSendEvent event) {
        final Player nukkitPlayer = event.getPlayer();
        if (nukkitPlayer == null) {
            return;
        }
        final GhostPlayer player = Ghost.getInstance().getPlayerManager().get(nukkitPlayer);
        if (player == null) {
            return;
        }

        if (event.getPacket() instanceof SetEntityLinkPacket packet) {
            player.latencyAdapter.sendLatencyStack(() ->
                    this.applySetEntityLink(player, packet));
        }
    }

    private void applySetEntityLink(final GhostPlayer player, final SetEntityLinkPacket packet) {
        this.applyLink(player, packet.vehicleUniqueId, packet.riderUniqueId, packet.type);
    }

    private void applyLink(final GhostPlayer player, final long vehicleUniqueId, final long riderUniqueId, final byte type) {
        final Long vehicleRuntime = player.entityRegistry.getRuntimeIdByUniqueId(vehicleUniqueId);
        
        
        
        final boolean localRider = riderUniqueId == player.runtimeEntityId;
        final Long riderRuntime = localRider
                ? player.runtimeEntityId
                : player.entityRegistry.getRuntimeIdByUniqueId(riderUniqueId);
        if (!localRider && riderRuntime != null) {
            final EntityCache rider = player.entityRegistry.getEntity(riderRuntime);
            if (rider != null) {
                rider.setInVehicle(type != SetEntityLinkPacket.TYPE_REMOVE && type != EntityLink.TYPE_REMOVE);
            }
        }

        if (!localRider) {
            return;
        }

        
        
        final EntityCache linkedVehicle = vehicleRuntime == null
                ? null
                : player.entityRegistry.getEntity(vehicleRuntime);
        if (linkedVehicle == null) {
            return;
        }
        player.getTeleportUtil().clearPendingTeleports();

        if (type == SetEntityLinkPacket.TYPE_REMOVE || type == EntityLink.TYPE_REMOVE) {
            player.entityContext.vehicleComponent.value = null;
            return;
        }

        final VehicleData data = new VehicleData();
        data.vehicleRuntimeId = vehicleRuntime;
        player.entityContext.vehicleComponent.value = data;
    }

    private static String ridingId(final GhostPlayer player) {
        return player.getSession() == null || player.getSession().riding == null
                ? "none" : Long.toString(player.getSession().riding.getId());
    }
}
