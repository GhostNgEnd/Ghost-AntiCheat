package ac.ghost.anticheat.prediction.bds.component;


public final class ActorMovementTickNeededComponent {
    private PlayerInputTick inputTick;

    public void set(final PlayerInputTick inputTick) {
        this.inputTick = inputTick;
    }

    public PlayerInputTick get() {
        return inputTick;
    }

    public boolean isPresent() {
        return inputTick != null;
    }

    public void clear() {
        inputTick = null;
    }
}
