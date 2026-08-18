package ac.ghost.anticheat.prediction.bds.system.liquid.water;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.system.restitution.ApplyGravityWithBounceCorrection;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.potion.Effect;


public final class MobMovementDrag {
    private static final float NORMAL_HORIZONTAL_DRAG = 0.8F;
    private static final float SPRINT_HORIZONTAL_DRAG = 0.9F;
    private static final float VERTICAL_DRAG = 0.8F;
    private static final float DEPTH_TARGET_DRAG = 0.54600006F;

    private MobMovementDrag() {
    }

    public static void tick(final GhostPlayer player, final float depthRatio) {
        final float base = player.entityContext.actorDataFlagComponent.has(cn.nukkit.entity.Entity.DATA_FLAG_SPRINTING)
                ? SPRINT_HORIZONTAL_DRAG
                : Float.isFinite(player.entityContext.waterMovementComponent.getDrag())
                ? player.entityContext.waterMovementComponent.getDrag()
                : NORMAL_HORIZONTAL_DRAG;
        final float horizontal = player.entityContext.swimSpeedMultiplierComponent.getValue() > 1.0F
                ? base
                : base + (DEPTH_TARGET_DRAG - base) * depthRatio;
        player.entityContext.stateVectorComponent.setDelta(player.entityContext.stateVectorComponent.getDelta().multiply(horizontal, VERTICAL_DRAG, horizontal));
        player.entityContext.stateVectorComponent.setDelta(applyFallingAdjustment(player, player.entityContext.stateVectorComponent.getDelta()));
    }

    private static Vec3 applyFallingAdjustment(final GhostPlayer player,
                                               final Vec3 motion) {
        if (player.entityContext.mobEffectsComponent.has(Effect.LEVITATION)) {
            ApplyGravityWithBounceCorrection.clear(player);
            final float target = (player.entityContext.mobEffectsComponent.get(Effect.LEVITATION).getAmplifier() + 1) * 0.05F;
            return new Vec3(motion.x, motion.y + (target - motion.y) * 0.2F, motion.z);
        }
        final float gravity = player.entityContext.mobEffectsComponent.effectiveGravity(player.entityContext.stateVectorComponent.getDelta());
        if (gravity != 0.0F && !player.entityContext.actorDataFlagComponent.has(cn.nukkit.entity.Entity.DATA_FLAG_SWIMMING)) {
            final float gravityDelta = ApplyGravityWithBounceCorrection.resolveGravityDelta(
                    player, -gravity / 16.0F);
            return new Vec3(motion.x, motion.y + gravityDelta, motion.z);
        }
        ApplyGravityWithBounceCorrection.clear(player);
        return motion;
    }
}
