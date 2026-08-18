package ac.ghost.anticheat.port.movement;

import ac.ghost.anticheat.util.math.Vec3;


public final class PendingPistonMovement {
    private Vec3 displacement;

    public synchronized void submit(final Vec3 value) {
        if (value == null || !Float.isFinite(value.x)
                || !Float.isFinite(value.y) || !Float.isFinite(value.z)) {
            return;
        }
        if (this.displacement == null) {
            this.displacement = value.clone();
        } else {
            this.displacement = this.displacement.add(value);
        }
    }

    public synchronized Vec3 take() {
        final Vec3 value = this.displacement;
        this.displacement = null;
        return value == null ? null : value.clone();
    }

    public synchronized void clear() {
        this.displacement = null;
    }
}
