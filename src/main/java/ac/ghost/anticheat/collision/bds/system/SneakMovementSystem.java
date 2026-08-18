package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MoveRequestComponent;
import ac.ghost.anticheat.prediction.bds.component.RewindCollisionShapesComponent;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;

import java.util.Collections;
import java.util.List;


public final class SneakMovementSystem {
    private static final int ACTOR_DATA_FLAG_BIT_6 = 6;
    private static final float HORIZONTAL_INSET = 0.025F;
    private static final float SUPPORT_DISTANCE_SCALE = 1.01F;
    private static final float BACKOFF_INCREMENT = 0.05F;

    private SneakMovementSystem() {
    }

    public static void run(final GhostPlayer player) {
        final MoveRequestComponent request = player.entityContext.moveRequestComponent;
        if (!player.entityContext.onGroundFlagComponent.isPresent()) {
            return;
        }

        
        
        if (!player.entityContext.moveInputComponent.isSneaking()) {
            return;
        }

        
        
        if (player.entityContext.actorDataFlagComponent.has(ACTOR_DATA_FLAG_BIT_6)) {
            return;
        }

        final RewindCollisionShapesComponent cache = player.entityContext.rewindCollisionShapesComponent;
        final List<Box> collisions = cache == null
                ? Collections.emptyList() : cache.collisionShapes();
        final Vec3 submitted = request.movement();
        final float supportDistance = player.entityContext.maxAutoStepComponent.value()
                * SUPPORT_DISTANCE_SCALE;
        float x = submitted.x;
        float z = submitted.z;
        final float xStep = Math.copySign(BACKOFF_INCREMENT, x);
        final float zStep = Math.copySign(BACKOFF_INCREMENT, z);

        while (Math.abs(x) > ActorMoveSystem.FLOAT_EPSILON
                && canFallAtLeast(request.originalAABB(), collisions,
                x, 0.0F, supportDistance)) {
            x = reduceTowardZero(x, xStep);
        }
        while (Math.abs(z) > ActorMoveSystem.FLOAT_EPSILON
                && canFallAtLeast(request.originalAABB(), collisions,
                0.0F, z, supportDistance)) {
            z = reduceTowardZero(z, zStep);
        }
        while (Math.abs(x) > ActorMoveSystem.FLOAT_EPSILON
                && Math.abs(z) > ActorMoveSystem.FLOAT_EPSILON
                && canFallAtLeast(request.originalAABB(), collisions,
                x, z, supportDistance)) {
            x = reduceTowardZero(x, xStep);
            z = reduceTowardZero(z, zStep);
        }

        final boolean zeroX = Math.abs(x) <= ActorMoveSystem.FLOAT_EPSILON;
        final boolean zeroZ = Math.abs(z) <= ActorMoveSystem.FLOAT_EPSILON;
        if (zeroX) {
            x = 0.0F;
        }
        if (zeroZ) {
            z = 0.0F;
        }
        if (zeroX || zeroZ) {
            final Vec3 delta = player.entityContext.stateVectorComponent.getDelta();
            player.entityContext.stateVectorComponent.setDelta(new Vec3(
                    zeroX ? 0.0F : delta.x,
                    delta.y,
                    zeroZ ? 0.0F : delta.z));
            player.entityContext.stateVectorComponent.setDelta(player.entityContext.stateVectorComponent.getDelta().clone());
        }

        request.setMovement(new Vec3(x, submitted.y, z));
    }

    private static float reduceTowardZero(final float value, final float signedStep) {
        if (Math.abs(value) <= BACKOFF_INCREMENT) {
            return 0.0F;
        }
        return value - signedStep;
    }

    private static boolean canFallAtLeast(final Box aabb,
                                          final List<Box> collisions,
                                          final float offsetX,
                                          final float offsetZ,
                                          final float distance) {
        return !ActorMoveSystem.hasCollision(
                supportAABB(aabb, offsetX, offsetZ, distance), collisions);
    }

    private static Box supportAABB(final Box aabb,
                                   final float offsetX,
                                   final float offsetZ,
                                   final float distance) {
        float minX = aabb.minX + HORIZONTAL_INSET;
        float maxX = aabb.maxX - HORIZONTAL_INSET;
        float minZ = aabb.minZ + HORIZONTAL_INSET;
        float maxZ = aabb.maxZ - HORIZONTAL_INSET;
        if (minX > maxX) {
            minX = maxX = (aabb.minX + aabb.maxX) * 0.5F;
        }
        if (minZ > maxZ) {
            minZ = maxZ = (aabb.minZ + aabb.maxZ) * 0.5F;
        }
        return new Box(
                minX + offsetX,
                aabb.minY - distance,
                minZ + offsetZ,
                maxX + offsetX,
                aabb.maxY - distance,
                maxZ + offsetZ);
    }
}
