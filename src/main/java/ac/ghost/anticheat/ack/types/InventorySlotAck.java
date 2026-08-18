package ac.ghost.anticheat.ack.types;

import ac.ghost.anticheat.ack.Acknowledgment;
import ac.ghost.anticheat.data.inventory.ItemSnapshot;
import cn.nukkit.item.Item;


public record InventorySlotAck(
        int containerId,
        int slot,
        ItemSnapshot item,
        ItemSnapshot storageItem) implements Acknowledgment {

    public static InventorySlotAck capture(final int containerId,
                                           final int slot,
                                           final Item item,
                                           final Item storageItem) {
        return new InventorySlotAck(containerId, slot,
                ItemSnapshot.of(item), ItemSnapshot.of(storageItem));
    }
}
