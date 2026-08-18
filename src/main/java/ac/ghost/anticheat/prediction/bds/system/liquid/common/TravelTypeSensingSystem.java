package ac.ghost.anticheat.prediction.bds.system.liquid.common;

import ac.ghost.anticheat.data.FluidState;
import ac.ghost.anticheat.player.GhostPlayer;


public final class TravelTypeSensingSystem {
    private TravelTypeSensingSystem() {
    }

    public static void tick(final GhostPlayer player) {
        apply(player, LiquidPhysicsSystem.sampleLiquids(player, false));
    }

    public static void apply(final GhostPlayer player,
                             final LiquidPhysicsSystem.Result result) {
        player.ghostMovementBridgeState.liquidSampledTick =
                player.entityContext.serverPlayerMovementComponent.getCurrentInputTick();
        player.ghostMovementBridgeState.waterSample = result.water();
        player.ghostMovementBridgeState.lavaSample = result.lava();
        player.entityContext.waterTravelFlagComponent.setPresent(
                result.selectedType() == FluidState.FluidType.WATER);
        player.entityContext.lavaTravelFlagComponent.setPresent(
                result.selectedType() == FluidState.FluidType.LAVA);
    }
}
