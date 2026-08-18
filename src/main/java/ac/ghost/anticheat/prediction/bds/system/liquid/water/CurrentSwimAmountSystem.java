package ac.ghost.anticheat.prediction.bds.system.liquid.water;

import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.entity.Entity;


public final class CurrentSwimAmountSystem {
    private CurrentSwimAmountSystem() {
    }

    public static void tick(final GhostPlayer player) {
        final var component = player.entityContext.swimAmountComponent;
        component.setPrevious(component.getCurrent());
        
        
        
        final boolean horizontalSwimPose = player.ghostMovementBridgeState.wasPredictionSwimming
                || player.ghostMovementBridgeState.wasPredictionCrawling;
        final float target = horizontalSwimPose ? 1.0F : 0.0F;
        if (component.getCurrent() < target) {
            component.setCurrent(Math.min(target, component.getCurrent() + 0.1F));
        } else if (component.getCurrent() > target) {
            component.setCurrent(Math.max(target, component.getCurrent() - 0.1F));
        }
    }
}
