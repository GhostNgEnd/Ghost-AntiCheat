package ac.ghost.anticheat.prediction.bds.world.block;

import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.BlockVector3;


public final class BlockType {
    private BlockType() {
    }

    
    public static TallestCollisionShapeUpdate updateTallestCollisionShape(
            final BlockLegacy block,
            final GhostPlayer collisionShapeContext,
            final BlockVector3 blockPosition,
            final Box intersectTestBox,
            final Box currentTallestShape,
            final Vec3 matchingHeightTarget,
            final float currentDistanceSquared) {
        final Box candidate = block.getCollisionShape(
                collisionShapeContext, blockPosition);
        if (!candidate.isValid()) {
            return new TallestCollisionShapeUpdate(
                    candidate, false, false, Float.MAX_VALUE,
                    "INVALID_COLLISION_SHAPE");
        }
        if (!strictlyIntersects(candidate, intersectTestBox)) {
            return new TallestCollisionShapeUpdate(
                    candidate, false, false, Float.MAX_VALUE,
                    "OUTSIDE_INTERSECT_TEST_BOX");
        }

        final float candidateDistanceSquared = squaredDistance(
                matchingHeightTarget,
                (candidate.minX + candidate.maxX) * 0.5F,
                (candidate.minY + candidate.maxY) * 0.5F,
                (candidate.minZ + candidate.maxZ) * 0.5F);
        if (currentTallestShape.isValid()
                && currentTallestShape.maxY > candidate.maxY) {
            return new TallestCollisionShapeUpdate(
                    candidate, true, false, candidateDistanceSquared,
                    "LOWER_TOP_REJECT");
        }
        if (!currentTallestShape.isValid()
                || candidate.maxY > currentTallestShape.maxY) {
            return new TallestCollisionShapeUpdate(
                    candidate, true, true, candidateDistanceSquared,
                    currentTallestShape.isValid()
                            ? "HIGHER_TOP_REPLACE" : "INITIAL");
        }
        if (candidate.maxY == currentTallestShape.maxY
                && candidateDistanceSquared < currentDistanceSquared) {
            return new TallestCollisionShapeUpdate(
                    candidate, true, true, candidateDistanceSquared,
                    "MATCHING_TOP_NEARER_SHAPE_REPLACE");
        }
        return new TallestCollisionShapeUpdate(
                candidate, true, false, candidateDistanceSquared,
                "MATCHING_TOP_NOT_NEARER_REJECT");
    }

    private static boolean strictlyIntersects(final Box shape,
                                              final Box query) {
        return query != null
                && query.minX < shape.maxX
                && shape.minX < query.maxX
                && query.minY < shape.maxY
                && shape.minY < query.maxY
                && query.minZ < shape.maxZ
                && shape.minZ < query.maxZ;
    }

    private static float squaredDistance(
            final Vec3 from, final float x, final float y, final float z) {
        final float deltaX = from.x - x;
        final float deltaY = from.y - y;
        final float deltaZ = from.z - z;
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    public record TallestCollisionShapeUpdate(
            Box shape,
            boolean eligible,
            boolean selected,
            float matchingHeightDistanceSquared,
            String decision) {
    }
}
