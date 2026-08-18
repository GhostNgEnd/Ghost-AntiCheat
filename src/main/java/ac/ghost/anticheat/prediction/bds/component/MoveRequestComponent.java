package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.prediction.model.CollisionShapeEntry;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class MoveRequestComponent {
    private Vec3 movement = Vec3.ZERO.clone();
    private Vec3 ordinaryMovement = Vec3.ZERO.clone();
    private Vec3 resolvedMovement = Vec3.ZERO.clone();
    private Box originalAABB = Box.EMPTY;
    private Box resolvedAABB = Box.EMPTY;
    private Vec3 depenetrationMagnitude = new Vec3(1.0F, 1.0F, 1.0F);
    private boolean collisionResponse;
    private float overlapDepth;
    
    
    private List<CollisionShapeEntry> collisionShapeEntries = Collections.emptyList();

    public void begin(final Vec3 movement, final Box aabb) {
        final Vec3 submitted = movement == null ? Vec3.ZERO.clone() : movement.clone();
        final Box box = aabb == null ? Box.EMPTY : aabb.clone();
        this.movement = submitted.clone();
        this.ordinaryMovement = submitted.clone();
        this.resolvedMovement = submitted.clone();
        this.originalAABB = box;
        this.resolvedAABB = box;
        this.depenetrationMagnitude = new Vec3(1.0F, 1.0F, 1.0F);
        this.collisionResponse = false;
        this.overlapDepth = 0.0F;
        this.collisionShapeEntries = Collections.emptyList();
    }

    public Vec3 movement() {
        return this.movement.clone();
    }

    public void setMovement(final Vec3 movement) {
        this.movement = movement == null ? Vec3.ZERO.clone() : movement.clone();
    }

    public void multiplyMovement(final Vec3 multiplier) {
        if (multiplier == null) {
            return;
        }
        this.movement.x *= multiplier.x;
        this.movement.y *= multiplier.y;
        this.movement.z *= multiplier.z;
    }

    public Vec3 ordinaryMovement() {
        return this.ordinaryMovement.clone();
    }

    public void setOrdinaryResult(final Vec3 movement, final Box aabb) {
        this.ordinaryMovement = movement.clone();
        this.resolvedMovement = movement.clone();
        this.resolvedAABB = aabb.clone();
    }

    public Vec3 resolvedMovement() {
        return this.resolvedMovement.clone();
    }

    public Box resolvedAABB() {
        return this.resolvedAABB.clone();
    }

    public void setResolvedResult(final Vec3 movement, final Box aabb) {
        this.resolvedMovement = movement.clone();
        this.resolvedAABB = aabb.clone();
    }

    public Box originalAABB() {
        return this.originalAABB.clone();
    }

    public Vec3 depenetrationMagnitude() {
        return this.depenetrationMagnitude.clone();
    }

    public void setDepenetrationMagnitude(final Vec3 value) {
        this.depenetrationMagnitude = value == null ? Vec3.ZERO.clone() : value.clone();
    }

    public boolean collisionResponse() {
        return this.collisionResponse;
    }

    public void setCollisionResponse(final boolean value) {
        this.collisionResponse = value;
    }

    public float overlapDepth() {
        return this.overlapDepth;
    }

    public void setOverlapDepth(final float value) {
        this.overlapDepth = value;
    }

    public void addOverlapDepth(final float value) {
        this.overlapDepth += value;
    }


    public void setCollisionShapeEntries(final List<CollisionShapeEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            this.collisionShapeEntries = Collections.emptyList();
            return;
        }
        final ArrayList<CollisionShapeEntry> copy = new ArrayList<>(entries.size());
        for (final CollisionShapeEntry entry : entries) {
            if (entry != null && entry.shape().isValid()) {
                copy.add(new CollisionShapeEntry(entry.shape(), entry.block()));
            }
        }
        this.collisionShapeEntries = copy.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(copy);
    }

    public List<CollisionShapeEntry> collisionShapeEntries() {
        return this.collisionShapeEntries;
    }

    public void clear() {
        begin(Vec3.ZERO, Box.EMPTY);
    }
}
