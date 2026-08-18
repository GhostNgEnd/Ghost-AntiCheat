package ac.ghost.anticheat.prediction.bds.component;

import cn.nukkit.item.Item;








public final class ItemInUseComponent {
    private boolean present;
    private Item item = Item.AIR_ITEM;
    private int itemId = -1;
    private int remainingDurationTicks;
    private int elapsedUseTicks;
    private int tridentUseTicks;

    public boolean isPresent() {
        return this.present;
    }

    public Item getItem() {
        return this.item == null ? Item.AIR_ITEM : this.item.clone();
    }

    public int getItemId() {
        return this.itemId;
    }

    public int getRemainingDurationTicks() {
        return this.remainingDurationTicks;
    }

    public int getElapsedUseTicks() {
        return this.elapsedUseTicks;
    }

    public int getTridentUseTicks() {
        return this.tridentUseTicks;
    }

    public void begin(final Item item, final int durationTicks) {
        if (item == null || item.isNull()) {
            clear();
            return;
        }
        this.present = true;
        this.item = item.clone();
        this.itemId = item.getId();
        this.remainingDurationTicks = Math.max(0, durationTicks);
        this.elapsedUseTicks = 0;
        this.tridentUseTicks = 0;
    }

    
    public void setPresent(final boolean present) {
        this.present = present && this.item != null && !this.item.isNull();
    }

    public void tickDuration() {
        if (!this.present) {
            return;
        }
        if (this.remainingDurationTicks > 0) {
            this.remainingDurationTicks--;
        }
        this.elapsedUseTicks++;
    }

    public void setTridentUseTicks(final int value) {
        this.tridentUseTicks = Math.max(0, value);
    }

    public void incrementTridentUseTicks() {
        if (this.present) {
            this.tridentUseTicks++;
        }
    }

    public void clear() {
        this.present = false;
        this.item = Item.AIR_ITEM;
        this.itemId = -1;
        this.remainingDurationTicks = 0;
        this.elapsedUseTicks = 0;
        this.tridentUseTicks = 0;
    }
}
