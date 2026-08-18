package ac.ghost.anticheat.prediction.bds.component;


public final class ActorDataHorseFlagComponent {
    private long flags;

    public long getFlags() { return flags; }
    public void setFlags(final long flags) { this.flags = flags; }
    public boolean has(final int bit) { return bit >= 0 && bit < 64 && (flags & (1L << bit)) != 0L; }
    public void set(final int bit, final boolean value) {
        if (bit < 0 || bit >= 64) throw new IllegalArgumentException("Horse flag: " + bit);
        if (value) flags |= 1L << bit; else flags &= ~(1L << bit);
    }
}
