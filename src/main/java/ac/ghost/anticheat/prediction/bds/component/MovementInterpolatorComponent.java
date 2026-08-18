package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.util.math.Vec3;


public final class MovementInterpolatorComponent {
    private Vec3 targetPosition = Vec3.ZERO.clone();
    private float pitch;
    private float yaw;
    private float headYaw;
    private boolean dirty;

    public void set(final Vec3 position, final float pitch,
                    final float yaw, final float headYaw) {
        this.targetPosition = position == null ? Vec3.ZERO.clone() : position.clone();
        this.pitch = pitch;
        this.yaw = yaw;
        this.headYaw = headYaw;
        this.dirty = true;
    }

    public Vec3 targetPosition() { return this.targetPosition.clone(); }
    public float pitch() { return this.pitch; }
    public float yaw() { return this.yaw; }
    public float headYaw() { return this.headYaw; }
    public boolean isDirty() { return this.dirty; }
    public void clearDirty() { this.dirty = false; }

    public void reset(final Vec3 position, final float pitch,
                      final float yaw, final float headYaw) {
        this.targetPosition = position == null ? Vec3.ZERO.clone() : position.clone();
        this.pitch = pitch;
        this.yaw = yaw;
        this.headYaw = headYaw;
        this.dirty = false;
    }
}
