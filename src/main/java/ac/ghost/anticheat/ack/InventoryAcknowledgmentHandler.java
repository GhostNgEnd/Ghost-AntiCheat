package ac.ghost.anticheat.ack;

import ac.ghost.anticheat.ack.types.InventoryContentAck;
import ac.ghost.anticheat.ack.types.InventorySlotAck;
import ac.ghost.anticheat.compensated.CompensatedInventory;
import ac.ghost.anticheat.compensated.cache.container.ContainerCache;
import ac.ghost.anticheat.data.inventory.ItemCache;
import ac.ghost.anticheat.data.inventory.ItemSnapshot;

import java.util.Objects;


public final class InventoryAcknowledgmentHandler {
    private static final int DYNAMIC_CONTAINER_ID = 125;

    private InventoryAcknowledgmentHandler() {
    }

    public static void handle(final CompensatedInventory inventory,
                              final Acknowledgment acknowledgment) {
        if (acknowledgment instanceof InventorySlotAck slot) {
            handleSlot(inventory, slot);
        } else if (acknowledgment instanceof InventoryContentAck content) {
            handleContent(inventory, content);
        }
    }

    private static void handleSlot(final CompensatedInventory inventory,
                                   final InventorySlotAck ack) {
        if (ack.containerId() == DYNAMIC_CONTAINER_ID) {
            final ItemCache bundle = bundleCacheForStorage(inventory, ack.storageItem());
            if (bundle == null || bundle.getBundle() == null) {
                return;
            }
            final ItemCache[] contents = bundle.getBundle().getContents();
            if (ack.slot() < 0 || ack.slot() >= contents.length) {
                return;
            }
            contents[ack.slot()] = ItemCache.build(inventory, ack.item());
            return;
        }

        final ContainerCache container = inventory.getContainer((byte) ack.containerId());
        if (container == null || ack.slot() < 0 || ack.slot() >= container.getContainerSize()) {
            return;
        }
        container.set(ack.slot(), ItemCache.build(inventory, ack.item()));
    }

    private static void handleContent(final CompensatedInventory inventory,
                                      final InventoryContentAck ack) {
        if (ack.containerId() == DYNAMIC_CONTAINER_ID) {
            final ItemCache bundle = bundleCacheForStorage(inventory, ack.storageItem());
            if (bundle == null || bundle.getBundle() == null) {
                return;
            }
            final ItemCache[] target = bundle.getBundle().getContents();
            final int length = Math.min(ack.contents().size(), target.length);
            for (int i = 0; i < length; i++) {
                target[i] = ItemCache.build(inventory, ack.contents().get(i));
            }
            return;
        }

        final ContainerCache container = inventory.getContainer((byte) ack.containerId());
        if (container == null) {
            return;
        }
        final ItemCache[] target = container.getContents();
        if (target == null || target.length == 0) {
            return;
        }
        final int length = Math.min(ack.contents().size(), target.length);
        for (int i = 0; i < length; i++) {
            container.set(i, ItemCache.build(inventory, ack.contents().get(i)), false);
        }
    }

    private static ItemCache bundleCacheForStorage(final CompensatedInventory inventory,
                                                   final ItemSnapshot storageItem) {
        int id;
        try {
            id = Objects.requireNonNull(storageItem.materialize().getNamedTag()).getInt("bundle_id");
        } catch (Exception ignored) {
            return null;
        }
        return inventory.getBundleCache().get(id);
    }
}
