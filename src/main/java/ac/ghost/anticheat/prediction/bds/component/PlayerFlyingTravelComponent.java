package ac.ghost.anticheat.prediction.bds.component;


public final class PlayerFlyingTravelComponent {
    private boolean present;
    private float surfaceFriction = 1.0F;
    private boolean idleHorizontalInput;

    public boolean isPresent() {
        return this.present;
    }

    public void setPresent(final boolean present) {
        this.present = present;
    }

    public float getSurfaceFriction() {
        return surfaceFriction;
    }

    public void setSurfaceFriction(final float surfaceFriction) {
        this.surfaceFriction = surfaceFriction;
    }

    public boolean isIdleHorizontalInput() {
        return idleHorizontalInput;
    }

    public void setIdleHorizontalInput(final boolean idleHorizontalInput) {
        this.idleHorizontalInput = idleHorizontalInput;
    }
}
