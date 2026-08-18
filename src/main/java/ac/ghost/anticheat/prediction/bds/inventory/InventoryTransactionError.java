package ac.ghost.anticheat.prediction.bds.inventory;


public enum InventoryTransactionError {
    NONE(0),
    BALANCE_MISMATCH(2),
    SOURCE_ITEM_MISMATCH(3),
    STATE_MISMATCH(7);

    private final int value;

    InventoryTransactionError(final int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public boolean isSuccess() {
        return this == NONE;
    }
}
