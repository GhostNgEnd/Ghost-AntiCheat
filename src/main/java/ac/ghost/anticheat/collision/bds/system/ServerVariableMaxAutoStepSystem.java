package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.entity.Entity;


public final class ServerVariableMaxAutoStepSystem {
    private static final float DEFAULT_MAX_AUTO_STEP = 0.5625F;
    private static final float WASD_CONTROLLED_MAX_AUTO_STEP = 1.0625F;

    private ServerVariableMaxAutoStepSystem() {
    }

    public static void run(final GhostPlayer player) {
        player.entityContext.maxAutoStepComponent.set(
                player.entityContext.actorDataFlagComponent.has(Entity.DATA_FLAG_WASD_CONTROLLED)
                        ? WASD_CONTROLLED_MAX_AUTO_STEP
                        : DEFAULT_MAX_AUTO_STEP);
    }
}
