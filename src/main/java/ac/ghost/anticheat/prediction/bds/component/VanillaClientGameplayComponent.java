package ac.ghost.anticheat.prediction.bds.component;






public final class VanillaClientGameplayComponent {
    public static final int SCAFFOLDING_INTENT_GATE = 0x10;

    private int flags = SCAFFOLDING_INTENT_GATE;

    public int getFlags() {
        return flags;
    }

    public void setFlags(final int flags) {
        this.flags = flags;
    }

    public boolean hasFlag(final int flag) {
        return (this.flags & flag) != 0;
    }
}
