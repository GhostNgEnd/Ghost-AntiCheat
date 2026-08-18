package ac.ghost.anticheat.prediction.bds.component;


public final class PermissionFlyFlagComponent {
    private boolean present;

    public boolean isPresent() {
        return this.present;
    }

    public void setPresent(final boolean present) {
        this.present = present;
    }

    public void clear() {
        this.present = false;
    }
}
