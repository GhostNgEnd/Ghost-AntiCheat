package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;

import java.util.ArrayList;
import java.util.List;


public final class MoveTowardsClosestSpaceSystemFromActor {
    private static final float QUERY_EXPANSION = 1.0F;
    private static final float PUSH_MAGNITUDE = 0.1F;
    private static final float DIRECTION_EPSILON = ActorMoveSystem.FLOAT_EPSILON;
    private static final float MIN_LENGTH = 1.0E-4F;

    private MoveTowardsClosestSpaceSystemFromActor() {
    }

    public static void run(final GhostPlayer player) {
        if (!player.entityContext.moveTowardsClosestSpaceFlagComponent.isPresent()) {
            return;
        }

                final Box actor = player.entityContext.aabbShapeComponent.getAABB();
        final Box query = new Box(
                actor.minX - QUERY_EXPANSION,
                actor.minY,
                actor.minZ - QUERY_EXPANSION,
                actor.maxX + QUERY_EXPANSION,
                actor.maxY,
                actor.maxZ + QUERY_EXPANSION);

        final List<Box> queried = player.entityContext.localConstBlockSourceFactoryComponent
                .create()
                .collectColliders(query);
        final List<Box> collisions = new ArrayList<>();
        for (final Box collision : queried) {
            if (collision.intersects(actor)) {
                collisions.add(collision);
            }
        }
        if (collisions.isEmpty()) {
            return;
        }

        float averageX = 0.0F;
        float averageZ = 0.0F;
        for (final Box collision : collisions) {
            averageX += (collision.minX + collision.maxX) * 0.5F;
            averageZ += (collision.minZ + collision.maxZ) * 0.5F;
        }
        averageX /= collisions.size();
        averageZ /= collisions.size();

        final float halfWidth = (actor.maxX - actor.minX) * 0.5F;
        final float halfDepth = (actor.maxZ - actor.minZ) * 0.5F;
        final float actorCenterX = (actor.minX + actor.maxX) * 0.5F;
        final float actorCenterZ = (actor.minZ + actor.maxZ) * 0.5F;

        float directionX = actorCenterX - averageX;
        float directionZ = actorCenterZ - averageZ;
        if (Math.abs(directionX) < DIRECTION_EPSILON
                && Math.abs(directionZ) < DIRECTION_EPSILON) {
            directionX = 1.0F;
            directionZ = 1.0F;
        }

        directionX = selectOpenDirectionX(
                directionX,
                averageX,
                averageZ,
                halfWidth,
                halfDepth,
                actor.minY,
                actor.maxY,
                collisions);
        directionZ = selectOpenDirectionZ(
                directionZ,
                averageX,
                averageZ,
                halfWidth,
                halfDepth,
                actor.minY,
                actor.maxY,
                collisions);

        if (Math.abs(directionX) < DIRECTION_EPSILON
                && Math.abs(directionZ) < DIRECTION_EPSILON) {
            return;
        }

        final float length = (float) Math.sqrt(
                directionX * directionX + directionZ * directionZ);
        final float pushX = directionX / length * PUSH_MAGNITUDE;
        final float pushZ = directionZ / length * PUSH_MAGNITUDE;

        final Vec3 delta = player.entityContext.stateVectorComponent.getDelta().clone();
        delta.x = applyHorizontalPush(delta.x, pushX, length);
        delta.z = applyHorizontalPush(delta.z, pushZ, length);
        player.entityContext.stateVectorComponent.setDelta(delta);
        player.entityContext.stateVectorComponent.setDelta(delta.clone());
    }

    private static float selectOpenDirectionX(
            final float direction,
            final float centerX,
            final float centerZ,
            final float halfWidth,
            final float halfDepth,
            final float minY,
            final float maxY,
            final List<Box> collisions) {
        final float sign = Math.signum(direction);
        if (sign == 0.0F) {
            return 0.0F;
        }

        final float baseMinX = centerX - halfWidth;
        final float baseMaxX = centerX + halfWidth;
        final float baseMinZ = centerZ - halfDepth;
        final float baseMaxZ = centerZ + halfDepth;
        final Box forward = new Box(
                baseMinX + (sign < 0.0F ? sign : 0.0F),
                minY,
                baseMinZ,
                baseMaxX + (sign > 0.0F ? sign : 0.0F),
                maxY,
                baseMaxZ);
        if (!intersectsAny(forward, collisions)) {
            return direction;
        }

        final Box reverse = new Box(
                baseMinX + (sign > 0.0F ? -sign : 0.0F),
                minY,
                baseMinZ,
                baseMaxX + (sign < 0.0F ? -sign : 0.0F),
                maxY,
                baseMaxZ);
        return intersectsAny(reverse, collisions) ? 0.0F : -direction;
    }

    private static float selectOpenDirectionZ(
            final float direction,
            final float centerX,
            final float centerZ,
            final float halfWidth,
            final float halfDepth,
            final float minY,
            final float maxY,
            final List<Box> collisions) {
        final float sign = Math.signum(direction);
        if (sign == 0.0F) {
            return 0.0F;
        }

        final float baseMinX = centerX - halfWidth;
        final float baseMaxX = centerX + halfWidth;
        final float baseMinZ = centerZ - halfDepth;
        final float baseMaxZ = centerZ + halfDepth;
        final Box forward = new Box(
                baseMinX,
                minY,
                baseMinZ + (sign < 0.0F ? sign : 0.0F),
                baseMaxX,
                maxY,
                baseMaxZ + (sign > 0.0F ? sign : 0.0F));
        if (!intersectsAny(forward, collisions)) {
            return direction;
        }

        final Box reverse = new Box(
                baseMinX,
                minY,
                baseMinZ + (sign > 0.0F ? -sign : 0.0F),
                baseMaxX,
                maxY,
                baseMaxZ + (sign < 0.0F ? -sign : 0.0F));
        return intersectsAny(reverse, collisions) ? 0.0F : -direction;
    }

    private static boolean intersectsAny(final Box test, final List<Box> collisions) {
        for (final Box collision : collisions) {
            if (collision.intersects(test)) {
                return true;
            }
        }
        return false;
    }

    private static float applyHorizontalPush(
            final float current,
            final float push,
            final float directionLength) {
        final float absolutePush = directionLength < MIN_LENGTH
                ? 0.0F
                : Math.abs(push);
        if (absolutePush <= Math.abs(current)) {
            return current;
        }
        return Math.max(-absolutePush, Math.min(absolutePush, current + push));
    }
}
