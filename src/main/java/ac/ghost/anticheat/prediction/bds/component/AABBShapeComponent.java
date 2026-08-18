package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.data.EntityDimensions;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;


public final class AABBShapeComponent {
    private EntityDimensions dimensions =
            EntityDimensions.changing(0.6F, 1.8F).withEyeHeight(1.62F);
    private Box aabb = Box.EMPTY;

    public EntityDimensions getDimensions() { return dimensions; }
    public void setDimensions(final EntityDimensions value, final Vec3 position) {
        if (value == null) return;
        this.dimensions = value;
        updateAt(position);
    }
    public void updateAt(final Vec3 position) {
        if (position == null) return;
        this.aabb = this.dimensions.getBoxAt(position.x, position.y, position.z);
    }
    public void setAABB(final Box value) {
        this.aabb = value == null ? Box.EMPTY : value.clone();
    }
    public Box getAABB() { return this.aabb.clone(); }
    public boolean isPresent() { return this.aabb.isValid(); }
    public void clear() { this.aabb = Box.EMPTY; }
    public float getWidth() { return this.aabb.maxX - this.aabb.minX; }
    public float getHeight() { return this.aabb.maxY - this.aabb.minY; }
}
