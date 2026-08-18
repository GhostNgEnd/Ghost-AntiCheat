package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.util.math.Vec3;









public final class StateVectorComponent {
    private Vec3 position = Vec3.ZERO.clone();
    private Vec3 previousPosition = Vec3.ZERO.clone();
    private Vec3 delta = Vec3.ZERO.clone();
    private boolean initialized;

    public void initialize(final Vec3 position) {
        final Vec3 value = position == null ? Vec3.ZERO.clone() : position.clone();
        this.position = value;
        this.previousPosition = value.clone();
        this.delta = Vec3.ZERO.clone();
        this.initialized = true;
    }

    
    public void beginTick() {
        if (!this.initialized) {
            initialize(Vec3.ZERO);
            return;
        }
        this.previousPosition = this.position.clone();
    }

    public Vec3 getPosition() { return position; }
    public void setPosition(final Vec3 value) {
        this.position = value == null ? Vec3.ZERO.clone() : value;
        this.initialized = true;
    }
    public Vec3 getPreviousPosition() { return previousPosition; }
    public void setPreviousPosition(final Vec3 value) {
        this.previousPosition = value == null ? Vec3.ZERO.clone() : value;
        this.initialized = true;
    }
    public Vec3 getDelta() { return delta; }
    public void setDelta(final Vec3 value) {
        this.delta = value == null ? Vec3.ZERO.clone() : value;
        this.initialized = true;
    }
}
