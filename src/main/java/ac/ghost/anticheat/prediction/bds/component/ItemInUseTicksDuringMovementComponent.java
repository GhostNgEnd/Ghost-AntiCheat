package ac.ghost.anticheat.prediction.bds.component;





public final class ItemInUseTicksDuringMovementComponent {
    private long lastMovementServerTick = Long.MIN_VALUE;

    public void mark(final long serverTick) {
        this.lastMovementServerTick = serverTick;
    }

    public boolean wasTicked(final long serverTick) {
        return this.lastMovementServerTick == serverTick;
    }

    public void clear() {
        this.lastMovementServerTick = Long.MIN_VALUE;
    }
}
