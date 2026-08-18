package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.data.block.BlockLegacy;


public final class CurrentlyStandingOnBlockComponent {
    private BlockLegacy blockAtCollisionShape;
    private BlockLegacy blockAboveCollisionShape;

    public BlockLegacy blockAtCollisionShape() {
        return this.blockAtCollisionShape;
    }

    public BlockLegacy blockAboveCollisionShape() {
        return this.blockAboveCollisionShape;
    }

    public void set(final BlockLegacy blockAtCollisionShape,
                    final BlockLegacy blockAboveCollisionShape) {
        this.blockAtCollisionShape = blockAtCollisionShape;
        this.blockAboveCollisionShape = blockAboveCollisionShape;
    }
}
