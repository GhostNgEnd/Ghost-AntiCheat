package ac.ghost.anticheat.util;

import ac.ghost.anticheat.check.api.Check;
import ac.ghost.anticheat.check.api.impl.PingBasedCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@RequiredArgsConstructor
public final class LatencyUtil {
    private final GhostPlayer player;

    




    private final Deque<Latency> pending = new ArrayDeque<>();
    private volatile int inFlight;

    public volatile Latency prevAcceptedLatency;
    public volatile long prevAcceptedTime = System.currentTimeMillis();

    public boolean hasInFlight() {
        return this.inFlight > 0;
    }

    public synchronized boolean hasPending(long id, boolean ours) {
        for (Latency latency : this.pending) {
            if (latency.id() == id && latency.ours() == ours) {
                return true;
            }
        }
        return false;
    }

    public synchronized void queue(long id, boolean ours) {
        this.pending.addLast(new Latency(id, System.currentTimeMillis(), System.nanoTime(), ours,
                ours ? new ArrayList<>() : null));
        this.inFlight = this.pending.size();
    }

    public void queue(Runnable runnable) {
        synchronized (this) {
            final Latency target = this.pending.peekLast();
            if (target != null) {
                target.addTask(runnable);
                return;
            }
        }
        runnable.run();
    }

    public void onLatencyAccepted(Latency latency) {
        if (this.player.isExempted()) {
            return;
        }
        for (final Check check : this.player.getCheckHolder().values()) {
            if (!(check instanceof PingBasedCheck pingBasedCheck)) {
                continue;
            }

            pingBasedCheck.onLatencyAccepted(latency);
        }
    }

    
    public synchronized void reset() {
        this.pending.clear();
        this.inFlight = 0;
        this.prevAcceptedLatency = null;
        this.prevAcceptedTime = System.currentTimeMillis();
    }

    




    public boolean onResponse(long id) {
        return this.releaseThrough(id, true);
    }

    





    public boolean onOrderedResponse() {
        final Latency latency;
        synchronized (this) {
            latency = this.pending.pollFirst();
            this.inFlight = this.pending.size();
        }
        if (latency == null) {
            return false;
        }

        latency.run();
        try {
            onLatencyAccepted(latency);
        } catch (Throwable ignored) {
        }
        this.prevAcceptedLatency = latency;
        this.prevAcceptedTime = System.currentTimeMillis();
        return latency.ours();
    }

    




    public boolean onLocalBarrier(final long id) {
        return this.releaseThrough(id, false);
    }

    private boolean releaseThrough(final long id,
                                   final boolean clientAcknowledged) {
        final List<Latency> released = new ArrayList<>();
        final boolean ours;

        synchronized (this) {
            Latency match = null;
            for (Latency latency : this.pending) {
                if (latency.id() == id) {
                    match = latency;
                    break;
                }
            }
            if (match == null) {
                return false;
            }

            ours = match.ours();
            while (!this.pending.isEmpty()) {
                final Latency head = this.pending.removeFirst();
                released.add(head);
                if (head == match) {
                    break;
                }
            }
            this.inFlight = this.pending.size();
        }

        for (Latency latency : released) {
            latency.run();
            if (clientAcknowledged) {
                try {
                    onLatencyAccepted(latency);
                } catch (Throwable ignored) {
                }
                this.prevAcceptedLatency = latency;
            }
            this.prevAcceptedTime = System.currentTimeMillis();
        }

        return ours;
    }

    @ToString
    @AllArgsConstructor
    public static final class Latency {
        private final long id;
        private final long ms;
        private final long ns;
        private boolean ours;
        private List<Runnable> tasks;

        public boolean ours() {
            return this.ours;
        }

        public long id() {
            return this.id;
        }

        public long ns() {
            return this.ns;
        }

        public long ms() {
            return this.ms;
        }

        public synchronized void addTask(Runnable task) {
            if (this.tasks == null) {
                this.tasks = new ArrayList<>();
            }
            this.tasks.add(task);
        }

        public void run() {
            final List<Runnable> pendingTasks;
            synchronized (this) {
                pendingTasks = this.tasks;
                this.tasks = null;
            }
            if (pendingTasks == null) {
                return;
            }

            for (final Runnable task : pendingTasks) {
                try {
                    task.run();
                } catch (Throwable ignored) {
                }
            }
        }
    }
}
