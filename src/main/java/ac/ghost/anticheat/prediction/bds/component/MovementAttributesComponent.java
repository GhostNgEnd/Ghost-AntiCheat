package ac.ghost.anticheat.prediction.bds.component;





public final class MovementAttributesComponent {
    private float movementSpeed;
    private boolean hasMovementSpeed;

    public boolean hasMovementSpeed() {
        return hasMovementSpeed;
    }

    public float getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(final float movementSpeed) {
        this.movementSpeed = movementSpeed;
        this.hasMovementSpeed = true;
    }

    public void clearMovementSpeed() {
        this.movementSpeed = 0.0F;
        this.hasMovementSpeed = false;
    }
}
