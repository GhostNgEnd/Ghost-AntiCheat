package ac.ghost.anticheat.prediction.bds.component;

import cn.nukkit.item.Item;

import java.util.concurrent.atomic.AtomicLong;


public final class ServerPlayerInventoryTransactionComponent {
    public boolean processing;
    public final AtomicLong desyncedFlag = new AtomicLong(-1L);
    private int pendingRiptideLevel;
    private int lastInventoryTransactionError;
    private int lastItemStackResponseStatus;
    private Item pendingRiptideItem = Item.AIR_ITEM;


    public int getLastInventoryTransactionError() {
        return this.lastInventoryTransactionError;
    }

    public void setLastInventoryTransactionError(final int value) {
        this.lastInventoryTransactionError = value;
    }

    public int getLastItemStackResponseStatus() {
        return this.lastItemStackResponseStatus;
    }

    public void setLastItemStackResponseStatus(final int value) {
        this.lastItemStackResponseStatus = value;
    }

    public boolean hasPendingRiptide() {
        return this.pendingRiptideLevel > 0;
    }

    public void queueRiptide(final int chargeTicks, final int level, final Item item) {
        if (chargeTicks < 10 || level <= 0) {
            return;
        }
        this.pendingRiptideLevel = level;
        this.pendingRiptideItem = item == null ? Item.AIR_ITEM : item.clone();
    }

    public int consumePendingRiptideLevel() {
        final int level = this.pendingRiptideLevel;
        this.pendingRiptideLevel = 0;
        this.pendingRiptideItem = Item.AIR_ITEM;
        return level;
    }

    public Item pendingRiptideItem() {
        return this.pendingRiptideItem.clone();
    }

    public void clearPendingRiptide() {
        this.pendingRiptideLevel = 0;
        this.pendingRiptideItem = Item.AIR_ITEM;
    }
}
