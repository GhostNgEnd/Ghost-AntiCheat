package ac.ghost.anticheat.prediction.bds.system.liquid.water;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MoveInputComponent;
import ac.ghost.anticheat.util.math.Vec3;


public final class WaterSinkInputSystem {
    public static final float SINK_IMPULSE = -0.04F;

    private WaterSinkInputSystem() {
    }

    public static void tick(final GhostPlayer player) {
        player.entityContext.stateVectorComponent.setDelta(apply(player, player.entityContext.stateVectorComponent.getDelta()));
    }

    public static Vec3 apply(final GhostPlayer player, final Vec3 velocity) {
        if (player.entityContext.movementAbilitiesComponent.isFlying()
                || !player.ghostMovementBridgeState.waterSample.touching()
                || (player.entityContext.serverPlayerMovementComponent.getCurrentInputTick() != 1L && player.ghostMovementBridgeState.lavaSample.touching())) {
            return velocity;
        }

        final MoveInputComponent input = player.entityContext.moveInputComponent;
        return input.wantsWaterSink()
                ? velocity.add(0.0F, SINK_IMPULSE, 0.0F)
                : velocity;
    }
}
