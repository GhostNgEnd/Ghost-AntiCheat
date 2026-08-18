package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.DepenetrationComponent;
import ac.ghost.anticheat.util.math.Vec3;


public final class ConfigureDepenetration {
    private ConfigureDepenetration() {}
    public static void run(final GhostPlayer player) {
        final DepenetrationComponent dep = player.entityContext.depenetrationComponent;
        final int flags = dep.flags();
        final Vec3 baseline;
        if ((flags & 0x08) != 0) {
            baseline = new Vec3(1.0F, 1.0F, 1.0F);
        } else if (!dep.collisionBoxes().isEmpty()) {
            baseline = Vec3.ZERO.clone();
        } else {
            baseline = (flags & 0x15) != 0
                    ? Vec3.ZERO.clone()
                    : new Vec3(1.0F, 1.0F, 1.0F);
        }
        if (player.entityContext.customDepenetrationMagnitudeComponent.isPresent()) {
            dep.setCustomMagnitude(player.entityContext.customDepenetrationMagnitudeComponent.value());
        }
        Vec3 value = max(baseline, dep.magnitude());
        if (dep.useCustomMagnitude()) value = max(value, dep.customMagnitude());
        player.entityContext.moveRequestComponent.setDepenetrationMagnitude(value);
    }
    private static Vec3 max(final Vec3 a, final Vec3 b) {
        return new Vec3(Math.max(a.x,b.x), Math.max(a.y,b.y), Math.max(a.z,b.z));
    }
}
