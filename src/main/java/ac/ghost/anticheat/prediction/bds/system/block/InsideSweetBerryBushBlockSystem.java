package ac.ghost.anticheat.prediction.bds.system.block;

import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.math.BlockVector3;


public final class InsideSweetBerryBushBlockSystem {
    private InsideSweetBerryBushBlockSystem() {
    }

    public static void tick(final GhostPlayer player,
                            final BlockLegacy blockState,
                            final BlockVector3 position) {
        if (!blockState.isSweetBerryBush()) {
            return;
        }

        
        
        
        
        player.entityContext.insideSweetBerryBushBlockComponent.add(position, blockState);
        player.entityContext.insideSlowingSweetBerryBushBlockComponent.markPresent();
    }
}
