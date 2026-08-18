package ac.ghost.anticheat.ack.types;

import ac.ghost.anticheat.ack.Acknowledgment;
import ac.ghost.anticheat.data.inventory.ItemSnapshot;
import cn.nukkit.item.Item;

import java.util.ArrayList;
import java.util.List;


public record InventoryContentAck(
        int containerId,
        List<ItemSnapshot> contents,
        ItemSnapshot storageItem) implements Acknowledgment {

    public InventoryContentAck {
        contents = List.copyOf(contents);
    }

    public static InventoryContentAck capture(final int containerId,
                                              final Item[] contents,
                                              final Item storageItem) {
        final List<ItemSnapshot> frozen = new ArrayList<>(contents == null ? 0 : contents.length);
        if (contents != null) {
            for (final Item item : contents) {
                frozen.add(ItemSnapshot.of(item));
            }
        }
        return new InventoryContentAck(containerId, frozen, ItemSnapshot.of(storageItem));
    }
}
