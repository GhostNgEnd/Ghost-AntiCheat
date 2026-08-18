package ac.ghost.anticheat.prediction.bds.system.block.BlockPosNotificationSystem;

import ac.ghost.anticheat.player.GhostPlayer;


public final class GenericOnStandOn {
    private GenericOnStandOn() {
    }

    public static void tick(final GhostPlayer entity) {
        if (!entity.entityContext.standOnOtherBlockFlagComponent.isPresent()) {
            return;
        }

        
        
    }
}
