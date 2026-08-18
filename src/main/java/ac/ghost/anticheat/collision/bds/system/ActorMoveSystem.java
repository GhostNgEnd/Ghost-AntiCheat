package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MoveRequestComponent;
import ac.ghost.anticheat.prediction.bds.component.RewindCollisionShapesComponent;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.BlockFace.Axis;

import java.util.Collections;
import java.util.List;


public final class ActorMoveSystem {
    static final float CONTACT_EPSILON = 1.0E-6F;
    static final float FLOAT_EPSILON = 1.1920929E-7F;
    static final float MAX_REASONABLE_ABS = 33554432.0F;

    private ActorMoveSystem() {
    }

    public static void run(final GhostPlayer player) {
        final MoveRequestComponent request = player.entityContext.moveRequestComponent;
        final RewindCollisionShapesComponent cache = player.entityContext.rewindCollisionShapesComponent;
        final List<Box> collisions = cache == null
                ? Collections.emptyList() : cache.collisionShapes();
        final Vec3 movement = request.movement();
        final SolveResult result = solveSegments(
                request.originalAABB(),
                collisions,
                request.depenetrationMagnitude(),
                new Vec3(0.0F, movement.y, 0.0F),
                new Vec3(movement.x, 0.0F, 0.0F),
                new Vec3(0.0F, 0.0F, movement.z));

        request.setOrdinaryResult(result.movement, result.aabb);
        request.setOverlapDepth(result.overlapDepth);
        request.setCollisionResponse(result.overlapDepth >= CONTACT_EPSILON);
    }

    static final class SolveResult {
        final Vec3 movement;
        final Box aabb;
        final float overlapDepth;

        SolveResult(final Vec3 movement, final Box aabb, final float overlapDepth) {
            this.movement = movement;
            this.aabb = aabb;
            this.overlapDepth = overlapDepth;
        }
    }

    private static final class ContactResult {
        final Vec3 passive;
        final Vec3 depenetrated;
        final Axis axis;
        final float depth;

        ContactResult(final Vec3 passive,
                      final Vec3 depenetrated,
                      final Axis axis,
                      final float depth) {
            this.passive = passive;
            this.depenetrated = depenetrated;
            this.axis = axis;
            this.depth = depth;
        }
    }

    
    private static final class AxisContact {
        
        final float distance;
        
        final float direction;
        final boolean overlapping;

        AxisContact(final float distance,
                    final float direction,
                    final boolean overlapping) {
            this.distance = distance;
            this.direction = direction;
            this.overlapping = overlapping;
        }
    }

    static SolveResult solveSegments(Box aabb,
                                     final List<Box> collisions,
                                     final Vec3 depenetrationMagnitude,
                                     final Vec3... segments) {
        float totalX = 0.0F;
        float totalY = 0.0F;
        float totalZ = 0.0F;
        float overlapDepth = 0.0F;

        for (final Vec3 input : segments) {
            if (input == null) {
                continue;
            }
            Vec3 stage = input.clone();
            for (int index = collisions.size() - 1; index >= 0; index--) {
                final ContactResult contact = solveContact(aabb, collisions.get(index), stage);
                if (contact == null) {
                    continue;
                }
                overlapDepth += contact.depth;
                if (component(depenetrationMagnitude, contact.axis) >= contact.depth) {
                    stage = contact.depenetrated;
                } else {
                    stage = contact.passive;
                }
            }
            totalX += stage.x;
            totalY += stage.y;
            totalZ += stage.z;
            if (stage.x != 0.0F || stage.y != 0.0F || stage.z != 0.0F) {
                aabb = aabb.offset(stage);
            }
        }
        return new SolveResult(new Vec3(totalX, totalY, totalZ), aabb, overlapDepth);
    }

    







