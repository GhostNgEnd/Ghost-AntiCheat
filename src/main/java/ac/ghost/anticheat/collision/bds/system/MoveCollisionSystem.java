package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.compensated.cache.entity.EntityCache;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.AABBShapeComponent;
import ac.ghost.anticheat.prediction.model.CollisionShapeEntry;
import ac.ghost.anticheat.prediction.bds.component.MoveRequestComponent;
import ac.ghost.anticheat.prediction.bds.component.RewindCollisionShapesComponent;
import ac.ghost.anticheat.prediction.bds.world.LocalConstBlockSource;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class MoveCollisionSystem {
    private static final float MAX_MOVE_LENGTH_SQUARED = 256.0F;
    private static final float MAX_MOVE_LENGTH = 16.0F;
    private static final float EXTRA_DOWN = 0.200000003F;
    private static final float EXTRA_UP = 0.08F;
    private static final float SNEAK_SUPPORT_DISTANCE_SCALE = 1.01F;
    private static final float ENTITY_QUERY_PADDING = 0.25F;

    private static final float AXIS_BOUNDARY_EPSILON = 1.0E-5F;
    private static final float MERGE_VOLUME_EPSILON = 0.001F;
    private static final int MAX_NATIVE_AXIS_BOUNDARIES = 27;

    private MoveCollisionSystem() {
    }

    public static void run(final GhostPlayer player) {
        final MoveRequestComponent request = player.entityContext.moveRequestComponent;
        request.setMovement(clamp(request.movement()));
        if (player.entityContext.rewindCollisionShapesComponent != null) {
            request.setCollisionShapeEntries(
                    player.entityContext.rewindCollisionShapesComponent
                            .collisionShapeEntries());
            return;
        }

        final Box original = request.originalAABB();
        final Box query = buildCollisionQuery(
                original,
                request.movement(),
                player.entityContext.maxAutoStepComponent.value());
        final LocalConstBlockSource blockSource =
                player.entityContext.localConstBlockSourceFactoryComponent.create();
        final ArrayList<CollisionShapeEntry> shapes = new ArrayList<>(
                blockSource.collectColliderEntries(query));

        if (player.entityContext.collidableMobNearFlagComponent.isPresent()) {
            for (final Box shape : collectEntityCollisionShapes(
                    player, query.expand(ENTITY_QUERY_PADDING), original.minY)) {
                shapes.add(CollisionShapeEntry.nonBlock(shape));
            }
        }

        final List<CollisionShapeEntry> optimized = optimize(shapes);
        request.setCollisionShapeEntries(optimized);
        final RewindCollisionShapesComponent collisionShapes =
                new RewindCollisionShapesComponent(
                        player.entityContext.serverPlayerMovementComponent.getCurrentInputTick(),
                        player.entityContext.stateVectorComponent.getPosition(),
                        query,
                        optimized);
        player.entityContext.rewindCollisionShapesComponent = collisionShapes;
    }

    private static Vec3 clamp(final Vec3 movement) {
        final float lengthSquared = movement.x * movement.x
                + movement.y * movement.y
                + movement.z * movement.z;
        if (!(lengthSquared > MAX_MOVE_LENGTH_SQUARED)) {
            return movement.clone();
        }
        final float length = (float) Math.sqrt(lengthSquared);
        if (!Float.isFinite(length) || !(length > 0.0F)) {
            return Vec3.ZERO.clone();
        }
        final float scale = MAX_MOVE_LENGTH / length;
        return new Vec3(
                movement.x * scale,
                movement.y * scale,
                movement.z * scale);
    }

    private static Box buildCollisionQuery(final Box current,
                                           final Vec3 delta,
                                           final float maxAutoStep) {
        final float minX = delta.x < 0.0F
                ? current.minX + delta.x : current.minX;
        final float maxX = delta.x > 0.0F
                ? current.maxX + delta.x : current.maxX;
        final float minZ = delta.z < 0.0F
                ? current.minZ + delta.z : current.minZ;
        final float maxZ = delta.z > 0.0F
                ? current.maxZ + delta.z : current.maxZ;

        final float raisedDestinationY = maxAutoStep + delta.y;
        final float minYOffset = Math.min(
                Math.min(0.0F, delta.y),
                Math.min(maxAutoStep, raisedDestinationY));
        final float maxYOffset = Math.max(
                Math.max(0.0F, delta.y),
                Math.max(maxAutoStep, raisedDestinationY));
        final float stepAwareMinY = current.minY + minYOffset;
        final float stepAwareMaxY = current.maxY + maxYOffset;
        final float sneakSupportMinY = current.minY
                - maxAutoStep * SNEAK_SUPPORT_DISTANCE_SCALE;
        final float minY = Math.min(
                Math.min(stepAwareMinY, sneakSupportMinY),
                current.minY - EXTRA_DOWN - Math.abs(delta.y));
        final float maxY = Math.max(stepAwareMaxY, current.maxY + EXTRA_UP);
        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static List<Box> collectEntityCollisionShapes(final GhostPlayer player,
                                                          final Box query,
                                                          final float fallingBlockHeightLimit) {
        final ArrayList<Box> result = new ArrayList<>();
        for (final EntityCache entity : player.entityRegistry.entities().values()) {
            if (entity == null
                    || entity.dimension() != player.entityContext.blockSource.getDimension()
                    || entity.currentState() == null
                    || !entity.collidableMobFlagComponent().isPresent()) {
                continue;
            }
            entity.refreshAABBShapeComponent();
            final AABBShapeComponent shape = entity.aabbShapeComponent();
            if (!shape.isPresent()) {
                continue;
            }
            final Box aabb = shape.getAABB();
            if (!query.intersects(aabb)) {
                continue;
            }
            if (entity.fallingBlockFlagComponent().isPresent()
                    && aabb.maxY > fallingBlockHeightLimit) {
                continue;
            }
            result.add(aabb);
        }
        return result;
    }

    private static List<CollisionShapeEntry> optimize(
            final List<CollisionShapeEntry> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        final AxisBoundaries x = new AxisBoundaries();
        final AxisBoundaries y = new AxisBoundaries();
        final AxisBoundaries z = new AxisBoundaries();
        final ArrayList<CollisionShapeEntry> normalized =
                new ArrayList<>(source.size());
        for (final CollisionShapeEntry entry : source) {
            if (entry == null) {
                continue;
            }
            final Box box = entry.shape();
            if (!valid(box)) {
                continue;
            }
            final Box canonical = new Box(
                    x.canonicalize(box.minX),
                    y.canonicalize(box.minY),
                    z.canonicalize(box.minZ),
                    x.canonicalize(box.maxX),
                    y.canonicalize(box.maxY),
                    z.canonicalize(box.maxZ));
            if (valid(canonical) && !containsExact(normalized, canonical, entry.block())) {
                normalized.add(new CollisionShapeEntry(
                        canonical, entry.block()));
            }
        }

        boolean changed;
        do {
            changed = false;
            outer:
            for (int first = 0; first < normalized.size(); first++) {
                for (int second = first + 1;
                     second < normalized.size(); second++) {
                    final CollisionShapeEntry leftEntry = normalized.get(first);
                    final CollisionShapeEntry rightEntry = normalized.get(second);
                    if (leftEntry.blockCollision() || rightEntry.blockCollision()) {
                        continue;
                    }
                    final Box left = leftEntry.shape();
                    final Box right = rightEntry.shape();
                    final Box union = left.union(right);
                    if (Math.abs(volume(union) - volume(left) - volume(right))
                            > MERGE_VOLUME_EPSILON) {
                        continue;
                    }
                    normalized.set(first, CollisionShapeEntry.nonBlock(union));
                    normalized.remove(second);
                    changed = true;
                    break outer;
                }
            }
        } while (changed);
        return normalized.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(normalized);
    }

    private static boolean valid(final Box box) {
        return box != null
                && Float.isFinite(box.minX)
                && Float.isFinite(box.minY)
                && Float.isFinite(box.minZ)
                && Float.isFinite(box.maxX)
                && Float.isFinite(box.maxY)
                && Float.isFinite(box.maxZ)
                && box.minX < box.maxX
                && box.minY < box.maxY
                && box.minZ < box.maxZ;
    }

    private static float volume(final Box box) {
        return (box.maxX - box.minX)
                * (box.maxY - box.minY)
                * (box.maxZ - box.minZ);
    }

    private static boolean containsExact(
            final List<CollisionShapeEntry> entries,
            final Box candidate,
            final ac.ghost.anticheat.data.block.BlockLegacy owner) {
        for (final CollisionShapeEntry entry : entries) {
            if (entry.block() != owner) {
                continue;
            }
            final Box box = entry.shape();
            if (Float.floatToRawIntBits(box.minX)
                    == Float.floatToRawIntBits(candidate.minX)
                    && Float.floatToRawIntBits(box.minY)
                    == Float.floatToRawIntBits(candidate.minY)
                    && Float.floatToRawIntBits(box.minZ)
                    == Float.floatToRawIntBits(candidate.minZ)
                    && Float.floatToRawIntBits(box.maxX)
                    == Float.floatToRawIntBits(candidate.maxX)
                    && Float.floatToRawIntBits(box.maxY)
                    == Float.floatToRawIntBits(candidate.maxY)
                    && Float.floatToRawIntBits(box.maxZ)
                    == Float.floatToRawIntBits(candidate.maxZ)) {
                return true;
            }
        }
        return false;
    }

    private static final class AxisBoundaries {
        private final float[] values = new float[MAX_NATIVE_AXIS_BOUNDARIES];
        private int size;

        float canonicalize(final float value) {
            for (int index = 0; index < this.size; index++) {
                final float known = this.values[index];
                final float difference = Math.abs(value - known);
                final float relative = ActorMoveSystem.FLOAT_EPSILON
                        * Math.max(1.0F, Math.max(Math.abs(value), Math.abs(known)));
                if (difference <= AXIS_BOUNDARY_EPSILON || difference <= relative) {
                    return known;
                }
            }
            if (this.size < this.values.length) {
                this.values[this.size++] = value;
            }
            return value;
        }
    }
}
