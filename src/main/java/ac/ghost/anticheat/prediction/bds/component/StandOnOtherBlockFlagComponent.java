package ac.ghost.anticheat.prediction.bds.component;


public final class StandOnOtherBlockFlagComponent {
    private boolean present;

    public boolean isPresent() {
        return present;
    }

    public void setPresent(final boolean present) {
        this.present = present;
    }

    public void clear() {
        this.present = false;
    }
}
