package ac.ghost.anticheat.prediction.bds.component;


public final class RiptideTridentSpinAttackComponent {
    private int riptideLevel;
    private int remainingTicks;
    private boolean startedThisTick;
    private boolean startedOnGroundThisTick;
    private boolean stopRequested;

    public int getRiptideLevel() { return this.riptideLevel; }
    public boolean isPresent() { return this.riptideLevel > 0 || this.remainingTicks > 0; }
    public void setRiptideLevel(final int value) { this.riptideLevel = Math.max(0, value); }
    public int consumeRiptideLevel() {
        final int value = this.riptideLevel;
        this.riptideLevel = 0;
        return value;
    }
    public int getRemainingTicks() { return this.remainingTicks; }
    public void setRemainingTicks(final int value) { this.remainingTicks = Math.max(0, value); }
    public void decrementRemainingTicks() { if (this.remainingTicks > 0) this.remainingTicks--; }
    public boolean startedThisTick() { return this.startedThisTick; }
    public void setStartedThisTick(final boolean value) { this.startedThisTick = value; }
    public boolean startedOnGroundThisTick() { return this.startedOnGroundThisTick; }
    public void setStartedOnGroundThisTick(final boolean value) { this.startedOnGroundThisTick = value; }
    public boolean stopRequested() { return this.stopRequested; }
    public void setStopRequested(final boolean value) { this.stopRequested = value; }
    public void resetTickFlags() {
        this.startedThisTick = false;
        this.startedOnGroundThisTick = false;
    }
    public void clear() {
        this.riptideLevel = 0;
        this.remainingTicks = 0;
        this.startedThisTick = false;
        this.startedOnGroundThisTick = false;
        this.stopRequested = false;
    }
}
