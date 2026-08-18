package ac.ghost.anticheat.data.block;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MoveInputComponent;
import ac.ghost.anticheat.util.math.Box;
import cn.nukkit.math.BlockVector3;

import java.util.List;


public final class PowderSnowBlock {
    private static final List<Box> EMPTY = List.of();
    private static final List<Box> FULL = List.of(new Box(0, 0, 0, 1, 1, 1));
    private static final List<Box> FALLING = List.of(new Box(0, 0, 0, 1, 0.9F, 1));

    private PowderSnowBlock() {
    }

    public static List<Box> getCollisionShape(final GhostPlayer player,
                                              final BlockVector3 blockPosition) {
        if (player == null) {
            return EMPTY;
        }

        final float blockTop = blockPosition.getY() + 1.0F;
        if (player.entityContext.aabbShapeComponent.getAABB().minY < blockTop - Math.ulp(1.0F)) {
            return EMPTY;
        }
        if (isDescending(player)) {
            return EMPTY;
        }
        if (player.entityContext.fallDistanceComponent.getValue() > 2.5F) {
            return FALLING;
        }
        if (player.entityContext.canStandOnSnowFlagComponent.isPresent()) {
            return FULL;
        }
        return EMPTY;
    }

    private static boolean isDescending(final GhostPlayer player) {
        return player.entityContext.moveInputComponent
                .hasStateFlag(MoveInputComponent.STATE_SNEAKING)
                || player.entityContext.moveInputComponent.hasFlag(MoveInputComponent.DESCEND);
    }
}
