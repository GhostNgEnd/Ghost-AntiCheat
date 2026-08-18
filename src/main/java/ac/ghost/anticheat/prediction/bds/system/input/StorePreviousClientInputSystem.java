package ac.ghost.anticheat.prediction.bds.system.input;

import ac.ghost.anticheat.prediction.bds.component.MoveInputComponent;


public final class StorePreviousClientInputSystem {
    private StorePreviousClientInputSystem() {
    }

    public static void tick(final MoveInputComponent input) {
        final boolean active = input.getEffectiveX() != 0.0F
                || input.getEffectiveY() != 0.0F;
        input.setInputActive(active);
    }
}
