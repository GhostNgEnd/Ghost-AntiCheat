package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.data.block.BlockLegacy;
import cn.nukkit.math.BlockVector3;


public final class BlockPosTrackerComponent {
    
    private boolean previousOnGround;
    
    private BlockVector3 previousPosition;
    
    private BlockLegacy currentBlock;
    
    private BlockVector3 currentPosition;
    
    private boolean shouldTriggerStandOn;

    public boolean previousOnGround() {
        return this.previousOnGround;
    }

    public BlockVector3 previousPosition() {
        return copy(this.previousPosition);
    }

    public BlockLegacy currentBlock() {
        return this.currentBlock;
    }

    public BlockVector3 currentPosition() {
        return copy(this.currentPosition);
    }

    public boolean shouldTriggerStandOn() {
        return this.shouldTriggerStandOn;
    }

    public void setCurrent(final BlockLegacy currentBlock,
                           final BlockVector3 currentPosition) {
        this.currentBlock = currentBlock;
        this.currentPosition = copy(currentPosition);
    }

    public void setShouldTriggerStandOn(
            final boolean shouldTriggerStandOn) {
        this.shouldTriggerStandOn = shouldTriggerStandOn;
    }

    
    public void commitCurrentAsPrevious(final boolean onGround) {
        this.previousOnGround = onGround;
        this.previousPosition = copy(this.currentPosition);
    }

    public void resetShouldTriggerStandOn() {
        this.shouldTriggerStandOn = false;
    }

    private static BlockVector3 copy(final BlockVector3 position) {
        return position == null
                ? null
                : new BlockVector3(
                        position.getX(), position.getY(), position.getZ());
    }
}
