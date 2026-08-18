package ac.ghost.anticheat.player.state;

import cn.nukkit.network.protocol.types.AuthInputAction;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GhostMovementBridgeStateTest {
    @Test
    void legacyButtonStateCreatesEdgesWithoutUsingMotionAxes() {
        final GhostMovementBridgeState state = new GhostMovementBridgeState();

        state.updateLegacyButtonState(true, true);
        final Set<AuthInputAction> pressed =
                state.consumeLegacyInputActions();
        assertTrue(pressed.contains(AuthInputAction.START_JUMPING));
        assertTrue(pressed.contains(AuthInputAction.JUMPING));
        assertTrue(pressed.contains(AuthInputAction.START_SNEAKING));
        assertTrue(pressed.contains(AuthInputAction.SNEAKING));

        state.updateLegacyButtonState(false, false);
        final Set<AuthInputAction> released =
                state.consumeLegacyInputActions();
        assertTrue(released.contains(AuthInputAction.STOP_SNEAKING));
        assertFalse(released.contains(AuthInputAction.JUMPING));
    }

    @Test
    void resetClearsLegacyPacketCapabilitiesAndPendingActions() {
        final GhostMovementBridgeState state = new GhostMovementBridgeState();
        state.predictionHasRawMoveVector = true;
        state.predictionHasDigitalDirectionState = false;
        state.queueLegacyInputAction(AuthInputAction.START_SPRINTING);
        state.legacyInputTick = 42L;

        state.resetLegacyInputBridge();

        assertFalse(state.predictionHasRawMoveVector);
        assertTrue(state.predictionHasDigitalDirectionState);
        assertTrue(state.consumeLegacyInputActions().isEmpty());
        assertTrue(state.legacyInputTick == 0L);
    }
}
