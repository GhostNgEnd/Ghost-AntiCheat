package ac.ghost.anticheat.util.math;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.compensated.cache.entity.EntityCache;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.MathUtil;
import ac.ghost.anticheat.util.Pair;







public final class ReachUtil {
    private static final float POSITION_SAMPLE_STEP = 0.01F;

    
    private static final float HITBOX_EXPANSION = 0.1F;

    private ReachUtil() {
    }

    




    public static float calculateReach(final GhostPlayer player,
                                       final Pair<Vec3, Vec3> pair,
                                       final EntityCache entity) {
        if (entity == null || entity.getCurrent() == null) {
            return Float.NaN;
        }

        final Box currentBox = entity.getCurrent().calculateBoundingBox();
        final Box pastBox = entity.getPast() == null
                ? null
                : entity.getPast().calculateBoundingBox();

        float bestDistanceSquared = Float.MAX_VALUE;
        final float maxRangeSquared = MathUtil.square(Ghost.getConfig().toleranceReach());

        for (float delta = 0F; delta < 1F + 1.0E-3F; delta += POSITION_SAMPLE_STEP) {
            final float sampleDelta = Math.min(delta, 1F);
            final Vec3 eye = getEyePosition(player, pair, sampleDelta);

            bestDistanceSquared = Math.min(
                    bestDistanceSquared,
                    distanceSquaredToExpandedBox(eye, currentBox)
            );
            if (pastBox != null) {
                bestDistanceSquared = Math.min(
                        bestDistanceSquared,
                        distanceSquaredToExpandedBox(eye, pastBox)
                );
            }

            if (bestDistanceSquared <= maxRangeSquared) {
                break;
            }
        }

        return (float) Math.sqrt(bestDistanceSquared);
    }

    private static float distanceSquaredToExpandedBox(final Vec3 point, final Box box) {
        final float minX = box.minX - HITBOX_EXPANSION;
        final float minY = box.minY - HITBOX_EXPANSION;
        final float minZ = box.minZ - HITBOX_EXPANSION;
        final float maxX = box.maxX + HITBOX_EXPANSION;
        final float maxY = box.maxY + HITBOX_EXPANSION;
        final float maxZ = box.maxZ + HITBOX_EXPANSION;

        final float gapX = axisGap(point.x, minX, maxX);
        final float gapY = axisGap(point.y, minY, maxY);
        final float gapZ = axisGap(point.z, minZ, maxZ);
        return gapX * gapX + gapY * gapY + gapZ * gapZ;
    }

    private static Vec3 getEyePosition(final GhostPlayer player,
                                       final Pair<Vec3, Vec3> pair,
                                       final float delta) {
        return new Vec3(
                MathUtil.lerp(delta, pair.a().x, pair.b().x),
                MathUtil.lerp(delta, pair.a().y, pair.b().y) + player.entityContext.aabbShapeComponent.getDimensions().eyeHeight(),
                MathUtil.lerp(delta, pair.a().z, pair.b().z)
        );
    }

    private static float axisGap(final float value, final float min, final float max) {
        if (value < min) {
            return min - value;
        }
        if (value > max) {
            return value - max;
        }
        return 0F;
    }
}
