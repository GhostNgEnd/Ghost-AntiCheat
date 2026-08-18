package ac.ghost.anticheat.prediction.bds.system.block.BlockPosNotificationSystem;

import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.entity.Entity;


public final class Filter {
    private Filter() {
    }

    public static void tick(final GhostPlayer entity) {
        if (!entity.entityContext.blockPosTrackerComponent.shouldTriggerStandOn()) {
            return;
        }

        final BlockLegacy block = entity.entityContext.blockPosTrackerComponent.currentBlock();
        if (block == null) {
            return;
        }

        if (block.isHoneyBlock() || block.isSlimeBlock()) {
            if (!entity.entityContext.actorDataFlagComponent.has(Entity.DATA_FLAG_SNEAKING)) {
                entity.entityContext.standOnSpeedAlteringBlockFlagComponent.setPresent(true);
            }
            return;
        }

        entity.entityContext.standOnOtherBlockFlagComponent.setPresent(true);
    }
}
