package ac.ghost.anticheat.prediction.bds.system.block;

import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Mutable;
import cn.nukkit.math.BlockVector3;


public final class SetEntityInsideSystem {
    private SetEntityInsideSystem() {
    }

    public static void setEntityInside(final GhostPlayer player,
                                       final BlockLegacy blockState,
                                       final Mutable position) {
        final BlockVector3 blockPosition = new BlockVector3(
                position.getX(), position.getY(), position.getZ());

        EntityInsideSystem.collectHoneyBlock(player, blockState, position);
        EntityInsideSystem.collectBubbleColumn(player, blockState, position);
        EntityInsideSystem.collectOnewayBlock(player, blockState, blockPosition);
        InsideSweetBerryBushBlockSystem.tick(
                player, blockState, blockPosition);
        EntityInsideSystem.collectWebBlock(
                player, blockState, blockPosition);
        InsidePowderSnowBlockSystem.tickEntityInside(
                player, blockState, blockPosition);
    }

    public static void cleanupSystem(final GhostPlayer player) {
        player.entityContext.insideBubbleColumnBlockComponent.clear();
        player.entityContext.insideHoneyBlockComponent.clear();
        player.entityContext.insideSweetBerryBushBlockComponent.clear();
        player.entityContext.insideSlowingSweetBerryBushBlockComponent.clear();
        player.entityContext.insideWebBlockComponent.clear();
        player.entityContext.insideOnewayBlockComponent.clear();
        player.entityContext.insidePowderSnowBlockComponent.clear();
    }
}
