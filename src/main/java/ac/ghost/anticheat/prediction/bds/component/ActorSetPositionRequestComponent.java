package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.util.math.Vec3;


public final class ActorSetPositionRequestComponent {
    private Vec3 position = Vec3.ZERO.clone();
    private boolean present;
    public boolean isPresent() { return present; }
    public Vec3 position() { return position.clone(); }
    public void set(final Vec3 position) { this.position = position == null ? Vec3.ZERO.clone() : position.clone(); this.present = true; }
    public void clear() { this.position = Vec3.ZERO.clone(); this.present = false; }
}
