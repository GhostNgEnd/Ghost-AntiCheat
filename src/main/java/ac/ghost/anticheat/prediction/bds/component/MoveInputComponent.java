package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.util.math.Vec3;





public final class MoveInputComponent {
    public static final int SNEAK_DOWN = 0x0001;
    public static final int SNEAK_TOGGLE_DOWN = 0x0002;
    public static final int WANT_DOWN_SLOW = 0x0004;
    public static final int WANT_UP_SLOW = 0x0008;
    public static final int ASCEND_BLOCK = 0x0020;
    public static final int DESCEND_BLOCK = 0x0040;
    public static final int JUMP_DOWN = 0x0080;
    public static final int SPRINT_DOWN = 0x0100;
    public static final int UP_LEFT = 0x0200;
    public static final int UP_RIGHT = 0x0400;
    public static final int DOWN_LEFT = 0x0800;
    public static final int DOWN_RIGHT = 0x1000;
    public static final int UP = 0x2000;
    public static final int DOWN = 0x4000;
    public static final int LEFT = 0x8000;
    public static final int RIGHT = 0x10000;
    public static final int ASCEND = 0x20000;
    public static final int DESCEND = 0x40000;
    public static final int CHANGE_HEIGHT = 0x80000;

    public static final int STATE_SNEAKING = 0x01;
    public static final int STATE_SPRINTING = 0x02;
    public static final int STATE_WANT_UP = 0x04;
    public static final int STATE_WANT_DOWN = 0x08;
    public static final int STATE_JUMPING = 0x10;
    public static final int STATE_AUTO_JUMPING_IN_WATER = 0x20;
    public static final int STATE_PERSIST_SNEAK = 0x80;

    private int flags;
    private int stateFlags;
    private float axisX;
    private float axisY;
    private float effectiveX;
    private float effectiveY;
    private boolean paddlingLeft;
    private boolean paddlingRight;
    private boolean inputActive;
    private boolean previousInputActive;

    public void clearForPacket() {
        this.flags = 0;
        this.stateFlags = 0;
        this.axisX = 0.0F;
        this.axisY = 0.0F;
        this.effectiveX = 0.0F;
        this.effectiveY = 0.0F;
        this.paddlingLeft = false;
        this.paddlingRight = false;
    }

    
    public void reset() {
        clearForPacket();
        this.inputActive = false;
        this.previousInputActive = false;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(final int flags) {
        this.flags = flags;
    }

    public void addFlag(final int flag) {
        this.flags |= flag;
    }

    public void clearFlag(final int flag) {
        this.flags &= ~flag;
    }

    public boolean hasFlag(final int flag) {
        return (this.flags & flag) != 0;
    }

    public int getStateFlags() {
        return stateFlags;
    }

    public void setStateFlags(final int stateFlags) {
        this.stateFlags = stateFlags;
    }

    public void addStateFlag(final int flag) {
        this.stateFlags |= flag;
    }

    public void clearStateFlag(final int flag) {
        this.stateFlags &= ~flag;
    }

    public boolean hasStateFlag(final int flag) {
        return (this.stateFlags & flag) != 0;
    }

    



    public boolean wantsWaterSink() {
        return hasFlag(WANT_DOWN_SLOW) || hasStateFlag(STATE_WANT_DOWN);
    }

    


    public boolean isJumpingInLiquid() {
        return hasStateFlag(STATE_JUMPING)
                || hasStateFlag(STATE_AUTO_JUMPING_IN_WATER);
    }

    public float getAxisX() {
        return axisX;
    }

    public void setAxisX(final float axisX) {
        this.axisX = axisX;
    }

    public float getAxisY() {
        return axisY;
    }

    public void setAxisY(final float axisY) {
        this.axisY = axisY;
    }

    public float getEffectiveX() {
        return effectiveX;
    }

    public float getEffectiveY() {
        return effectiveY;
    }

    public void setEffective(final float x, final float y) {
        this.effectiveX = x;
        this.effectiveY = y;
    }

    public boolean isSneaking() {
        return hasStateFlag(STATE_SNEAKING);
    }

    public void setSneaking(final boolean sneaking) {
        if (sneaking) {
            addStateFlag(STATE_SNEAKING);
        } else {
            clearStateFlag(STATE_SNEAKING);
        }
    }

    public boolean isPaddlingLeft() {
        return paddlingLeft;
    }

    public void setPaddlingLeft(final boolean paddlingLeft) {
        this.paddlingLeft = paddlingLeft;
    }

    public boolean isPaddlingRight() {
        return paddlingRight;
    }

    public void setPaddlingRight(final boolean paddlingRight) {
        this.paddlingRight = paddlingRight;
    }

    public boolean isInputActive() {
        return inputActive;
    }

    public boolean wasInputActive() {
        return previousInputActive;
    }

    public void setInputActive(final boolean inputActive) {
        this.previousInputActive = this.inputActive;
        this.inputActive = inputActive;
    }
    
    public Vec3 resolveMovementVector() {
        final float normalizeEpsilon = 0.0001F;
        float x = axisX;
        float y = axisY;

        if (x == 0.0F && y == 0.0F) {
            if (hasFlag(UP)) y += 1.0F;
            if (hasFlag(DOWN)) y -= 1.0F;
            if (hasFlag(LEFT)) x += 1.0F;
            if (hasFlag(RIGHT)) x -= 1.0F;
            if (hasFlag(UP_LEFT)) { x += 1.0F; y += 1.0F; }
            if (hasFlag(UP_RIGHT)) { x -= 1.0F; y += 1.0F; }
            if (hasFlag(DOWN_LEFT)) { x += 1.0F; y -= 1.0F; }
            if (hasFlag(DOWN_RIGHT)) { x -= 1.0F; y -= 1.0F; }

            final float lengthSquared = x * x + y * y;
            if (lengthSquared < normalizeEpsilon) {
                x = 0.0F;
                y = 0.0F;
            } else {
                final float inverseLength =
                        1.0F / (float) Math.sqrt(lengthSquared);
                x *= inverseLength;
                y *= inverseLength;
            }
        } else {
            final float lengthSquared = x * x + y * y;
            if (lengthSquared > 1.0F) {
                final float inverseLength =
                        1.0F / (float) Math.sqrt(lengthSquared);
                x *= inverseLength;
                y *= inverseLength;
            }
        }

        setEffective(x, y);
        return new Vec3(x, 0.0F, y);
    }

}
