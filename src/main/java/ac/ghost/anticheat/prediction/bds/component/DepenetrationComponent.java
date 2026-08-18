package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;

import java.util.ArrayList;
import java.util.List;


public final class DepenetrationComponent {
    private int flags;
    private Vec3 magnitude = Vec3.ZERO.clone();
    private final List<Box> collisionBoxes = new ArrayList<>();
    private Vec3 customMagnitude = Vec3.ZERO.clone();
    private boolean useCustomMagnitude;

    public int flags() { return flags; }
    public void setFlags(final int flags) { this.flags = flags; }
    public Vec3 magnitude() { return magnitude.clone(); }
    public void setMagnitude(final Vec3 value) { this.magnitude = value == null ? Vec3.ZERO.clone() : value.clone(); }
    public List<Box> collisionBoxes() { return collisionBoxes; }
    public Vec3 customMagnitude() { return customMagnitude.clone(); }
    public void setCustomMagnitude(final Vec3 value) {
        this.customMagnitude = value == null ? Vec3.ZERO.clone() : value.clone();
        this.useCustomMagnitude = true;
    }
    public boolean useCustomMagnitude() { return useCustomMagnitude; }
    public void clearCustomMagnitude() { this.customMagnitude = Vec3.ZERO.clone(); this.useCustomMagnitude = false; }
}
