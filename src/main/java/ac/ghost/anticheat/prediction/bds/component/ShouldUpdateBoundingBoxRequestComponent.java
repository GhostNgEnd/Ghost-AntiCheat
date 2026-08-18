package ac.ghost.anticheat.prediction.bds.component;


public final class ShouldUpdateBoundingBoxRequestComponent {
    private boolean requested;

    public void request() {
        this.requested = true;
    }

    public boolean consume() {
        final boolean value = this.requested;
        this.requested = false;
        return value;
    }

    public void clear() {
        this.requested = false;
    }
}
