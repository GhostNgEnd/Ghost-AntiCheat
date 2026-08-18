package ac.ghost.anticheat.prediction.bds.component;


public final class BlockMovementSlowdownAppliedComponent {
    private boolean present;

    public boolean isPresent() {
        return present;
    }

    public void markApplied() {
        present = true;
    }

    public void clear() {
        present = false;
    }
}
