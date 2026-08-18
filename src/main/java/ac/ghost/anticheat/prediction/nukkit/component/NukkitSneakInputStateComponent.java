package ac.ghost.anticheat.prediction.nukkit.component;








public final class NukkitSneakInputStateComponent {
    private boolean rawSneaking;
    private boolean initialized;

    public boolean isRawSneaking() {
        return rawSneaking;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setRawSneaking(final boolean rawSneaking) {
        this.rawSneaking = rawSneaking;
        this.initialized = true;
    }

    public void reset() {
        this.rawSneaking = false;
        this.initialized = false;
    }
}
