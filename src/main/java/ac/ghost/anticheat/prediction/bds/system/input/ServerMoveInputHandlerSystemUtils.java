package ac.ghost.anticheat.prediction.bds.system.input;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.ActorDataFlag;
import ac.ghost.anticheat.prediction.bds.component.MoveInputComponent;
import ac.ghost.anticheat.prediction.bds.component.PlayerActionComponent;
import ac.ghost.anticheat.prediction.bds.player.PlayerMovement;
import ac.ghost.anticheat.util.math.Vec3;


public final class ServerMoveInputHandlerSystemUtils {
    private static final int DIRECT_SNEAK_MASK =
            MoveInputComponent.SNEAK_DOWN | MoveInputComponent.DESCEND;

    private ServerMoveInputHandlerSystemUtils() {
    }

    
    public static void _tickServerMoveInputHandler(final GhostPlayer player) {
        final MoveInputComponent input = player.entityContext.moveInputComponent;
        final Vec3 unscaled = player.entityContext.mobTravelComponent.getInput();
        Vec3 resolved = PlayerMovement.calculateMoveVector(
                unscaled,
                input,
                player.entityContext.movementAbilitiesComponent.isFlying(),
                player.entityContext.actorDataFlagComponent,
                player.ghostMovementBridgeState.waterSample.touching(),
                player.entityContext.sneakingComponent);

        
        
        
        final boolean existingSneakState =
                (input.getFlags() & DIRECT_SNEAK_MASK) != 0
                        || player.entityContext.playerActionComponent
                        .has(PlayerActionComponent.START_SNEAKING)
                        || player.entityContext.playerActionComponent
                        .has(PlayerActionComponent.STOP_SNEAKING)
                        || player.entityContext.actorDataFlagComponent
                        .has(ActorDataFlag.SNEAKING);
        if (existingSneakState) {
            final float multiplier =
                    player.entityContext.movementAbilitiesComponent.isFlying()
                            || player.ghostMovementBridgeState.waterSample.touching()
                            ? 1.0F
                            : player.entityContext.sneakingComponent.getMovementScale();
            resolved = unscaled.multiply(multiplier, 1.0F, multiplier);
        }

        player.entityContext.mobTravelComponent.setInput(resolved);
        input.setEffective(resolved.x, resolved.z);
    }
}
