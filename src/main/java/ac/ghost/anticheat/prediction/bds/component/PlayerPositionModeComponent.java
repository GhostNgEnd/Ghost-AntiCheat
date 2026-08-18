package ac.ghost.anticheat.prediction.bds.component;







public final class PlayerPositionModeComponent {
    public static final int NORMAL = 0;
    public static final int RESET = 1;
    public static final int TELEPORT = 2;

    private int mode = NORMAL;

    public int getMode() {
        return this.mode;
    }

    public void setMode(final int mode) {
        this.mode = mode;
    }

    public boolean isTeleport() {
        return this.mode == TELEPORT;
    }
}
