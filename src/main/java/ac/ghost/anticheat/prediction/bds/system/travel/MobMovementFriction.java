package ac.ghost.anticheat.prediction.bds.system.travel;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MobTravelComponent;
import ac.ghost.anticheat.prediction.bds.component.PlayerFlyingTravelComponent;
import ac.ghost.anticheat.prediction.bds.component.StateVectorComponent;
import ac.ghost.anticheat.prediction.bds.math.BdsMovementMath;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.network.protocol.types.GameType;


public final class MobMovementFriction {
    private static final float HORIZONTAL_FRICTION_SCALE =
            Float.intBitsToFloat(0x3F68F5C3);
    private static final float NORMAL_TRAVEL_VERTICAL_FACTOR =
            Float.intBitsToFloat(0x3F7AE148);
    private static final float CREATIVE_IDLE_HORIZONTAL_DAMPING =
            Float.intBitsToFloat(0x3EC00000);
    private static final float OTHER_IDLE_HORIZONTAL_DAMPING =
            Float.intBitsToFloat(0x3F400000);

    private MobMovementFriction() {
    }

    public static void tickGroundOrAir(final StateVectorComponent stateVector,
                                       final MobTravelComponent mobTravel) {
        final Vec3 velocity = stateVector.getDelta();
        final float horizontalFactor = mobTravel.getFrictionForDamping()
                * HORIZONTAL_FRICTION_SCALE;
        applyHorizontal(velocity, horizontalFactor);
        applyVertical(velocity, NORMAL_TRAVEL_VERTICAL_FACTOR);
    }

    public static void tickPlayerFlying(final GhostPlayer player,
                                        final StateVectorComponent stateVector,
                                        final PlayerFlyingTravelComponent flyingTravel) {
        float horizontalFactor = flyingTravel.getSurfaceFriction()
                * HORIZONTAL_FRICTION_SCALE;
        if (flyingTravel.isIdleHorizontalInput()) {
            final float idleOverride = player.entityContext.actorGameTypeComponent.value == GameType.CREATIVE
                    ? CREATIVE_IDLE_HORIZONTAL_DAMPING
                    : OTHER_IDLE_HORIZONTAL_DAMPING;
            horizontalFactor = horizontalFactor * idleOverride;
        }
        applyHorizontalAfterFriction(
                stateVector.getDelta(), horizontalFactor);
    }

    private static void applyHorizontal(final Vec3 velocity,
                                        final float horizontalFactor) {
        final float horizontalX = velocity.x;
        if (Math.abs(horizontalX) <= BdsMovementMath.FLOAT_EPSILON) {
            velocity.x = 0.0F;
        } else {
            velocity.x = horizontalX * horizontalFactor;
        }

        final float horizontalZ = velocity.z;
        if (Math.abs(horizontalZ) <= BdsMovementMath.FLOAT_EPSILON) {
            velocity.z = 0.0F;
        } else {
            velocity.z = horizontalZ * horizontalFactor;
        }
    }

    private static void applyVertical(final Vec3 velocity,
                                      final float verticalFactor) {
        final float verticalY = velocity.y;
        if (Math.abs(verticalY) <= BdsMovementMath.FLOAT_EPSILON) {
            velocity.y = 0.0F;
        } else {
            velocity.y = verticalY * verticalFactor;
        }
    }

    private static void applyHorizontalAfterFriction(
            final Vec3 velocity,
            final float horizontalFactor) {
        final float multipliedX = velocity.x * horizontalFactor;
        velocity.x = Math.abs(multipliedX) <= BdsMovementMath.FLOAT_EPSILON
                ? 0.0F
                : multipliedX;

        final float multipliedZ = velocity.z * horizontalFactor;
        velocity.z = Math.abs(multipliedZ) <= BdsMovementMath.FLOAT_EPSILON
                ? 0.0F
                : multipliedZ;
    }
}
