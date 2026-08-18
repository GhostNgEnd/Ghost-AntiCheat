package ac.ghost.anticheat.prediction.bds.system.liquid.lava;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.system.travel.AutoClimbSystem;
import ac.ghost.anticheat.util.GhostTrigMath;
import ac.ghost.anticheat.util.MathUtil;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.entity.Entity;

public final class LavaMoveSystem {
    private LavaMoveSystem() {
    }

    public static Vec3 tick(final GhostPlayer player,
                            final Vec3 velocity,
                            final float strength) {
        final Vec3 input = player.entityContext.mobTravelComponent.getInput();
        final float len2 = input.lengthSquared();
        Vec3 moved = velocity;
        if (len2 >= 0.0001F) {
            final float scale = strength / Math.max((float) Math.sqrt(len2), 1.0F);
            final Vec3 local = input.multiply(scale);
            final float yaw = player.entityContext.actorRotationComponent.getYaw() * MathUtil.DEGREE_TO_RAD;
            final float sin = GhostTrigMath.sin(yaw);
            final float cos = GhostTrigMath.cos(yaw);
            moved = velocity.add(
                    local.x * cos - local.z * sin,
                    local.y,
                    local.z * cos + local.x * sin);
        }

        final boolean climbing = AutoClimbSystem.isClimbing(player);
        final float vertical = applyVerticalStage(player, moved.y, climbing);

        
        
        
        
        return new Vec3(moved.x, vertical, moved.z);
    }

    static float applyVerticalStage(final GhostPlayer player,
                                    float vertical,
                                    final boolean climbing) {
        if (!climbing) {
            return vertical;
        }

        vertical = Math.max(vertical, -0.2F);
        if (vertical < 0.0F
                && player.entityContext.actorDataFlagComponent.has(Entity.DATA_FLAG_SNEAKING)
                && AutoClimbSystem.hasClimbHaltPropertyAtFeet(player)) {
            vertical = 0.0F;
        }
        return vertical;
    }
}
