package ac.ghost.anticheat.prediction.bds.inventory;

import ac.ghost.anticheat.compensated.CompensatedInventory;
import ac.ghost.anticheat.data.inventory.ItemCache;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.entity.EntityContext;
import cn.nukkit.network.protocol.ItemStackRequestPacket;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequest;

import java.util.ArrayList;
import java.util.List;


public final class ItemStackNetManagerServer {
    private final EntityContext entity;
    private final GhostPlayer player;

    public ItemStackNetManagerServer(final EntityContext entity) {
        this.entity = entity;
        this.player = entity.externalDataComponent.player();
    }

    public ItemStackResponseStatus handle(final ItemStackRequestPacket packet) {
        this.entity.serverPlayerInventoryTransactionComponent.processing = true;
        if (packet == null || packet.getRequests() == null) {
            return finish(ItemStackResponseStatus.ERROR);
        }

        final PacketSnapshot snapshot = PacketSnapshot.capture(
                this.player.compensatedInventory);
        final List<ItemStackRequest> requests = new ArrayList<>(packet.getRequests());
        for (final ItemStackRequest request : requests) {
            final ItemStackRequestActionHandler handler =
                    new ItemStackRequestActionHandler(this.player);
            final ItemStackResponseStatus status = handler.handleRequest(request);
            if (!status.isSuccess()) {
                
                
                
                
                
                snapshot.restore(this.player.compensatedInventory);
                return finish(status);
            }
        }
        return finish(ItemStackResponseStatus.OK);
    }

    private ItemStackResponseStatus finish(final ItemStackResponseStatus status) {
        this.entity.serverPlayerInventoryTransactionComponent
                .setLastItemStackResponseStatus(status.value());
        return status;
    }

    private record PacketSnapshot(
            ItemCache[] inventory,
            ItemCache[] offhand,
            ItemCache[] armor,
            ItemCache[] hud,
            ItemCache[] open) {

        static PacketSnapshot capture(final CompensatedInventory inventory) {
            return new PacketSnapshot(
                    inventory.inventoryContainer.snapshotContents(),
                    inventory.offhandContainer.snapshotContents(),
                    inventory.armorContainer.snapshotContents(),
                    inventory.hudContainer.snapshotContents(),
                    inventory.openContainer == null ? null
                            : inventory.openContainer.snapshotContents());
        }

        void restore(final CompensatedInventory inventory) {
            inventory.inventoryContainer.restoreContents(this.inventory);
            inventory.offhandContainer.restoreContents(this.offhand);
            inventory.armorContainer.restoreContents(this.armor);
            inventory.hudContainer.restoreContents(this.hud);
            if (inventory.openContainer != null && this.open != null) {
                inventory.openContainer.restoreContents(this.open);
            }
        }
    }
}
