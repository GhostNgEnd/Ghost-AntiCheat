package ac.ghost.anticheat.compensated.cache.container;

import ac.ghost.anticheat.compensated.CompensatedInventory;
import ac.ghost.anticheat.data.inventory.ItemCache;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.item.Item;
import cn.nukkit.math.BlockVector3;
import lombok.Getter;

import java.util.Arrays;

public class ContainerCache {
    public static final byte OFFHAND = (byte) 119;
    public static final byte UI = (byte) 124;

    protected final CompensatedInventory inventory;

    @Getter
    private final byte id;
    @Getter
    private final InventoryType type;
    @Getter
    private final BlockVector3 blockPosition;
    @Getter
    private final long uniqueEntityId;

    private final int containerSize;
    @Getter
    private final int offset;
    @Getter
    private final ItemCache[] contents;

    public ContainerCache(CompensatedInventory inventory, byte id, InventoryType type,
                          BlockVector3 blockPosition, long uniqueEntityId) {
        this.inventory = inventory;
        this.id = id;
        this.type = type;
        this.blockPosition = blockPosition;
        this.uniqueEntityId = uniqueEntityId;
        this.offset = offsetFor(type);
        this.containerSize = sizeFor(type);
        
        
        
        
        this.contents = new ItemCache[Math.max(0, this.containerSize)];
        Arrays.fill(this.contents, ItemCache.AIR);
    }

    private static int offsetFor(final InventoryType type) {
        if (type == null) {
            return 0;
        }
        return switch (type) {
            case ENCHANT_TABLE -> 14;
            case LOOM -> 9;
            case WORKBENCH -> 32;
            case BEACON -> 27;
            case ANVIL -> 1;
            case STONECUTTER -> 3;
            case CARTOGRAPHY -> 12;
            case SMITHING_TABLE -> 51;
            case GRINDSTONE -> 16;
            case TRADING -> 4;
            default -> 0;
        };
    }

    private static int sizeFor(final InventoryType type) {
        if (type == null) {
            return 36;
        }
        return switch (type) {
            case FURNACE, BLAST_FURNACE, SMOKER, LOOM, SMITHING_TABLE -> 3;
            case BREWING_STAND, HOPPER, MINECART_HOPPER -> 5;
            case DROPPER, DISPENSER, WORKBENCH, CRAFTER -> 9;
            case ENCHANT_TABLE, ANVIL, HORSE, CARTOGRAPHY, GRINDSTONE -> 2;
            case BEACON, STONECUTTER -> 1;
            case COMMAND_BLOCK -> 0;
            case MINECART_CHEST, CHEST_BOAT -> 26;
            case CHEST, ENDER_CHEST, DOUBLE_CHEST, SHULKER_BOX, BARREL -> 56;
            case TRADING -> 2;
            case ENTITY_ARMOR -> 4;
            default -> 36;
        };
    }

    public int getContainerSize() {
        return this.containerSize + this.offset;
    }

    public ItemCache get(final int slot) {
        final int index = slot - this.offset;
        if (this.contents == null || index < 0 || index >= this.contents.length) {
            return ItemCache.AIR;
        }

        final ItemCache cache = this.contents[index];
        return cache == null ? ItemCache.AIR : cache;
    }


    public ItemCache[] snapshotContents() {
        final ItemCache[] snapshot = new ItemCache[this.contents.length];
        for (int i = 0; i < this.contents.length; i++) {
            final ItemCache item = this.contents[i];
            snapshot[i] = item == null ? ItemCache.AIR : item.clone();
        }
        return snapshot;
    }

    public void restoreContents(final ItemCache[] snapshot) {
        if (snapshot == null || snapshot.length != this.contents.length) {
            return;
        }
        for (int i = 0; i < snapshot.length; i++) {
            final ItemCache item = snapshot[i];
            this.contents[i] = item == null ? ItemCache.AIR : item.clone();
        }
    }

    public void set(final int slot, final Item raw) {
        this.set(slot, raw, true);
    }

    public void set(final int slot, final ItemCache raw) {
        this.set(slot, raw, true);
    }

    public void set(final int slot, final Item raw, final boolean offset) {
        this.set(slot, ItemCache.build(this.inventory, raw), offset);
    }

    public void set(final int slot, final ItemCache cache, final boolean offset) {
        final int index = offset ? slot - this.offset : slot;
        if (this.contents == null || index < 0 || index >= this.contents.length) {
            return;
        }

        this.contents[index] = cache == null ? ItemCache.AIR : cache;
    }
}
