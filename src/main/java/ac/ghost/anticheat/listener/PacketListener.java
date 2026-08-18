package ac.ghost.anticheat.listener;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.check.impl.elytra.ElytraActionUtil;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.packets.ClientMovementPredictionSyncPacket;
import ac.ghost.anticheat.prediction.bds.system.movement.UpdateHorizontalPoseSystem;
import ac.ghost.anticheat.prediction.bds.system.movement.PlayerBoundingBoxStateUpdateSystem;
import ac.ghost.anticheat.prediction.bds.system.player.StartGlidingActionServerSystem;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;

public class PacketListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPacketReceived(final DataPacketReceiveEvent event) {
        final Player nukkitPlayer = event.getPlayer();
        final GhostPlayer player = Ghost.getInstance().getPlayerManager().get(nukkitPlayer);
        if (player == null) {
            return;
        }

        if (event.getPacket() instanceof ClientMovementPredictionSyncPacket packet) {
            if (packet.runtimeEntityId != player.runtimeEntityId) {
                return;
            }

            if (packet.speed != player.entityContext.attributesComponent.movementSpeed()) {
                player.getSession().sendAttributes();
            }

            player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_SNEAKING,
                    packet.hasFlag(Entity.DATA_FLAG_SNEAKING));
            
            
            final boolean syncedSwimming =
                    packet.hasFlag(Entity.DATA_FLAG_SWIMMING);
            player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_SWIMMING, syncedSwimming);
            player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_CRAWLING,
                    packet.hasFlag(Entity.DATA_FLAG_CRAWLING));
            player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_SPRINTING,
                    packet.hasFlag(Entity.DATA_FLAG_SPRINTING));

            player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_GLIDING,
                    packet.hasFlag(Entity.DATA_FLAG_GLIDING)
                            && StartGlidingActionServerSystem.hasElytraEquipped(player.entityContext));
            UpdateHorizontalPoseSystem.tick(player.entityContext);
            PlayerBoundingBoxStateUpdateSystem.tick(player.entityContext);
        }

        if (player.isExempted()) {
            return;
        }

        final PlayerAuthInputPacket authInput = event.getPacket() instanceof PlayerAuthInputPacket packet
                ? packet : null;
        if (authInput != null) {
            ElytraActionUtil.beginPacket(player);
        }
        try {
            for (final PacketCheck check : player.getCheckHolder().packetChecks()) {
                check.onPacketReceive(event);
            }
        } finally {
            if (authInput != null) {
                ElytraActionUtil.finishPacket(player, authInput);
            }
        }
    }


    @EventHandler(ignoreCancelled = false)
    public void onPacketSent(final DataPacketSendEvent event) {
        final Player nukkitPlayer = event.getPlayer();
        final GhostPlayer player = Ghost.getInstance().getPlayerManager().get(nukkitPlayer);
        if (player == null) {
            return;
        }

        if (player.isExempted()) {
            return;
        }

        for (final PacketCheck check : player.getCheckHolder().packetChecks()) {
            check.onPacketSend(event);
        }
    }
}
