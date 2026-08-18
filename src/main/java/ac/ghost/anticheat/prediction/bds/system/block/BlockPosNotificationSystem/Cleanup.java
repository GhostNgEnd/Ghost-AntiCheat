package ac.ghost.anticheat.prediction.bds.system.block.BlockPosNotificationSystem;

import ac.ghost.anticheat.player.GhostPlayer;


public final class Cleanup {
    private Cleanup() {
    }

    public static void tick(final GhostPlayer entity) {
        entity.entityContext.standOnSpeedAlteringBlockFlagComponent.clear();
        entity.entityContext.standOnOtherBlockFlagComponent.clear();
    }
}
