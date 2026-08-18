package ac.ghost.anticheat.check.impl.reach;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.annotations.Experimental;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.compensated.cache.entity.EntityCache;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.protocol.BedrockProtocolCapabilities;
import ac.ghost.anticheat.util.Pair;
import ac.ghost.anticheat.util.math.ReachUtil;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.network.protocol.InteractPacket;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.types.GameType;

import java.util.HashMap;
import java.util.Map;

@Experimental
@CheckInfo(name = "Reach")
public final class Reach extends PacketCheck {
    private final Map<Pair<Vec3, Vec3>, EntityCache> queuedHitAttacks = new HashMap<>();
    private boolean lastKnownDistanceWasValid;

    public Reach(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final DataPacketReceiveEvent event) {
        
        if (event.getPacket() instanceof InteractPacket packet
                && packet.action == InteractPacket.ACTION_LEFT_CLICK) {
            if (!BedrockProtocolCapabilities.usesLegacyEntityInteraction(
                    player.getSession().protocol)) {
                event.setCancelled(true);
                return;
            }
            handleAttack(event, packet.target);
            return;
        }

        if (!(event.getPacket() instanceof InventoryTransactionPacket packet)
                || packet.transactionType != InventoryTransactionPacket.TYPE_USE_ITEM_ON_ENTITY) {
            return;
        }

        final UseItemOnEntityData data = (UseItemOnEntityData) packet.transactionData;
        if (data.actionType != InventoryTransactionPacket.USE_ITEM_ON_ENTITY_ACTION_ATTACK) {
            return;
        }

        handleAttack(event, data.entityRuntimeId);
    }

    private void handleAttack(final DataPacketReceiveEvent event,
                              final long targetRuntimeId) {
        final EntityCache entity = player.entityRegistry.getEntity(targetRuntimeId);
        
        if (entity == null || entity.isInVehicle()) {
            return;
        }

        if (player.entityContext.actorGameTypeComponent.value == GameType.CREATIVE || player.entityContext.actorGameTypeComponent.value == GameType.SPECTATOR) {
            return;
        }

        
        final Pair<Vec3, Vec3> pair = new Pair<>(player.entityContext.stateVectorComponent.getPreviousPosition(), player.entityContext.stateVectorComponent.getPosition());
        this.queuedHitAttacks.put(pair, entity);

        
        final float distance = ReachUtil.calculateReach(player, pair, entity);
        if (!Float.isNaN(distance) && distance > Ghost.getConfig().toleranceReach()) {
            if (!this.lastKnownDistanceWasValid) {
                event.setCancelled(true);
            }
            this.lastKnownDistanceWasValid = false;
        } else if (!Float.isNaN(distance)) {
            this.lastKnownDistanceWasValid = true;
        }
    }

    public void pollQueuedHits() {
        this.lastKnownDistanceWasValid = false;
        if (this.queuedHitAttacks.isEmpty()) {
            return;
        }

        float farthestDistance = 0F;
        boolean evaluated = false;

        for (Map.Entry<Pair<Vec3, Vec3>, EntityCache> entry : this.queuedHitAttacks.entrySet()) {
            final EntityCache entity = entry.getValue();
            if (entity == null) {
                continue;
            }

            final float distance = ReachUtil.calculateReach(player, entry.getKey(), entity);
            if (Float.isNaN(distance)) {
                continue;
            }

            evaluated = true;
            farthestDistance = Math.max(farthestDistance, distance);
        }

        if (evaluated && farthestDistance > Ghost.getConfig().toleranceReach()) {
            this.fail("entity out of range, distance=" + farthestDistance);
        }

        this.queuedHitAttacks.clear();
    }
}
