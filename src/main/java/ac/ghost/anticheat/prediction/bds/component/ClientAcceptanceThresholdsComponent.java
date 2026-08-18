package ac.ghost.anticheat.prediction.bds.component;





public final class ClientAcceptanceThresholdsComponent {
    public static final float DEFAULT_PLAYER_POSITION_ACCEPTANCE_THRESHOLD = 0.5F;

    
    private float positionThresholdSquared = squarePerTick(
            DEFAULT_PLAYER_POSITION_ACCEPTANCE_THRESHOLD);
    private boolean positionThresholdEnabled;

    public void setPositionThreshold(final float configuredThreshold) {
        final float configured = Float.isFinite(configuredThreshold)
                ? Math.max(0.0F, configuredThreshold)
                : DEFAULT_PLAYER_POSITION_ACCEPTANCE_THRESHOLD;
        this.positionThresholdSquared = squarePerTick(configured);
        this.positionThresholdEnabled = true;
    }


    public float positionThresholdSquared() {
        return this.positionThresholdSquared;
    }

    public boolean positionThresholdEnabled() {
        return this.positionThresholdEnabled;
    }

    public void setPositionThresholdEnabled(final boolean enabled) {
        this.positionThresholdEnabled = enabled;
    }

    private static float squarePerTick(final float configured) {
        final float perTick = configured / 100.0F;
        return perTick * perTick;
    }
}
