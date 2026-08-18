package ac.ghost.anticheat.data.inventory;

import ac.ghost.anticheat.compensated.CompensatedInventory;
import ac.ghost.anticheat.util.ItemUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

public final class BundleData {
    private final CompensatedInventory inventory;
    @Getter
    private final ItemCache[] contents = new ItemCache[64];
    @Setter
    @Getter
    private int bundleId = -1;
    public int count = 0;

    public BundleData(final CompensatedInventory inventory) {
        this.inventory = inventory;
        Arrays.fill(this.contents, ItemCache.AIR);
    }

    public boolean add(final int slot, final ItemCache cache) {
        final ItemCache current = this.contents[slot];
        if (!current.getData().isNull()
                && !ItemUtil.sameDefinition(this.inventory.getPlayer(), cache.getData(), current.getData())) {
            return false;
        }
        if (this.count + cache.count() > 64) {
            return false;
        }

        if (current.getData().isNull()) {
            this.contents[slot] = cache.clone();
        } else {
            current.count(current.count() + cache.count());
            this.contents[slot] = current;
        }
        this.count += cache.count();
        return true;
    }
}
