package ac.ghost.anticheat.prediction.nukkit.component;





public final class NukkitItemUseStateComponent {
    public enum PendingUseSource {
        NONE,
        INVENTORY_TRANSACTION,
        METADATA
    }

    private PendingUseSource pendingUseSource = PendingUseSource.NONE;
    private boolean useButtonLatched;
    private boolean buttonReleasePending;
    private int pendingMetadataFalsePackets;
    private long lastMetadataTrueSentPlayerTick = Long.MIN_VALUE;
    private long lastMetadataFalseAckPlayerTick = Long.MIN_VALUE;
    private boolean awaitingMetadataStartConfirmation;
    private boolean predictionUsingItem;
    private int useHotbarSlot = -1;
    private boolean noSlowConsumeRollbackPending;

    public PendingUseSource getPendingUseSource() {
        return pendingUseSource;
    }

    public void setPendingUseSource(final PendingUseSource pendingUseSource) {
        this.pendingUseSource = pendingUseSource == null
                ? PendingUseSource.NONE : pendingUseSource;
    }

    public boolean isUseButtonLatched() {
        return useButtonLatched;
    }

    public void setUseButtonLatched(final boolean useButtonLatched) {
        this.useButtonLatched = useButtonLatched;
    }

    public boolean isButtonReleasePending() {
        return buttonReleasePending;
    }

    public void setButtonReleasePending(final boolean buttonReleasePending) {
        this.buttonReleasePending = buttonReleasePending;
    }

    public int getPendingMetadataFalsePackets() {
        return pendingMetadataFalsePackets;
    }

    public void incrementPendingMetadataFalsePackets() {
        this.pendingMetadataFalsePackets++;
    }

    public void decrementPendingMetadataFalsePackets() {
        if (this.pendingMetadataFalsePackets > 0) {
            this.pendingMetadataFalsePackets--;
        }
    }

    public long getLastMetadataTrueSentPlayerTick() {
        return lastMetadataTrueSentPlayerTick;
    }

    public void setLastMetadataTrueSentPlayerTick(final long value) {
        this.lastMetadataTrueSentPlayerTick = value;
    }

    public long getLastMetadataFalseAckPlayerTick() {
        return lastMetadataFalseAckPlayerTick;
    }

    public void setLastMetadataFalseAckPlayerTick(final long value) {
        this.lastMetadataFalseAckPlayerTick = value;
    }

    public boolean isAwaitingMetadataStartConfirmation() {
        return awaitingMetadataStartConfirmation;
    }

    public void setAwaitingMetadataStartConfirmation(final boolean value) {
        this.awaitingMetadataStartConfirmation = value;
    }

    public boolean isPredictionUsingItem() {
        return predictionUsingItem;
    }

    public void setPredictionUsingItem(final boolean value) {
        this.predictionUsingItem = value;
    }

    public int getUseHotbarSlot() {
        return useHotbarSlot;
    }

    public void setUseHotbarSlot(final int useHotbarSlot) {
        this.useHotbarSlot = useHotbarSlot;
    }


    public boolean isNoSlowConsumeRollbackPending() {
        return noSlowConsumeRollbackPending;
    }

    public void setNoSlowConsumeRollbackPending(final boolean value) {
        this.noSlowConsumeRollbackPending = value;
    }

    public void reset() {
        this.pendingUseSource = PendingUseSource.NONE;
        this.useButtonLatched = false;
        this.buttonReleasePending = false;
        this.pendingMetadataFalsePackets = 0;
        this.lastMetadataTrueSentPlayerTick = Long.MIN_VALUE;
        this.lastMetadataFalseAckPlayerTick = Long.MIN_VALUE;
        this.awaitingMetadataStartConfirmation = false;
        this.predictionUsingItem = false;
        this.useHotbarSlot = -1;
        this.noSlowConsumeRollbackPending = false;
    }
}
