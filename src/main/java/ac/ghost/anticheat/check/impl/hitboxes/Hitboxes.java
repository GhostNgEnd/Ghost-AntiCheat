package ac.ghost.anticheat.check.impl.hitboxes;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.annotations.Experimental;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.compensated.cache.entity.EntityCache;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.protocol.BedrockProtocolCapabilities;
import ac.ghost.anticheat.util.MathUtil;
import ac.ghost.anticheat.util.Pair;
import ac.ghost.anticheat.util.math.HitboxUtil;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.network.protocol.InteractPacket;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.types.GameType;
import cn.nukkit.network.protocol.types.InputMode;

import java.util.HashMap;
import java.util.Map;

@Experimental
@CheckInfo(name = "Hitboxes")
public final class Hitboxes extends PacketCheck {
    private final Map<Pair<Vec3, Vec3>, QueuedHit> queuedHitAttacks = new HashMap<>();
    private boolean lastKnownSightWasValid;

    public Hitboxes(final GhostPlayer player) {
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
        
        
        
        if (player.entityContext.playerInputModeComponent.getProtocolValue() == InputMode.TOUCH && player.entityContext.playerActionComponent.interactRotation() == null) {
            this.lastKnownSightWasValid = false;
            this.queuedHitAttacks.clear();
            return;
        }

        final EntityCache entity = player.entityRegistry.getEntity(targetRuntimeId);
        
        if (entity == null || entity.isInVehicle()) {
            return;
        }

        if (player.entityContext.actorGameTypeComponent.value == GameType.CREATIVE || player.entityContext.actorGameTypeComponent.value == GameType.SPECTATOR) {
            return;
        }

        if (player.entityContext.playerInputModeComponent.getProtocolValue() == InputMode.TOUCH
                && MathUtil.wrapDegrees(Math.abs(player.entityContext.actorRotationComponent.getYaw() - player.entityContext.playerActionComponent.interactRotation().getY())) > 110) {
            this.lastKnownSightWasValid = false;
            event.setCancelled(true);
            return;
        }

        
        final Pair<Vec3, Vec3> pair = new Pair<>(player.entityContext.stateVectorComponent.getPreviousPosition(), player.entityContext.stateVectorComponent.getPosition());
        this.queuedHitAttacks.put(pair, new QueuedHit(entity));

        
        if (!HitboxUtil.isInSight(player, pair, entity)) {
            if (!this.lastKnownSightWasValid) {
                event.setCancelled(true);
            }
            this.lastKnownSightWasValid = false;
        } else {
            this.lastKnownSightWasValid = true;
        }
    }

    public void pollQueuedHits() {
        this.lastKnownSightWasValid = false;
        if (player.entityContext.playerInputModeComponent.getProtocolValue() == InputMode.TOUCH && player.entityContext.playerActionComponent.interactRotation() == null) {
            this.queuedHitAttacks.clear();
            return;
        }
        if (this.queuedHitAttacks.isEmpty()) {
            return;
        }

        boolean failedSight = false;
        for (Map.Entry<Pair<Vec3, Vec3>, QueuedHit> entry : this.queuedHitAttacks.entrySet()) {
            final QueuedHit queuedHit = entry.getValue();
            final EntityCache entity = queuedHit.entity;
            if (entity == null || entity.getCurrent() == null) {
                continue;
            }

            final boolean inSight = HitboxUtil.isInSight(player, entry.getKey(), entity);
            final int interpolationStep = entity.getCurrent().getInterpolator() == null
                    ? 0
                    : entity.getCurrent().getInterpolator().getStep();
            if (!inSight && interpolationStep > 1) {
                continue;
            }

            if (!inSight) {
                failedSight = true;
            }
        }

        if (failedSight) {
            this.fail("failed to find entity in sight.");
        }

        this.queuedHitAttacks.clear();
    }

    private static final class QueuedHit {
        private final EntityCache entity;

        private QueuedHit(final EntityCache entity) {
            this.entity = entity;
        }
    }
}
