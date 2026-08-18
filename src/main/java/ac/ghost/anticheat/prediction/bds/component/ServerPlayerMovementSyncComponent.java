package ac.ghost.anticheat.prediction.bds.component;









public final class ServerPlayerMovementSyncComponent {
    
    private boolean correctionRequested;
    
    private byte correctionState;
    
    
    private long clientBoundPacketTick;

    public boolean correctionRequested() {
        return this.correctionRequested;
    }

    public void requestCorrection() {
        this.correctionRequested = true;
    }

    public byte correctionState() {
        return this.correctionState;
    }

    public void setCorrectionState(final byte value) {
        this.correctionState = value;
    }

    public void clearCorrectionState() {
        this.correctionState = 0;
    }

    public long clientBoundPacketTick() {
        return Math.max(0L, this.clientBoundPacketTick);
    }

    public void setClientBoundPacketTick(final long value) {
        this.clientBoundPacketTick = Math.max(0L, value);
    }

    
    public void finishCorrectionTick() {
        this.correctionRequested = false;
    }

    public void reset() {
        this.correctionRequested = false;
        this.correctionState = 0;
        this.clientBoundPacketTick = 0L;
    }
}
