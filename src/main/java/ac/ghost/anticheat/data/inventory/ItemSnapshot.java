package ac.ghost.anticheat.data.inventory;

import cn.nukkit.item.Item;










public final class ItemSnapshot {
    private static final ItemSnapshot AIR = new ItemSnapshot(Item.AIR_ITEM);

    private final Item frozen;

    private ItemSnapshot(final Item item) {
        this.frozen = freeze(item);
    }

    public static ItemSnapshot air() {
        return AIR;
    }

    public static ItemSnapshot of(final Item item) {
        if (item == null || item.isNull()) {
            return AIR;
        }
        return new ItemSnapshot(item);
    }

    public Item materialize() {
        return freeze(this.frozen);
    }

    public boolean isEmpty() {
        return this.frozen == null || this.frozen.isNull() || this.frozen.getCount() <= 0;
    }

    public int count() {
        return this.frozen == null ? 0 : this.frozen.getCount();
    }

    public ItemSnapshot withCount(final int count) {
        final Item copy = materialize();
        copy.setCount(count);
        return of(copy);
    }

    private static Item freeze(final Item item) {
        final Item source = item == null ? Item.AIR_ITEM : item;
        final Item copy = source.clone();
        return copy == null ? Item.AIR_ITEM.clone() : copy;
    }
}
