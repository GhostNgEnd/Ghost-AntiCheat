package ac.ghost.anticheat.check.impl.multiactions;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.annotations.Experimental;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.MovePlayerPacket;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.PlayerInputPacket;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.network.protocol.v113.DropItemPacket_v113;


@Experimental
@CheckInfo(name = "MultiActionsC")
public final class MultiActionsC extends PacketCheck {
    private boolean dropping;

    public MultiActionsC(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final DataPacketReceiveEvent event) {
        final Object packet = event.getPacket();

        
        
        if (isMovementPacket(packet)) {
            this.dropping = false;
        }

        if (packet instanceof DropItemPacket_v113
                || packet instanceof InventoryTransactionPacket transactionPacket
                && isDrop(transactionPacket)) {
            this.dropping = true;
            return;
        }

        if (this.dropping || !MultiActionUtil.isUsingSlowdownItem(player)) {
            return;
        }

        if (packet instanceof PlayerActionPacket actionPacket
                && actionPacket.action == PlayerActionPacket.ACTION_MISSED_SWING) {
            fail("action=missed_swing, item=" + MultiActionUtil.usedItemName(player));
            event.setCancelled(true);
            return;
        }

        if (packet instanceof AnimatePacket animatePacket
                && animatePacket.action == AnimatePacket.Action.SWING_ARM
                
                
                
                && animatePacket.swingSource != AnimatePacket.SwingSource.MINE
                && animatePacket.swingSource != AnimatePacket.SwingSource.BUILD) {
            fail("action=swing, item=" + MultiActionUtil.usedItemName(player));
            event.setCancelled(true);
        }
    }

    private static boolean isMovementPacket(final Object packet) {
        return packet instanceof PlayerAuthInputPacket
                || packet instanceof MovePlayerPacket
                || packet instanceof PlayerInputPacket;
    }

    private static boolean isDrop(final InventoryTransactionPacket packet) {
        if (packet.transactionType != InventoryTransactionPacket.TYPE_NORMAL
                || packet.actions == null) {
            return false;
        }
        for (final NetworkInventoryAction action : packet.actions) {
            if (action != null
                    && action.sourceType == NetworkInventoryAction.SOURCE_WORLD
                    && action.inventorySlot
                    == InventoryTransactionPacket.ACTION_MAGIC_SLOT_DROP_ITEM) {
                return true;
            }
        }
        return false;
    }
}
