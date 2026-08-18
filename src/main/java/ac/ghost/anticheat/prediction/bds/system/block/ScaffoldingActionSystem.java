package ac.ghost.anticheat.prediction.bds.system.block;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MoveInputComponent;
import ac.ghost.anticheat.prediction.bds.component.PlayerInputModeComponent;
import ac.ghost.anticheat.prediction.bds.component.StateVectorComponent;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.entity.Entity;


public final class ScaffoldingActionSystem {
    public static final int DESCEND_THROUGH_BLOCK_FLAG = 71;
    private static final float DESCEND_VELOCITY =
            Float.intBitsToFloat(0xBE19999A);

    private ScaffoldingActionSystem() {
    }

    public static void tick(final GhostPlayer player,
                            final StateVectorComponent stateVector) {
        final boolean overDescendable = player.entityContext.actorDataFlagComponent
                .has(Entity.DATA_FLAG_OVER_DESCENDABLE_BLOCK);
        final MoveInputComponent input = player.entityContext.moveInputComponent;
        final int mode = player.entityContext.playerInputModeComponent.getValue();

        final boolean descend = overDescendable && switch (mode) {
            case PlayerInputModeComponent.MOUSE -> input.isSneaking();
            case PlayerInputModeComponent.TOUCH ->
                    input.hasFlag(MoveInputComponent.DESCEND_BLOCK);
            case PlayerInputModeComponent.GAME_PAD ->
                    input.hasFlag(MoveInputComponent.SNEAK_TOGGLE_DOWN)
                            && input.hasFlag(MoveInputComponent.SNEAK_DOWN)
                            && input.isSneaking();
            default -> false;
        };

        player.entityContext.actorDataFlagComponent.set(DESCEND_THROUGH_BLOCK_FLAG, descend);
        if (descend) {
            
            
            
            
            stateVector.getDelta().y = DESCEND_VELOCITY;
        }
    }

    
    public static Vec3 applyAcceptedDescend(final GhostPlayer player,
                                            final Vec3 velocity) {
        final Vec3 result = velocity.clone();
        if (player.entityContext.actorDataFlagComponent.has(
                DESCEND_THROUGH_BLOCK_FLAG)) {
            result.y = DESCEND_VELOCITY;
        }
        return result;
    }
}
