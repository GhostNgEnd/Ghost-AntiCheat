package ac.ghost.anticheat.prediction.bds.component;


public record PlayerInputTick(long value) {
    public PlayerInputTick {
        if (value < 0L) {
            throw new IllegalArgumentException("Player input tick must be non-negative");
        }
    }
}
