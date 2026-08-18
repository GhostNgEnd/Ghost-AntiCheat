package ac.ghost.anticheat.data.inventory;

import ac.ghost.anticheat.compensated.CompensatedInventory;
import ac.ghost.anticheat.util.ItemUtil;
import cn.nukkit.item.Item;
import lombok.Getter;
import lombok.Setter;










@Getter
@Setter
public class ItemCache {
    public static final ItemCache AIR = new ItemCache(ItemSnapshot.air());

    private ItemSnapshot snapshot;
    private BundleData bundle = null;

    private ItemCache(final ItemSnapshot snapshot) {
        this.snapshot = snapshot == null ? ItemSnapshot.air() : snapshot;
    }

    
    public Item getData() {
        return this.snapshot.materialize();
    }

    
    public void setData(final Item data) {
        this.snapshot = ItemSnapshot.of(data);
    }

    public ItemCache count(final int count) {
        this.snapshot = this.snapshot.withCount(count);
        return this;
    }

    public int count() {
        return this.snapshot.count();
    }

    public boolean isEmpty() {
        return this.snapshot.isEmpty();
    }

    @Override
    public ItemCache clone() {
        
        
        final ItemCache cache = new ItemCache(this.snapshot);
        cache.setBundle(this.bundle);
        return cache;
    }

    public static ItemCache build(final CompensatedInventory inventory,
                                  final Item data) {
        return build(inventory, ItemSnapshot.of(data));
    }

    public static ItemCache build(final CompensatedInventory inventory,
                                  final ItemSnapshot snapshot) {
        final ItemSnapshot frozen = snapshot == null ? ItemSnapshot.air() : snapshot;
        final Item item = frozen.materialize();
        final ItemCache cache = new ItemCache(frozen);
        if (!ItemUtil.isBundle(inventory.getPlayer(), item)) {
            return cache;
        }

        int id = -1;
        try {
            if (item.getNamedTag() != null) {
                id = item.getNamedTag().getInt("bundle_id");
            }
        } catch (Exception ignored) {
        }
        if (id == -1 || inventory.getBundleCache().containsKey(id)) {
            return cache;
        }

        final BundleData bundle = new BundleData(inventory);
        bundle.setBundleId(id);
        cache.setBundle(bundle);
        inventory.getBundleCache().put(id, cache);
        return cache;
    }
}
