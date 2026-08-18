package ac.ghost.anticheat.prediction.bds.system.liquid.water;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.system.liquid.common.LiquidPhysicsSystem;


public final class InWaterSensingSystem {
    private InWaterSensingSystem() {
    }

    public static void tick(final GhostPlayer player,
                            final LiquidPhysicsSystem.Sample sample) {
        if (!player.entityContext.wasInWaterFlagComponent.isPresent() && sample.touching()) {
            player.ghostMovementBridgeState.downwardLiquidEncountered =
                    sample.downwardFlow();
        }
    }
}
