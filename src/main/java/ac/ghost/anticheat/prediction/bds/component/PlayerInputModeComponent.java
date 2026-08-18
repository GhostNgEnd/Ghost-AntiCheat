package ac.ghost.anticheat.prediction.bds.component;

import cn.nukkit.network.protocol.types.InputMode;


public final class PlayerInputModeComponent {
    public static final int UNDEFINED = 0;
    public static final int MOUSE = 1;
    public static final int TOUCH = 2;
    public static final int GAME_PAD = 3;
    public static final int MOTION_CONTROLLER = 4;

    private int value = UNDEFINED;
    private InputMode protocolValue = InputMode.UNDEFINED;

    public int getValue() {
        return value;
    }

    public boolean is(final int mode) {
        return this.value == mode;
    }

    public InputMode getProtocolValue() {
        return this.protocolValue;
    }

    public void set(final InputMode inputMode) {
        if (inputMode == null) {
            this.protocolValue = InputMode.UNDEFINED;
            this.value = UNDEFINED;
            return;
        }

        this.protocolValue = inputMode;

        if (inputMode == InputMode.MOUSE) {
            this.value = MOUSE;
        } else if (inputMode == InputMode.TOUCH) {
            this.value = TOUCH;
        } else if (inputMode == InputMode.GAME_PAD) {
            this.value = GAME_PAD;
        } else if (inputMode == InputMode.MOTION_CONTROLLER) {
            this.value = MOTION_CONTROLLER;
        } else {
            this.value = UNDEFINED;
        }
    }
}
