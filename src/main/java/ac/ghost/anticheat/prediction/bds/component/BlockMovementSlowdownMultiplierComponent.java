package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.util.math.Vec3;





public final class BlockMovementSlowdownMultiplierComponent {
    private final Vec3 multiplier = Vec3.ZERO.clone();
    private boolean present;

    public void add(final Vec3 value) {
        if (value == null) {
            return;
        }
        if (!present) {
            multiplier.x = value.x;
            multiplier.y = value.y;
            multiplier.z = value.z;
            present = true;
            return;
        }
        multiplier.x = Math.min(multiplier.x, value.x);
        multiplier.y = Math.min(multiplier.y, value.y);
        multiplier.z = Math.min(multiplier.z, value.z);
    }

    public void set(final Vec3 value) {
        if (value == null) {
            clear();
            return;
        }
        multiplier.x = value.x;
        multiplier.y = value.y;
        multiplier.z = value.z;
        present = true;
    }

    public boolean isPresent() {
        return present;
    }

    public Vec3 value() {
        return multiplier;
    }

    public void clear() {
        multiplier.x = 0.0F;
        multiplier.y = 0.0F;
        multiplier.z = 0.0F;
        present = false;
    }
}
