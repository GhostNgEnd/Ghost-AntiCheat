package ac.ghost.anticheat.prediction.bds.system.input;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MoveInputComponent;








public final class JumpInputSystem {
    private JumpInputSystem() {
    }

    public static void tick(final GhostPlayer player) {
        final MoveInputComponent input = player.entityContext.moveInputComponent;
        final boolean jumping = (input.getStateFlags()
                & (MoveInputComponent.STATE_JUMPING
                | MoveInputComponent.STATE_AUTO_JUMPING_IN_WATER)) != 0
                || input.hasFlag(MoveInputComponent.ASCEND_BLOCK);
        player.entityContext.mobIsJumpingFlagComponent.setPresent(
                jumping && !player.entityContext.movementAbilitiesComponent.isFlying());
    }
}
