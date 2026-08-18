package ac.ghost.anticheat.prediction.bds.component;


public final class PlayerMovementSettingsComponent {
    private boolean serverAuthoritativeMovementStrict;
    private float playerPositionAcceptanceThreshold =
            ClientAcceptanceThresholdsComponent.DEFAULT_PLAYER_POSITION_ACCEPTANCE_THRESHOLD;
    private float playerMovementActionDirectionThreshold = 0.85F;
    private int playerRewindHistorySizeTicks = 40;

    public boolean serverAuthoritativeMovementStrict() {
        return serverAuthoritativeMovementStrict;
    }

    public void setServerAuthoritativeMovementStrict(final boolean value) {
        this.serverAuthoritativeMovementStrict = value;
    }

    public float playerPositionAcceptanceThreshold() {
        return playerPositionAcceptanceThreshold;
    }

    public void setPlayerPositionAcceptanceThreshold(final float value) {
        this.playerPositionAcceptanceThreshold = Float.isFinite(value)
                ? Math.max(0.0F, value)
                : ClientAcceptanceThresholdsComponent.DEFAULT_PLAYER_POSITION_ACCEPTANCE_THRESHOLD;
    }

    public float playerMovementActionDirectionThreshold() {
        return playerMovementActionDirectionThreshold;
    }

    public void setPlayerMovementActionDirectionThreshold(final float value) {
        this.playerMovementActionDirectionThreshold = Float.isFinite(value)
                ? value : 0.85F;
    }

    public int playerRewindHistorySizeTicks() {
        return playerRewindHistorySizeTicks;
    }

    public void setPlayerRewindHistorySizeTicks(final int value) {
        this.playerRewindHistorySizeTicks = Math.max(1, value);
    }
}
