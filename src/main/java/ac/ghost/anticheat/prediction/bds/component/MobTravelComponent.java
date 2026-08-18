package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.util.math.Vec3;


public final class MobTravelComponent {
    private float frictionForDamping = 1.0F;
    private Vec3 input = Vec3.ZERO.clone();

    public float getFrictionForDamping() { return frictionForDamping; }
    public void setFrictionForDamping(final float value) { this.frictionForDamping = value; }
    public Vec3 getInput() { return input; }
    public void setInput(final Vec3 value) { this.input = value == null ? Vec3.ZERO.clone() : value; }
}
