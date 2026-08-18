package ac.ghost.anticheat.prediction.bds.component;


public final class PushableByBlockComponent {
    private boolean present = true;

    public boolean isPresent() {
        return this.present;
    }

    public void setPresent(final boolean present) {
        this.present = present;
    }
}
