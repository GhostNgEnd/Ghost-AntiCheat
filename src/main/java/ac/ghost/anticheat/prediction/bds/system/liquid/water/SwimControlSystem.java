package ac.ghost.anticheat.prediction.bds.system.liquid.water;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MoveInputComponent;
import ac.ghost.anticheat.prediction.bds.component.PlayerInputRequestComponent;
import ac.ghost.anticheat.util.MathUtil;
import ac.ghost.anticheat.util.math.Vec3;


public final class SwimControlSystem {
    private static final float NORMAL_RESPONSE = 0.06F;
    private static final float DOWNWARD_RESPONSE = 0.085F;
    private static final float ABILITY_RESPONSE = 1.3F;

    private SwimControlSystem() {
    }

    public static void tick(final GhostPlayer player) {
        player.entityContext.stateVectorComponent.setDelta(apply(player, player.entityContext.stateVectorComponent.getDelta()));
    }

    public static Vec3 apply(final GhostPlayer player, final Vec3 velocity) {
        if (!player.entityContext.actorDataFlagComponent.has(cn.nukkit.entity.Entity.DATA_FLAG_SWIMMING)) {
            return velocity;
        }

        
        
        final MoveInputComponent input = player.entityContext.moveInputComponent;
        if (input.isJumpingInLiquid()) {
            return velocity;
        }

        final float targetY = MathUtil.getRotationVector(
                player.entityContext.actorRotationComponent.getPitch(),
                player.entityContext.actorRotationComponent.getYaw()).y;

        final boolean abilityBypass =
                player.entityContext.movementAbilitiesComponent.isFlying();
        final PlayerInputRequestComponent request =
                player.entityContext.playerInputRequestComponent;
        if (targetY > 0.0F && !request.isBreathingInLiquid()
                && (!abilityBypass
                || request.isBreathingInAir()
                && !input.hasFlag(MoveInputComponent.WANT_DOWN_SLOW))) {
            return new Vec3(velocity.x, 0.0F, velocity.z);
        }

        final float response = abilityBypass
                ? ABILITY_RESPONSE
                : targetY < -0.2F ? DOWNWARD_RESPONSE : NORMAL_RESPONSE;
        return new Vec3(
                velocity.x,
                velocity.y + (targetY - velocity.y) * response,
                velocity.z);
    }
}
