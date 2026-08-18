package ac.ghost.anticheat.prediction.bds.system.travel;

import ac.ghost.anticheat.prediction.bds.entity.EntityContext;
import ac.ghost.anticheat.util.GhostTrigMath;
import ac.ghost.anticheat.util.MathUtil;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.entity.Entity;


public final class JumpFromGroundSystem {
    private JumpFromGroundSystem() {
    }

    public static Vec3 apply(final EntityContext entity, Vec3 velocity) {
        final float jumpPower = ApplyJumpModifierSystem.getJumpPower(entity);
        if (jumpPower <= 1.0E-5f) {
            return velocity;
        }

        velocity = new Vec3(velocity.x, Math.max(jumpPower, velocity.y), velocity.z);
        if (entity.actorDataFlagComponent.has(Entity.DATA_FLAG_SPRINTING)) {
            final float yaw = entity.actorRotationComponent.getYaw()
                    * MathUtil.DEGREE_TO_RAD;
            velocity = velocity.add(
                    -GhostTrigMath.sin(yaw) * 0.2F,
                    0,
                    GhostTrigMath.cos(yaw) * 0.2F);
        }
        return velocity;
    }
}
