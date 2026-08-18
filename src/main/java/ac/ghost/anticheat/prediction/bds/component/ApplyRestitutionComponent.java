package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.util.math.Vec3;


public final class ApplyRestitutionComponent {
    private final Vec3 velocity = Vec3.ZERO.clone();

    public Vec3 velocity() {
        return velocity;
    }

    public boolean hasRestitution() {
        return velocity.lengthSquared() > 1.0E-14F;
    }
}
