package ac.ghost.anticheat.prediction.model;

import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.util.math.Box;







public final class CollisionShapeEntry {
    private final Box shape;
    private final BlockLegacy block;

    
    public CollisionShapeEntry(final Box shape, final BlockLegacy block) {
        this.shape = shape == null ? Box.EMPTY : shape.clone();
        this.block = block;
    }

    
    public static CollisionShapeEntry nonBlock(final Box shape) {
        return new CollisionShapeEntry(shape, null);
    }

    public Box shape() {
        return this.shape.clone();
    }

    public boolean blockCollision() {
        return this.block != null;
    }

    
    public BlockLegacy block() {
        return this.block;
    }
}
