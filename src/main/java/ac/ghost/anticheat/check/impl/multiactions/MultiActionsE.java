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


@CheckInfo(name = "MultiActionsE")
public final class MultiActionsE extends PacketCheck {
    private int openWindowId = ContainerIds.NONE;
    private boolean directionalInput;

    public MultiActionsE(final GhostPlayer player) {
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
            if (isContainerOpen() && directionalInput
                    && InventoryInteractionUtil.isItemInteraction(authInput)) {
                fail("windowId=" + openWindowId);
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
            openWindowId = ContainerIds.NONE;
            return;
        }

        if (packet instanceof ContainerClosePacket close) {
            close(close.windowId);
            return;
        }

        if (isContainerOpen() && directionalInput
                && InventoryInteractionUtil.isItemInteraction(packet)) {
            fail("windowId=" + openWindowId);
            event.setCancelled(true);
        }
    }

    @Override
    public void onPacketSend(final DataPacketSendEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getPacket() instanceof ContainerOpenPacket packet) {
            openWindowId = isContainerId(packet.windowId)
                    ? packet.windowId : ContainerIds.NONE;
            return;
        }

        if (event.getPacket() instanceof UpdateTradePacket packet) {
            openWindowId = isContainerId(packet.windowId)
                    ? packet.windowId : ContainerIds.NONE;
            return;
        }

        if (event.getPacket() instanceof ContainerClosePacket packet) {
            close(packet.windowId);
        }
    }

    private void close(final int windowId) {
        if (windowId == openWindowId || windowId == ContainerIds.NONE) {
            openWindowId = ContainerIds.NONE;
        }
    }

    private boolean isContainerOpen() {
        return isContainerId(openWindowId);
    }

    private static boolean isContainerId(final int windowId) {
        return windowId >= ContainerIds.FIRST && windowId <= ContainerIds.LAST;
    }
}
