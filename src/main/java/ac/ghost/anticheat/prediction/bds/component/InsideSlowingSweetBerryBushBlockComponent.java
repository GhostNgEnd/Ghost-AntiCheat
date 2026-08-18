package ac.ghost.anticheat.prediction.bds.component;


public final class InsideSlowingSweetBerryBushBlockComponent {
    private boolean present;

    public boolean isPresent() {
        return present;
    }

    public void markPresent() {
        present = true;
    }

    public void clear() {
        present = false;
    }
}
