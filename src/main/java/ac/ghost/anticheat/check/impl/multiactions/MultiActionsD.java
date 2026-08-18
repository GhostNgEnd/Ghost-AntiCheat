package ac.ghost.anticheat.check.impl.multiactions;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.network.protocol.ContainerClosePacket;
import cn.nukkit.network.protocol.ContainerOpenPacket;
import cn.nukkit.network.protocol.InteractPacket;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.PlayerInputPacket;
import cn.nukkit.network.protocol.UpdateTradePacket;
import cn.nukkit.network.protocol.types.ContainerIds;


@CheckInfo(name = "MultiActionsD")
public final class MultiActionsD extends PacketCheck {
    private boolean inventoryOpen;
    private boolean directionalInput;

    public MultiActionsD(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final DataPacketReceiveEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Object packet = event.getPacket();
        if (packet instanceof PlayerAuthInputPacket authInput) {
            directionalInput = InventoryInteractionUtil
                    .hasDirectionalInput(player, authInput);
            if (inventoryOpen && directionalInput
                    && InventoryInteractionUtil.isItemInteraction(authInput)) {
                fail();
                InventoryInteractionUtil.removeEmbeddedItemInteraction(authInput);
            }
            return;
        }

        if (packet instanceof PlayerInputPacket legacyInput) {
            directionalInput = InventoryInteractionUtil
                    .hasDirectionalInput(legacyInput);
            return;
        }

        if (packet instanceof InteractPacket interact
                && interact.action == InteractPacket.ACTION_OPEN_INVENTORY) {
            inventoryOpen = true;
            return;
        }

        if (packet instanceof ContainerClosePacket close) {
            if (close.windowId == ContainerIds.INVENTORY
                    || close.windowId == ContainerIds.NONE) {
                inventoryOpen = false;
            }
            return;
        }

        if (inventoryOpen && directionalInput
                && InventoryInteractionUtil.isItemInteraction(packet)) {
            fail();
            event.setCancelled(true);
        }
    }

    @Override
    public void onPacketSend(final DataPacketSendEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getPacket() instanceof ContainerOpenPacket packet) {
            inventoryOpen = packet.windowId == ContainerIds.INVENTORY;
            return;
        }

        if (event.getPacket() instanceof UpdateTradePacket) {
            inventoryOpen = false;
            return;
        }

        if (event.getPacket() instanceof ContainerClosePacket packet
                && (packet.windowId == ContainerIds.INVENTORY
                || packet.windowId == ContainerIds.NONE)) {
            inventoryOpen = false;
        }
    }
}
