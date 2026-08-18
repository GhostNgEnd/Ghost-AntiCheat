package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.nukkit.data.ReplayableActorInput;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;











public final class ServerPlayerMovementComponent {
    public static final int HARD_RECORD_LIMIT = 999;
    public static final int HARD_EVENT_LIMIT = 999;

    private final Deque<HistoryRecord> history = new ArrayDeque<>();

    private boolean clearHistoryRequested;
    private boolean inputBlocked;
    private int replayHistoryCounter;

    private long currentInputTick = -1L;
    private long lastAuthInputWallClock = System.currentTimeMillis();
    private long lastReceivedInputTick = -1L;
    private long lastProcessedInputTick = -1L;
    private HistoryRecord processingRecord;

    public Acceptance addPlayerAuthInputPacket(final PlayerAuthInputPacket packet) {
        return this.addPlayerAuthInputPacket(ReplayableActorInput.capture(packet));
    }

    public Acceptance addPlayerAuthInputPacket(final ReplayableActorInput input) {
        if (this.inputBlocked) {
            return Acceptance.INPUT_BLOCKED;
        }
        final long inputTick = input.inputTick();
        if (inputTick < 0L) {
            return Acceptance.NEGATIVE;
        }
        if (inputTick <= this.lastReceivedInputTick) {
            return Acceptance.NON_MONOTONIC;
        }
        if (this.history.size() >= HARD_RECORD_LIMIT) {
            return Acceptance.HISTORY_LIMIT;
        }

        final HistoryRecord record = new HistoryRecord(inputTick, input);
        if (!record.appendEvent(new InputPacketEvent(input))) {
            return Acceptance.EVENT_LIMIT;
        }
        this.history.addLast(record);
        this.lastReceivedInputTick = inputTick;
        this.replayHistoryCounter = saturatingIncrement(this.replayHistoryCounter);
        return Acceptance.ACCEPTED;
    }

    public HistoryRecord beginProcessing(final long inputTick) {
        if (this.processingRecord != null) {
            throw new IllegalStateException("A movement history record is already being processed");
        }
        final HistoryRecord record = this.find(inputTick);
        if (record == null || record.processed()) {
            throw new IllegalStateException("Input tick was not queued: " + inputTick);
        }
        this.processingRecord = record;
        return record;
    }

    public void capturePreSimulationState(final GhostPlayer player) {
        final HistoryRecord record = requireProcessingRecord();
        if (record.replayState() != null) {
            return;
        }
        record.setReplayState(ReplayStateComponent.capture(
                player, record.inputTick()));
        record.setExternalData(player.entityContext.externalDataComponent.capture(player));
        record.setReplayStateTracker(player.entityContext.replayStateTrackerComponent.copy());
        record.setInsideSlowingSweetBerryBush(
                player.entityContext.insideSlowingSweetBerryBushBlockComponent.isPresent());
        record.setInsideWebBlock(player.entityContext.insideWebBlockComponent.isPresent());
        record.setInsidePowderSnowBlock(
                !player.entityContext.insidePowderSnowBlockComponent.isEmpty());
    }

    public void completeProcessing(final long inputTick) {
        final HistoryRecord record = this.processingRecord;
        if (record == null || record.inputTick() != inputTick) {
            return;
        }
        record.setProcessed(true);
        if (inputTick > this.lastProcessedInputTick) {
            this.lastProcessedInputTick = inputTick;
        }
        this.processingRecord = null;
    }

    public HistoryRecord processingRecord() {
        return this.processingRecord;
    }

    public HistoryRecord requireProcessingRecord() {
        if (this.processingRecord == null) {
            throw new IllegalStateException("No movement history record is being processed");
        }
        return this.processingRecord;
    }

    public HistoryRecord find(final long inputTick) {
        for (final HistoryRecord record : this.history) {
            if (record.inputTick() == inputTick) {
                return record;
            }
        }
        return null;
    }

    
    public HistoryRecord newestValidRecord() {
        final Iterator<HistoryRecord> iterator = this.history.descendingIterator();
        while (iterator.hasNext()) {
            final HistoryRecord record = iterator.next();
            if (record.snapshotValid()) {
                return record;
            }
        }
        return null;
    }

    public void trimToHistorySize(final int configuredHistoryTicks) {
        final int target = Math.max(1, Math.min(HARD_RECORD_LIMIT,
                configuredHistoryTicks));
        while (this.history.size() > target) {
            final HistoryRecord first = this.history.peekFirst();
            if (first == this.processingRecord) {
                break;
            }
            this.history.removeFirst();
            this.replayHistoryCounter = saturatingDecrement(
                    this.replayHistoryCounter);
        }
    }

    public void consumeOldestRecord() {
        final HistoryRecord first = this.history.peekFirst();
        if (first == null || first == this.processingRecord) {
            return;
        }
        this.history.removeFirst();
        this.replayHistoryCounter = saturatingDecrement(this.replayHistoryCounter);
    }

    public List<HistoryRecord> historyView() {
        return Collections.unmodifiableList(new ArrayList<>(this.history));
    }

    public int historySize() {
        return this.history.size();
    }

    public long getCurrentInputTick() {
        return this.currentInputTick;
    }

    public void setCurrentInputTick(final long value) {
        this.currentInputTick = value;
    }

    public long getLastAuthInputWallClock() {
        return this.lastAuthInputWallClock;
    }

