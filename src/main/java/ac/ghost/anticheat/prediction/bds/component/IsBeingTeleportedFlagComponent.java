package ac.ghost.anticheat.prediction.bds.component;


public final class IsBeingTeleportedFlagComponent {
    private boolean present;
    public boolean isPresent() { return this.present; }
    public void setPresent(final boolean present) { this.present = present; }
    public void clear() { this.present = false; }
}
