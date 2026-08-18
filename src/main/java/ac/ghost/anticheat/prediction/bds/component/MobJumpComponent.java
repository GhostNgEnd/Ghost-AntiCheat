package ac.ghost.anticheat.prediction.bds.component;









public final class MobJumpComponent {
    private int noJumpDelay;

    public int getNoJumpDelay() {
        return noJumpDelay;
    }

    public void setNoJumpDelay(final int noJumpDelay) {
        this.noJumpDelay = Math.max(0, noJumpDelay);
    }
}
