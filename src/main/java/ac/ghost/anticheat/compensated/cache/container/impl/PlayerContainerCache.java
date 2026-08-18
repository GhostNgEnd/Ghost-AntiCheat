package ac.ghost.anticheat.compensated.cache.container.impl;

import ac.ghost.anticheat.compensated.CompensatedInventory;
import ac.ghost.anticheat.compensated.cache.container.ContainerCache;
import ac.ghost.anticheat.data.inventory.ItemCache;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.item.Item;

public class PlayerContainerCache extends ContainerCache {
    public static final byte INVENTORY_ID = 0;

    public PlayerContainerCache(final CompensatedInventory inventory) {
        super(inventory, INVENTORY_ID, InventoryType.PLAYER, null, -1L);
    }

    public ItemCache getHeldItemCache() {
        return this.getItemFromSlot(this.inventory.heldItemSlot);
    }

    public Item getHeldItemData() {
        return this.getItemFromSlot(this.inventory.heldItemSlot).getData();
    }

    public Item getHeldItem() {
        return getHeldItemData();
    }

    public ItemCache getItemFromSlot(final int slot) {
        if (slot < 0 || slot > 8 || slot >= this.getContainerSize()) {
            return ItemCache.AIR;
        }

        return this.get(slot);
    }
}