    public void markAuthInputReceived() {
        this.lastAuthInputWallClock = System.currentTimeMillis();
    }

    public long getLastReceivedInputTick() {
        return this.lastReceivedInputTick;
    }

    public long getLastProcessedInputTick() {
        return this.lastProcessedInputTick;
    }

    public PlayerInputTick getProcessingInputTick() {
        return this.processingRecord == null
                ? null : new PlayerInputTick(this.processingRecord.inputTick());
    }

    public ReplayableActorInput getProcessingInput() {
        return this.processingRecord == null
                ? null : this.processingRecord.input();
    }

    public int getPendingInputCount() {
        int count = 0;
        for (final HistoryRecord record : this.history) {
            if (!record.processed()) {
                count++;
            }
        }
        return count;
    }

    public boolean isClearHistoryRequested() {
        return clearHistoryRequested;
    }

    public void requestClearHistory() {
        this.clearHistoryRequested = true;
    }

    public boolean isInputBlocked() {
        return inputBlocked;
    }

    public void setInputBlocked(final boolean inputBlocked) {
        this.inputBlocked = inputBlocked;
    }

    public int getReplayHistoryCounter() {
        return replayHistoryCounter;
    }

    public void reset() {
        this.history.clear();
        this.clearHistoryRequested = false;
        this.inputBlocked = false;
        this.replayHistoryCounter = 0;
        this.currentInputTick = -1L;
        this.lastReceivedInputTick = -1L;
        this.lastProcessedInputTick = -1L;
        this.processingRecord = null;
    }

    private static int saturatingIncrement(final int value) {
        return value == Integer.MAX_VALUE ? value : value + 1;
    }

    private static int saturatingDecrement(final int value) {
        return value <= 0 ? 0 : value - 1;
    }

    public enum Acceptance {
        ACCEPTED,
        NEGATIVE,
        NON_MONOTONIC,
        INPUT_BLOCKED,
        HISTORY_LIMIT,
        EVENT_LIMIT
    }

    public interface HistoryEvent {
    }

    public record InputPacketEvent(ReplayableActorInput input)
            implements HistoryEvent {
    }

    public static final class HistoryRecord {
        private final long inputTick;
        private final ReplayableActorInput input;
        private final List<HistoryEvent> events = new ArrayList<>();
        private ReplayStateComponent replayState;
        private ExternalDataComponent.Snapshot externalData;
        private ReplayStateTrackerComponent replayStateTracker;
        private RewindCollisionShapesComponent collisionShapes;
        private PredictedMovementComponent authoritativeSnapshot;
        private boolean insideSlowingSweetBerryBush;
        private boolean insideWebBlock;
        private boolean insidePowderSnowBlock;
        private boolean snapshotValid;
        private boolean processed;

        private HistoryRecord(final long inputTick,
                              final ReplayableActorInput input) {
            this.inputTick = inputTick;
            this.input = input;
        }

        public long inputTick() {
            return inputTick;
        }

        public ReplayableActorInput input() {
            return input;
        }

        public List<HistoryEvent> events() {
            return Collections.unmodifiableList(events);
        }

        public boolean appendEvent(final HistoryEvent event) {
            if (event == null || this.events.size() >= HARD_EVENT_LIMIT) {
                return false;
            }
            this.events.add(event);
            return true;
        }

        public ReplayStateComponent replayState() {
            return replayState == null ? null : replayState.copy();
        }

        private void setReplayState(final ReplayStateComponent replayState) {
            this.replayState = replayState == null ? null : replayState.copy();
        }

        public ExternalDataComponent.Snapshot externalData() {
            return externalData;
        }

        private void setExternalData(
                final ExternalDataComponent.Snapshot externalData) {
            this.externalData = externalData;
        }

        public ReplayStateTrackerComponent replayStateTracker() {
            return replayStateTracker == null ? null : replayStateTracker.copy();
        }

        public void setReplayStateTracker(
                final ReplayStateTrackerComponent replayStateTracker) {
            this.replayStateTracker = replayStateTracker == null
                    ? null : replayStateTracker.copy();
        }

        public RewindCollisionShapesComponent collisionShapes() {
            return collisionShapes;
        }

        public void setCollisionShapes(
                final RewindCollisionShapesComponent collisionShapes) {
            this.collisionShapes = collisionShapes;
        }

        public PredictedMovementComponent authoritativeSnapshot() {
            return authoritativeSnapshot;
        }

        public void setAuthoritativeSnapshot(
                final PredictedMovementComponent authoritativeSnapshot) {
            this.authoritativeSnapshot = authoritativeSnapshot;
            this.snapshotValid = authoritativeSnapshot != null;
        }

        public boolean insideSlowingSweetBerryBush() {
            return insideSlowingSweetBerryBush;
        }

        private void setInsideSlowingSweetBerryBush(final boolean value) {
            this.insideSlowingSweetBerryBush = value;
        }

        public boolean insideWebBlock() {
            return insideWebBlock;
        }

        private void setInsideWebBlock(final boolean value) {
            this.insideWebBlock = value;
        }

        public boolean insidePowderSnowBlock() {
            return insidePowderSnowBlock;
        }

        private void setInsidePowderSnowBlock(final boolean value) {
            this.insidePowderSnowBlock = value;
        }

        public boolean snapshotValid() {
            return snapshotValid;
        }

        public boolean processed() {
            return processed;
        }

        private void setProcessed(final boolean processed) {
            this.processed = processed;
        }

    }

}
