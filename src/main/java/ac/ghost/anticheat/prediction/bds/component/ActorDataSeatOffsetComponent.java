package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.util.math.Vec3;


public final class ActorDataSeatOffsetComponent {
    private Vec3 value = Vec3.ZERO.clone();
    public Vec3 getValue() { return value.clone(); }
    public void setValue(final Vec3 value) { this.value = value == null ? Vec3.ZERO.clone() : value.clone(); }
}
