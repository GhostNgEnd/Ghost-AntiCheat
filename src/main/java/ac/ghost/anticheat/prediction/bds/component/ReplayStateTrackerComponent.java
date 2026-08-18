package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Vec3;












public final class ReplayStateTrackerComponent {
    public static final long ACTOR_FLAG_MASK_0 = 0x030010811000011AL;
    public static final long ACTOR_FLAG_MASK_1 = 0x00041018100000E0L;
    public static final long ACTOR_FLAG_MASK_2 = 0L;
    public static final long HORSE_FLAG_MASK = 0x20L;

    private long actorFlags0;
    private long actorFlags1;
    private long actorFlags2;
    private long changedActorFlags0;
    private long changedActorFlags1;
    private long changedActorFlags2;
    private long horseFlags;
    private long changedHorseFlags;
    private byte jumpDuration;
    private boolean jumpDurationChanged;
    private Vec3 boundingBox = Vec3.ZERO.clone();
    private boolean boundingBoxChanged;
    private Vec3 seatOffset = Vec3.ZERO.clone();
    private boolean seatOffsetChanged;

    public void discardHistory(final GhostPlayer player) {
        captureCurrent(player);
        clearChangedState();
    }

    public void accumulate(final GhostPlayer player) {
        player.entityContext.actorDataBoundingBoxComponent.readFrom(player);
        final ActorDataFlagComponent actor = player.entityContext.actorDataFlagComponent;
        final long next0 = actor.getWord(0) & ACTOR_FLAG_MASK_0;
        final long next1 = actor.getWord(1) & ACTOR_FLAG_MASK_1;
        final long next2 = actor.getWord(2) & ACTOR_FLAG_MASK_2;
        this.changedActorFlags0 |= this.actorFlags0 ^ next0;
        this.changedActorFlags1 |= this.actorFlags1 ^ next1;
        this.changedActorFlags2 |= this.actorFlags2 ^ next2;
        this.actorFlags0 = next0;
        this.actorFlags1 = next1;
        this.actorFlags2 = next2;

        final long nextHorse = player.entityContext.actorDataHorseFlagComponent.getFlags() & HORSE_FLAG_MASK;
        this.changedHorseFlags |= this.horseFlags ^ nextHorse;
        this.horseFlags = nextHorse;

        final byte nextJump = player.entityContext.actorDataJumpDurationComponent.getDuration();
        this.jumpDurationChanged |= this.jumpDuration != nextJump;
        this.jumpDuration = nextJump;

        final Vec3 nextBoundingBox = player.entityContext.actorDataBoundingBoxComponent.getValue();
        this.boundingBoxChanged |= !same(this.boundingBox, nextBoundingBox);
        this.boundingBox = nextBoundingBox;

        final Vec3 nextSeatOffset = player.entityContext.actorDataSeatOffsetComponent.getValue();
        this.seatOffsetChanged |= !same(this.seatOffset, nextSeatOffset);
        this.seatOffset = nextSeatOffset;
    }

    
    public void includeDifferencesFromCurrent(final GhostPlayer player) {
        player.entityContext.actorDataBoundingBoxComponent.readFrom(player);
        this.changedActorFlags0 |= this.actorFlags0
                ^ (player.entityContext.actorDataFlagComponent.getWord(0) & ACTOR_FLAG_MASK_0);
        this.changedActorFlags1 |= this.actorFlags1
                ^ (player.entityContext.actorDataFlagComponent.getWord(1) & ACTOR_FLAG_MASK_1);
        this.changedActorFlags2 |= this.actorFlags2
                ^ (player.entityContext.actorDataFlagComponent.getWord(2) & ACTOR_FLAG_MASK_2);
        this.changedHorseFlags |= this.horseFlags
                ^ (player.entityContext.actorDataHorseFlagComponent.getFlags() & HORSE_FLAG_MASK);
        this.jumpDurationChanged |= this.jumpDuration
                != player.entityContext.actorDataJumpDurationComponent.getDuration();
        this.boundingBoxChanged |= !same(this.boundingBox,
                player.entityContext.actorDataBoundingBoxComponent.getValue());
        this.seatOffsetChanged |= !same(this.seatOffset,
                player.entityContext.actorDataSeatOffsetComponent.getValue());
    }

