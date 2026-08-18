package ac.ghost.anticheat.prediction.bds.system.travel;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MobTravelComponent;
import ac.ghost.anticheat.prediction.bds.component.MovementSpeedComponent;
import ac.ghost.anticheat.prediction.bds.component.StateVectorComponent;
import ac.ghost.anticheat.prediction.bds.math.BdsMovementMath;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.BlockVector3;


public final class DefaultMoveSystems {
    private static final float AIR_DAMPING_FRICTION = Float.intBitsToFloat(0x3F800000);
    private static final float CLIMB_DOWN_LIMIT = Float.intBitsToFloat(0xBE4CCCCD);

    private DefaultMoveSystems() {
    }

    public static void tickGroundOrAir(final GhostPlayer player,
                                       final StateVectorComponent stateVector,
                                       final MovementSpeedComponent movementSpeed,
                                       final MobTravelComponent mobTravel) {
        final Vec3 velocity = moveRelative(
                stateVector.getDelta(),
                mobTravel.getInput(),
                movementSpeed.getValue(),
                player.entityContext.actorRotationComponent.getYaw());
        stateVector.setDelta(velocity);
        mobTravel.setFrictionForDamping(sampleFrictionForDamping(player));
        applyClimbDownClamp(player, stateVector, velocity);
    }

    public static void tickFlyingPlayer(final GhostPlayer player,
                                        final StateVectorComponent stateVector,
                                        final MovementSpeedComponent movementSpeed,
                                        final MobTravelComponent mobTravel) {
        stateVector.setDelta(moveRelative(
                stateVector.getDelta(),
                mobTravel.getInput(),
                movementSpeed.getValue(),
                player.entityContext.actorRotationComponent.getYaw()));
    }

    private static Vec3 moveRelative(final Vec3 startVelocity,
                                     final Vec3 input,
                                     final float moveSpeed,
                                     final float yawDegrees) {
        final float inputX = input.x;
        final float inputY = input.y;
        final float inputZ = input.z;

        final float xSquared = inputX * inputX;
        final float ySquared = inputY * inputY;
        final float xySquared = ySquared + xSquared;
        final float zSquared = inputZ * inputZ;
        final float lengthSquared = zSquared + xySquared;

        if (lengthSquared < BdsMovementMath.INPUT_EPSILON_SQUARED) {
            return startVelocity.clone();
        }

        final float length = BdsMovementMath.sqrtf(lengthSquared);
        final float denominator = BdsMovementMath.maxss(
                BdsMovementMath.ONE, length);
        final float scale = moveSpeed / denominator;

        final float localX = inputX * scale;
        final float localY = inputY * scale;
        final float localZ = inputZ * scale;

        final float yawRadians = yawDegrees
                * BdsMovementMath.DEGREES_TO_RADIANS;
        final float sinYaw = BdsMovementMath.sinf(yawRadians);
        final float cosYaw = BdsMovementMath.cosf(yawRadians);

        float xDelta = localX * cosYaw;
        final float zSin = localZ * sinYaw;
        xDelta = xDelta - zSin;
        xDelta = xDelta + startVelocity.x;
        final float resultX = xDelta;

        final float resultY = localY + startVelocity.y;

        float zDelta = localZ * cosYaw;
        final float xSin = localX * sinYaw;
        zDelta = zDelta + xSin;
        zDelta = zDelta + startVelocity.z;
        final float resultZ = zDelta;

        return new Vec3(resultX, resultY, resultZ);
    }

    private static float sampleFrictionForDamping(final GhostPlayer player) {
        if (!player.entityContext.onGroundFlagComponent.isPresent()) {
            return AIR_DAMPING_FRICTION;
        }

        final float x = player.entityContext.stateVectorComponent.getPosition().x;
        final float y = player.entityContext.stateVectorComponent.getPosition().y - 0.1F;
        final float z = player.entityContext.stateVectorComponent.getPosition().z;
        final BlockVector3 blockPosition = new BlockVector3(
                floorFloat(x), floorFloat(y), floorFloat(z));
        final var blockState = player.entityContext.localConstBlockSourceFactoryComponent
                .create().getBlockState(blockPosition, 0);
        final float friction = blockState.getFriction();
        return friction;
    }

    private static void applyClimbDownClamp(final GhostPlayer player,
                                            final StateVectorComponent stateVector,
                                            final Vec3 velocity) {
        final boolean climbing = AutoClimbSystem.isClimbing(player, stateVector);
        final boolean powderSnow;
        if (climbing) {
            powderSnow = false;
        } else {
            powderSnow = isPowderSnowClimbSurface(player, stateVector);
            if (!powderSnow) {
                return;
            }
        }

        if (velocity.y < CLIMB_DOWN_LIMIT) {
            velocity.y = CLIMB_DOWN_LIMIT;
        }
        final boolean haltAtFeet = velocity.y < 0.0F
                && player.entityContext.actorDataFlagComponent.has(cn.nukkit.entity.Entity.DATA_FLAG_SNEAKING)
                && AutoClimbSystem.hasClimbHaltPropertyAtFeet(player, stateVector);
        if (haltAtFeet) {
            velocity.y = 0.0F;
        }
    }

    private static boolean isPowderSnowClimbSurface(
            final GhostPlayer player,
            final StateVectorComponent stateVector) {
        if (!player.entityContext.canStandOnSnowFlagComponent.isPresent()
                && !player.entityContext.hasLightweightFamilyFlagComponent.isPresent()) {
            return false;
        }

        final Vec3 position = stateVector.getPosition();
        final int x = floorFloat(position.x);
        final int y = floorFloat(player.entityContext.aabbShapeComponent.getAABB().minY);
        final int z = floorFloat(position.z);
        if (!player.entityContext.localConstBlockSourceFactoryComponent.create()
                .isChunkLoaded(x, z)) {
            return false;
        }
        return player.entityContext.localConstBlockSourceFactoryComponent.create()
                .getBlockState(x, y, z, 0).isPowderSnow();
    }

    private static int floorFloat(final float value) {
        final int truncated = (int) value;
        return value < truncated ? truncated - 1 : truncated;
    }
}
