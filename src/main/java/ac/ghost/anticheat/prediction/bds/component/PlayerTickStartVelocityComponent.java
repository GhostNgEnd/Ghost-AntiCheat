package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.util.math.Vec3;











public final class PlayerTickStartVelocityComponent {
    private Candidate uncertainVelocity;
    private Candidate certainVelocity;
    private Vec3 selectedVelocity = Vec3.ZERO.clone();
    private Type selectedType = Type.NORMAL;
    private boolean comparedCandidates;
    private float uncertainDistanceSquared = Float.NaN;
    private float ordinaryDistanceSquared = Float.NaN;

    public synchronized void acknowledgeUncertain(final Vec3 velocity) {
        this.uncertainVelocity = new Candidate(Type.VELOCITY, velocity, false);
    }

    public synchronized void promoteUncertain() {
        if (this.uncertainVelocity != null) {
            this.certainVelocity = this.uncertainVelocity;
        }
        this.uncertainVelocity = null;
    }

    




    public synchronized Candidates begin(final Vec3 fallbackVelocity) {
        this.comparedCandidates = false;
        this.uncertainDistanceSquared = Float.NaN;
        this.ordinaryDistanceSquared = Float.NaN;
        final Candidate ordinary = this.certainVelocity == null
                ? new Candidate(Type.NORMAL, fallbackVelocity, false)
                : this.certainVelocity.copy();
        this.certainVelocity = null;

        final Candidate uncertain = this.uncertainVelocity == null
                ? null : this.uncertainVelocity.copy();
        if (this.uncertainVelocity != null && this.uncertainVelocity.twice()) {
            
            
            this.uncertainVelocity = null;
        }

        select(ordinary);
        return new Candidates(uncertain, ordinary);
    }

    public synchronized void finish(final Candidates candidates,
                                    final Candidate selected) {
        if (selected == null) {
            return;
        }
        select(selected);

        final Candidate uncertain = candidates.uncertain();
        if (uncertain == null || this.uncertainVelocity == null) {
            return;
        }
        if (selected.type() == Type.VELOCITY
                && sameVelocity(selected.velocity(), uncertain.velocity())) {
            this.uncertainVelocity = null;
        } else {
            this.uncertainVelocity = new Candidate(
                    Type.VELOCITY, uncertain.velocity(), true);
        }
    }

    public synchronized void recordComparison(
            final float uncertainDistanceSquared,
            final float ordinaryDistanceSquared) {
        this.comparedCandidates = true;
        this.uncertainDistanceSquared = uncertainDistanceSquared;
        this.ordinaryDistanceSquared = ordinaryDistanceSquared;
    }

    private void select(final Candidate selected) {
        this.selectedType = selected.type();
        this.selectedVelocity = selected.velocity();
    }

    private static boolean sameVelocity(final Vec3 first, final Vec3 second) {
        return Float.floatToRawIntBits(first.x) == Float.floatToRawIntBits(second.x)
                && Float.floatToRawIntBits(first.y) == Float.floatToRawIntBits(second.y)
                && Float.floatToRawIntBits(first.z) == Float.floatToRawIntBits(second.z);
    }

    public synchronized Type selectedType() {
        return this.selectedType;
    }

    public synchronized Vec3 selectedVelocity() {
        return this.selectedVelocity.clone();
    }

    public synchronized boolean selectedServerVelocity() {
        return this.selectedType == Type.VELOCITY;
    }

    public synchronized boolean hasCertainVelocity() {
        return this.certainVelocity != null;
    }

    public synchronized boolean hasUncertainVelocity() {
        return this.uncertainVelocity != null;
    }

    public synchronized boolean comparedCandidates() {
        return this.comparedCandidates;
    }

    public synchronized float uncertainDistanceSquared() {
        return this.uncertainDistanceSquared;
    }

    public synchronized float ordinaryDistanceSquared() {
        return this.ordinaryDistanceSquared;
    }

    public synchronized void clear() {
        this.uncertainVelocity = null;
        this.certainVelocity = null;
        this.selectedVelocity = Vec3.ZERO.clone();
        this.selectedType = Type.NORMAL;
        this.comparedCandidates = false;
        this.uncertainDistanceSquared = Float.NaN;
        this.ordinaryDistanceSquared = Float.NaN;
    }

    public enum Type {
        NORMAL,
        VELOCITY
    }

    public record Candidate(Type type, Vec3 velocity, boolean twice) {
        public Candidate {
            velocity = velocity == null ? Vec3.ZERO.clone() : velocity.clone();
        }

        @Override
        public Vec3 velocity() {
            return velocity.clone();
        }

        public Candidate copy() {
            return new Candidate(type, velocity, twice);
        }
    }

    public record Candidates(Candidate uncertain, Candidate ordinary) {
        public boolean ambiguous() {
            return uncertain != null;
        }
    }
}
