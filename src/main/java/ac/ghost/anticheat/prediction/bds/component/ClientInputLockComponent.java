package ac.ghost.anticheat.prediction.bds.component;


public final class ClientInputLockComponent {
    public static final int BLOCK_POSITIVE_X = 0x0080;
    public static final int BLOCK_NEGATIVE_X = 0x0100;
    public static final int BLOCK_POSITIVE_Y = 0x0200;
    public static final int BLOCK_NEGATIVE_Y = 0x0400;

    private int mask;

    public int getMask() {
        return mask;
    }

    public void setMask(final int mask) {
        this.mask = mask & 0xFFFF;
    }

    
    public void applyTo(final MoveInputComponent input) {
        float x = input.getAxisX();
        float y = input.getAxisY();

        if ((mask & BLOCK_POSITIVE_X) != 0) {
            x = Math.min(x, 0.0F);
            input.clearFlag(MoveInputComponent.LEFT
                    | MoveInputComponent.UP_LEFT
                    | MoveInputComponent.DOWN_LEFT);
        }
        if ((mask & BLOCK_NEGATIVE_X) != 0) {
            x = Math.max(x, 0.0F);
            input.clearFlag(MoveInputComponent.RIGHT
                    | MoveInputComponent.UP_RIGHT
                    | MoveInputComponent.DOWN_RIGHT);
        }
        if ((mask & BLOCK_POSITIVE_Y) != 0) {
            y = Math.min(y, 0.0F);
            input.clearFlag(MoveInputComponent.UP
                    | MoveInputComponent.UP_LEFT
                    | MoveInputComponent.UP_RIGHT);
        }
        if ((mask & BLOCK_NEGATIVE_Y) != 0) {
            y = Math.max(y, 0.0F);
            input.clearFlag(MoveInputComponent.DOWN
                    | MoveInputComponent.DOWN_LEFT
                    | MoveInputComponent.DOWN_RIGHT);
        }

        input.setAxisX(x);
        input.setAxisY(y);
    }
}
