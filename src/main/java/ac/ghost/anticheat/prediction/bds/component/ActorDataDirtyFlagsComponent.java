package ac.ghost.anticheat.prediction.bds.component;


public final class ActorDataDirtyFlagsComponent {
    private long actorFlags0;
    private long actorFlags1;
    private long actorFlags2;
    private long horseFlags;
    private long auxiliary;

    public void markActorFlags(final long word0, final long word1, final long word2) {
        actorFlags0 |= word0;
        actorFlags1 |= word1;
        actorFlags2 |= word2;
    }

    public void markHorseFlags(final long mask) { horseFlags |= mask; }
    public void markAuxiliary(final long mask) { auxiliary |= mask; }

    
    public void mark(final long mask) { markAuxiliary(mask); }
    public long getDirtyMask() { return auxiliary; }

    public long[] consumeActorFlags() {
        final long[] result = {actorFlags0, actorFlags1, actorFlags2};
        actorFlags0 = actorFlags1 = actorFlags2 = 0L;
        return result;
    }

    public long consumeHorseFlags() {
        final long result = horseFlags;
        horseFlags = 0L;
        return result;
    }

    public long consume() {
        final long result = auxiliary;
        auxiliary = 0L;
        return result;
    }

    public void clear() {
        actorFlags0 = actorFlags1 = actorFlags2 = 0L;
        horseFlags = 0L;
        auxiliary = 0L;
    }
}
