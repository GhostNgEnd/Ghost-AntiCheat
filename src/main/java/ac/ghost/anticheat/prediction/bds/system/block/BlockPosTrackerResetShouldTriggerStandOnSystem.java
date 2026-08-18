package ac.ghost.anticheat.prediction.bds.system.block;

import ac.ghost.anticheat.player.GhostPlayer;


public final class BlockPosTrackerResetShouldTriggerStandOnSystem {
    private BlockPosTrackerResetShouldTriggerStandOnSystem() {
    }

    public static void tick(final GhostPlayer player) {
        player.entityContext.blockPosTrackerComponent.resetShouldTriggerStandOn();
    }
}