    public ReplayStateTrackerComponent copy() {
        final ReplayStateTrackerComponent copy = new ReplayStateTrackerComponent();
        copy.actorFlags0 = actorFlags0;
        copy.actorFlags1 = actorFlags1;
        copy.actorFlags2 = actorFlags2;
        copy.changedActorFlags0 = changedActorFlags0;
        copy.changedActorFlags1 = changedActorFlags1;
        copy.changedActorFlags2 = changedActorFlags2;
        copy.horseFlags = horseFlags;
        copy.changedHorseFlags = changedHorseFlags;
        copy.jumpDuration = jumpDuration;
        copy.jumpDurationChanged = jumpDurationChanged;
        copy.boundingBox = boundingBox.clone();
        copy.boundingBoxChanged = boundingBoxChanged;
        copy.seatOffset = seatOffset.clone();
        copy.seatOffsetChanged = seatOffsetChanged;
        return copy;
    }

    public void replace(final ReplayStateTrackerComponent source) {
        if (source == null) { clear(); return; }
        final ReplayStateTrackerComponent copy = source.copy();
        this.actorFlags0 = copy.actorFlags0;
        this.actorFlags1 = copy.actorFlags1;
        this.actorFlags2 = copy.actorFlags2;
        this.changedActorFlags0 = copy.changedActorFlags0;
        this.changedActorFlags1 = copy.changedActorFlags1;
        this.changedActorFlags2 = copy.changedActorFlags2;
        this.horseFlags = copy.horseFlags;
        this.changedHorseFlags = copy.changedHorseFlags;
        this.jumpDuration = copy.jumpDuration;
        this.jumpDurationChanged = copy.jumpDurationChanged;
        this.boundingBox = copy.boundingBox;
        this.boundingBoxChanged = copy.boundingBoxChanged;
        this.seatOffset = copy.seatOffset;
        this.seatOffsetChanged = copy.seatOffsetChanged;
    }

    public void clear() {
        actorFlags0 = actorFlags1 = actorFlags2 = 0L;
        changedActorFlags0 = changedActorFlags1 = changedActorFlags2 = 0L;
        horseFlags = changedHorseFlags = 0L;
        jumpDuration = 0;
        jumpDurationChanged = false;
        boundingBox = Vec3.ZERO.clone();
        boundingBoxChanged = false;
        seatOffset = Vec3.ZERO.clone();
        seatOffsetChanged = false;
    }

    private void captureCurrent(final GhostPlayer player) {
        player.entityContext.actorDataBoundingBoxComponent.readFrom(player);
        actorFlags0 = player.entityContext.actorDataFlagComponent.getWord(0) & ACTOR_FLAG_MASK_0;
        actorFlags1 = player.entityContext.actorDataFlagComponent.getWord(1) & ACTOR_FLAG_MASK_1;
        actorFlags2 = player.entityContext.actorDataFlagComponent.getWord(2) & ACTOR_FLAG_MASK_2;
        horseFlags = player.entityContext.actorDataHorseFlagComponent.getFlags() & HORSE_FLAG_MASK;
        jumpDuration = player.entityContext.actorDataJumpDurationComponent.getDuration();
        boundingBox = player.entityContext.actorDataBoundingBoxComponent.getValue();
        seatOffset = player.entityContext.actorDataSeatOffsetComponent.getValue();
    }

    private void clearChangedState() {
        changedActorFlags0 = changedActorFlags1 = changedActorFlags2 = 0L;
        changedHorseFlags = 0L;
        jumpDurationChanged = false;
        boundingBoxChanged = false;
        seatOffsetChanged = false;
    }

    public long actorFlags0() { return actorFlags0; }
    public long actorFlags1() { return actorFlags1; }
    public long actorFlags2() { return actorFlags2; }
    public long changedActorFlags0() { return changedActorFlags0; }
    public long changedActorFlags1() { return changedActorFlags1; }
    public long changedActorFlags2() { return changedActorFlags2; }
    public long horseFlags() { return horseFlags; }
    public long changedHorseFlags() { return changedHorseFlags; }
    public byte jumpDuration() { return jumpDuration; }
    public boolean jumpDurationChanged() { return jumpDurationChanged; }
    public Vec3 boundingBox() { return boundingBox.clone(); }
    public boolean boundingBoxChanged() { return boundingBoxChanged; }
    public Vec3 seatOffset() { return seatOffset.clone(); }
    public boolean seatOffsetChanged() { return seatOffsetChanged; }

    private static boolean same(final Vec3 a, final Vec3 b) {
        return Float.floatToIntBits(a.x) == Float.floatToIntBits(b.x)
                && Float.floatToIntBits(a.y) == Float.floatToIntBits(b.y)
                && Float.floatToIntBits(a.z) == Float.floatToIntBits(b.z);
    }
}