    private static ContactResult solveContact(final Box moving,
                                              final Box collider,
                                              final Vec3 stage) {
        if (!validCollider(collider)) {
            return null;
        }

        final AxisContact x = classifyAxis(
                moving.minX, moving.maxX, collider.minX, collider.maxX);
        final AxisContact y = classifyAxis(
                moving.minY, moving.maxY, collider.minY, collider.maxY);
        final AxisContact z = classifyAxis(
                moving.minZ, moving.maxZ, collider.minZ, collider.maxZ);

        final int separatedAxes = (x.overlapping ? 0 : 1)
                + (y.overlapping ? 0 : 1)
                + (z.overlapping ? 0 : 1);
        if (separatedAxes > 1) {
            return null;
        }
        if (separatedAxes == 1) {
            if (!x.overlapping) {
                return sweepSeparatedAxis(stage, Axis.X, x);
            }
            if (!y.overlapping) {
                return sweepSeparatedAxis(stage, Axis.Y, y);
            }
            return sweepSeparatedAxis(stage, Axis.Z, z);
        }

        Axis axis = Axis.X;
        AxisContact selected = x;
        if (y.distance < selected.distance) {
            axis = Axis.Y;
            selected = y;
        }
        if (z.distance < selected.distance) {
            axis = Axis.Z;
            selected = z;
        }

        final Vec3 passive = stage.clone();
        final Vec3 depenetrated = stage.clone();
        setAtLeastSeparation(
                depenetrated,
                axis,
                selected.distance * selected.direction);
        return new ContactResult(
                passive,
                depenetrated,
                axis,
                selected.distance);
    }

    private static AxisContact classifyAxis(final float movingMin,
                                            final float movingMax,
                                            final float colliderMin,
                                            final float colliderMax) {
        final float minimumSideDistance = normalizeContactDistance(
                movingMax - colliderMin);
        final float minimumSideDepth = Math.max(minimumSideDistance, 0.0F);
        if (minimumSideDepth == 0.0F) {
            return new AxisContact(minimumSideDistance, -1.0F, false);
        }

        final float maximumSideDistance = normalizeContactDistance(
                colliderMax - movingMin);
        final float maximumSideDepth = Math.max(maximumSideDistance, 0.0F);
        if (maximumSideDepth == 0.0F) {
            return new AxisContact(maximumSideDistance, 1.0F, false);
        }

        final boolean minimumSideIsCloser = minimumSideDepth < maximumSideDepth;
        return new AxisContact(
                Math.min(minimumSideDepth, maximumSideDepth),
                minimumSideIsCloser ? -1.0F : 1.0F,
                true);
    }

    private static ContactResult sweepSeparatedAxis(final Vec3 stage,
                                                    final Axis axis,
                                                    final AxisContact contact) {
        final float requested = component(stage, axis);
        final float crossedDistance = contact.distance
                - requested * contact.direction;
        if (!(crossedDistance > 0.0F)) {
            return null;
        }

        final float clipped = contact.distance * contact.direction;
        final Vec3 result = stage.clone();
        setComponent(result, axis, clipped);
        return new ContactResult(result, result.clone(), axis, 0.0F);
    }

    private static float normalizeContactDistance(final float distance) {
        return Math.abs(distance) < CONTACT_EPSILON ? 0.0F : distance;
    }

    private static boolean validCollider(final Box collider) {
        return collider != null
                && collider.minX < collider.maxX
                && collider.minY < collider.maxY
                && collider.minZ < collider.maxZ;
    }

    private static void setAtLeastSeparation(final Vec3 vector,
                                             final Axis axis,
                                             final float resolution) {
        final float current = component(vector, axis);
        if (resolution < 0.0F) {
            setComponent(vector, axis, Math.min(current, resolution));
        } else if (resolution > 0.0F) {
            setComponent(vector, axis, Math.max(current, resolution));
        }
    }

    static boolean hasCollision(final Box query, final List<Box> collisions) {
        for (int index = collisions.size() - 1; index >= 0; index--) {
            if (query.intersects(collisions.get(index))) {
                return true;
            }
        }
        return false;
    }

    static boolean isReasonable(final Vec3 value) {
        return value != null
                && reasonable(value.x)
                && reasonable(value.y)
                && reasonable(value.z);
    }

    static boolean isReasonable(final Box value) {
        return value != null
                && reasonable(value.minX)
                && reasonable(value.minY)
                && reasonable(value.minZ)
                && reasonable(value.maxX)
                && reasonable(value.maxY)
                && reasonable(value.maxZ)
                && value.minX < value.maxX
                && value.minY < value.maxY
                && value.minZ < value.maxZ;
    }

    private static boolean reasonable(final float value) {
        return Float.isFinite(value) && Math.abs(value) <= MAX_REASONABLE_ABS;
    }

    private static float component(final Vec3 vector, final Axis axis) {
        return switch (axis) {
            case X -> vector.x;
            case Y -> vector.y;
            case Z -> vector.z;
        };
    }

    private static void setComponent(final Vec3 vector, final Axis axis, final float value) {
        switch (axis) {
            case X -> vector.x = value;
            case Y -> vector.y = value;
            case Z -> vector.z = value;
        }
    }

}
