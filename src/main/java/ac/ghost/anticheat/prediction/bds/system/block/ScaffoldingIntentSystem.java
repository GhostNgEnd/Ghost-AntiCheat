package ac.ghost.anticheat.prediction.bds.system.block;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MoveInputComponent;
import ac.ghost.anticheat.prediction.bds.component.PlayerInputModeComponent;
import ac.ghost.anticheat.prediction.bds.component.VanillaClientGameplayComponent;


public final class ScaffoldingIntentSystem {
    private static final int HOLD_TICKS = 6;

    private ScaffoldingIntentSystem() {
    }

    public static void tick(final GhostPlayer player) {
        if (!player.entityContext.actorMovementTickNeededComponent.isPresent()) {
            return;
        }

        final MoveInputComponent input = player.entityContext.moveInputComponent;
        final boolean holdingControllerSneak =
                player.entityContext.playerInputModeComponent.is(PlayerInputModeComponent.GAME_PAD)
                        && input.hasFlag(MoveInputComponent.SNEAK_TOGGLE_DOWN)
                        && player.entityContext.vanillaClientGameplayComponent.hasFlag(
                        VanillaClientGameplayComponent.SCAFFOLDING_INTENT_GATE);

        final int oldTicks = player.entityContext.playerInputRequestComponent
                .getScaffoldingSneakHoldTicks();
        if (holdingControllerSneak) {
            player.entityContext.playerInputRequestComponent
                    .setScaffoldingSneakHoldTicks(oldTicks + 1);
            if (oldTicks >= HOLD_TICKS - 1) {
                input.addFlag(MoveInputComponent.SNEAK_DOWN);
                input.setSneaking(true);
            }
            return;
        }

        if (oldTicks >= HOLD_TICKS) {
            input.clearFlag(MoveInputComponent.SNEAK_DOWN);
            input.setSneaking(false);
        }
        player.entityContext.playerInputRequestComponent.setScaffoldingSneakHoldTicks(0);
    }
}
