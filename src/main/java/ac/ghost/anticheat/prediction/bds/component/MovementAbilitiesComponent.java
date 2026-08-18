package ac.ghost.anticheat.prediction.bds.component;








public final class MovementAbilitiesComponent {
    public static final int FLYING = 1 << 0;
    public static final int MAY_FLY = 1 << 1;
    public static final int INSTABUILD = 1 << 2;
    public static final int OPERATOR_COMMANDS = 1 << 3;
    public static final int NO_CLIP = 1 << 4;
    public static final int WORLD_BUILDER = 1 << 5;

    private int bits;
    private float flySpeed;
    private float verticalFlySpeed;

    public int getBits() {
        return bits;
    }

    public void setBits(final int bits) {
        this.bits = bits;
    }

    public boolean isFlying() {
        return has(FLYING);
    }

    public boolean mayFly() {
        return has(MAY_FLY);
    }

    public boolean isInstabuild() {
        return has(INSTABUILD);
    }

    public boolean hasOperatorCommands() {
        return has(OPERATOR_COMMANDS);
    }

    public boolean isNoClip() {
        return has(NO_CLIP);
    }

    public boolean isWorldBuilder() {
        return has(WORLD_BUILDER);
    }

    public float getFlySpeed() {
        return flySpeed;
    }

    public void setFlySpeed(final float flySpeed) {
        this.flySpeed = flySpeed;
    }

    public float getVerticalFlySpeed() {
        return verticalFlySpeed;
    }

    public void setVerticalFlySpeed(final float verticalFlySpeed) {
        this.verticalFlySpeed = verticalFlySpeed;
    }

    private boolean has(final int ability) {
        return (bits & ability) != 0;
    }
}
