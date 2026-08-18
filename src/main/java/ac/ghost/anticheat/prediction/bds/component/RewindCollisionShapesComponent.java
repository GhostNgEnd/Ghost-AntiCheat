package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.prediction.model.CollisionShapeEntry;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class RewindCollisionShapesComponent {
    private final long sourceTick;
    private final Vec3 origin;
    private final Box collisionQuery;
    private final List<CollisionShapeEntry> collisionShapeEntries;
    private final List<Box> collisionShapes;

    public RewindCollisionShapesComponent(
            final long sourceTick,
            final Vec3 origin,
            final Box collisionQuery,
            final List<CollisionShapeEntry> collisionShapeEntries) {
        this.sourceTick = sourceTick;
        this.origin = origin == null ? Vec3.ZERO.clone() : origin.clone();
        this.collisionQuery = collisionQuery == null
                ? Box.EMPTY : collisionQuery.clone();
        this.collisionShapeEntries = immutableEntries(collisionShapeEntries);
        this.collisionShapes = immutableBoxes(this.collisionShapeEntries);
    }

    public long sourceTick() {
        return this.sourceTick;
    }

    public Vec3 origin() {
        return this.origin.clone();
    }

    public Box collisionQuery() {
        return this.collisionQuery.clone();
    }

    public List<Box> collisionShapes() {
        return this.collisionShapes;
    }

    public List<CollisionShapeEntry> collisionShapeEntries() {
        return this.collisionShapeEntries;
    }

    private static List<CollisionShapeEntry> immutableEntries(
            final List<CollisionShapeEntry> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        final ArrayList<CollisionShapeEntry> copy =
                new ArrayList<>(source.size());
        for (final CollisionShapeEntry entry : source) {
            if (entry != null && entry.shape().isValid()) {
                copy.add(new CollisionShapeEntry(
                        entry.shape(), entry.block()));
            }
        }
        return Collections.unmodifiableList(copy);
    }

    private static List<Box> immutableBoxes(
            final List<CollisionShapeEntry> source) {
        if (source.isEmpty()) {
            return Collections.emptyList();
        }
        final ArrayList<Box> copy = new ArrayList<>(source.size());
        for (final CollisionShapeEntry entry : source) {
            copy.add(entry.shape());
        }
        return Collections.unmodifiableList(copy);
    }
}
