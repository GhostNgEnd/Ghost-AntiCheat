package ac.ghost.anticheat.prediction.nukkit.inventory;

import ac.ghost.anticheat.compensated.CompensatedInventory;
import ac.ghost.anticheat.compensated.cache.container.ContainerCache;
import ac.ghost.anticheat.data.inventory.ItemCache;
import ac.ghost.anticheat.prediction.bds.inventory.ItemStackRequestActionHandler;
import cn.nukkit.network.protocol.types.inventory.ContainerSlotType;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequestSlotData;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.action.TransferItemStackRequestAction;


public final class NukkitBundleRequestAdapter {
    private NukkitBundleRequestAdapter() {
    }

    public static BundleResponse process(final CompensatedInventory inventory,
                                         final TransferItemStackRequestAction action) {
        if (!isBundle(action.getDestination()) && !isBundle(action.getSource())) {
            return new BundleResponse(false, false);
        }
        if (isBundle(action.getDestination()) && isBundle(action.getSource())) {
            return new BundleResponse(true, false);
        }

        final ItemStackRequestSlotData source = action.getSource();
        final ItemStackRequestSlotData destination = action.getDestination();
        final int sourceSlot = source.getSlot();
        final int destinationSlot = destination.getSlot();
        final int count = action.getCount();
        if (sourceSlot < 0 || destinationSlot < 0 || count <= 0) {
            return new BundleResponse(true, false);
        }

        if (isBundle(source)) {
            final ContainerCache destinationContainer =
                    ItemStackRequestActionHandler.findContainer(
                            inventory, destination.getContainer());
            if (destinationContainer == null || sourceSlot >= 64
                    || destinationSlot < destinationContainer.getOffset()
                    || destinationSlot >= destinationContainer.getContainerSize()) {
                return new BundleResponse(true, false);
            }
            final ItemCache bundleItem = inventory.getBundleCache()
                    .get(source.getContainerName().getDynamicId());
            if (bundleItem == null || bundleItem.getBundle() == null) {
                return new BundleResponse(true, false);
            }
            final ItemCache item = bundleItem.getBundle().getContents()[sourceSlot];
            if (item == null || item.isEmpty() || count > item.count()) {
                return new BundleResponse(true, false);
            }
            final ItemCache destinationItem = destinationContainer.get(destinationSlot);
            if (!destinationItem.isEmpty()) {
                return new BundleResponse(true, false);
            }
            destinationContainer.set(destinationSlot, item.clone().count(count));
            if (count == item.count()) {
                bundleItem.getBundle().getContents()[sourceSlot] = ItemCache.AIR;
            } else {
                item.count(item.count() - count);
            }
            bundleItem.getBundle().count -= count;
            return new BundleResponse(true, true);
        }

        final ContainerCache sourceContainer =
                ItemStackRequestActionHandler.findContainer(
                        inventory, source.getContainer());
        if (sourceContainer == null || destinationSlot >= 64
                || sourceSlot < sourceContainer.getOffset()
                || sourceSlot >= sourceContainer.getContainerSize()) {
            return new BundleResponse(true, false);
        }
        final ItemCache bundleItem = inventory.getBundleCache()
                .get(destination.getContainerName().getDynamicId());
        if (bundleItem == null || bundleItem.getBundle() == null) {
            return new BundleResponse(true, false);
        }
        final ItemCache sourceItem = sourceContainer.get(sourceSlot);
        if (sourceItem.isEmpty() || count > sourceItem.count()) {
            return new BundleResponse(true, false);
        }
        final ItemCache moved = sourceItem.clone().count(count);
        final boolean valid = bundleItem.getBundle().add(destinationSlot, moved);
        if (!valid) {
            return new BundleResponse(true, false);
        }
        if (count == sourceItem.count()) {
            sourceContainer.set(sourceSlot, ItemCache.AIR);
        } else {
            sourceContainer.set(sourceSlot,
                    sourceItem.clone().count(sourceItem.count() - count));
        }
        return new BundleResponse(true, true);
    }

    private static boolean isBundle(final ItemStackRequestSlotData request) {
        return request != null && request.getContainerName() != null
                && request.getContainerName().getContainer()
                == ContainerSlotType.DYNAMIC_CONTAINER;
    }

    public record BundleResponse(boolean bundle, boolean valid) {
    }
}
