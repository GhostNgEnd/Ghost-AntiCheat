package ac.ghost.anticheat.check.impl.multiactions;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.annotations.Experimental;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.protocol.BedrockProtocolCapabilities;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.InteractPacket;


@Experimental
@CheckInfo(name = "MultiActionsA")
public final class MultiActionsA extends PacketCheck {
    public MultiActionsA(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final DataPacketReceiveEvent event) {
        final Object packet = event.getPacket();
        final boolean legacyAttack = packet instanceof InteractPacket interact
                && interact.action == InteractPacket.ACTION_LEFT_CLICK
                && BedrockProtocolCapabilities.usesLegacyEntityInteraction(
                        player.getSession().protocol);
        final boolean transactionAttack = packet instanceof InventoryTransactionPacket transaction
                && transaction.transactionType == InventoryTransactionPacket.TYPE_USE_ITEM_ON_ENTITY
                && transaction.transactionData instanceof UseItemOnEntityData data
                && data.actionType == InventoryTransactionPacket.USE_ITEM_ON_ENTITY_ACTION_ATTACK;
        if ((!legacyAttack && !transactionAttack)
                || !MultiActionUtil.isUsingSlowdownItem(player)) {
            return;
        }

        fail("action=attack, item=" + MultiActionUtil.usedItemName(player));
        event.setCancelled(true);
    }
}
