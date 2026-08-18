package ac.ghost.anticheat.prediction.bds.component;


public final class SwimAmountComponent {
    private float previous;
    private float current;

    public float getPrevious() {
        return previous;
    }

    public void setPrevious(final float previous) {
        this.previous = previous;
    }

    public float getCurrent() {
        return current;
    }

    public void setCurrent(final float current) {
        this.current = current;
    }
}
