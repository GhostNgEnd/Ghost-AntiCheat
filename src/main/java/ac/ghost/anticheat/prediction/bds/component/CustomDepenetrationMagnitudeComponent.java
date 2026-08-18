package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.util.math.Vec3;


public final class CustomDepenetrationMagnitudeComponent {
    private Vec3 value = Vec3.ZERO.clone();
    private boolean present;
    public boolean isPresent() { return present; }
    public Vec3 value() { return value.clone(); }
    public void set(final Vec3 value) { this.value = value == null ? Vec3.ZERO.clone() : value.clone(); this.present = true; }
    public void clear() { this.value = Vec3.ZERO.clone(); this.present = false; }
}
