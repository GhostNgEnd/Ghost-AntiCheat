package ac.ghost.anticheat.prediction.bds.component;


public final class SpinAttackResultsComponent {
    private boolean candidateEntitiesPresent;
    private boolean hitNearbyMob;
    private boolean horizontalCollision;

    public void set(final boolean candidateEntitiesPresent,
                    final boolean hitNearbyMob,
                    final boolean horizontalCollision) {
        this.candidateEntitiesPresent = candidateEntitiesPresent;
        this.hitNearbyMob = hitNearbyMob;
        this.horizontalCollision = horizontalCollision;
    }

    public boolean hasCandidateEntities() {
        return candidateEntitiesPresent;
    }

    public boolean hitNearbyMob() {
        return hitNearbyMob;
    }

    public boolean hasHorizontalCollision() {
        return horizontalCollision;
    }

    public void clear() {
        this.candidateEntitiesPresent = false;
        this.hitNearbyMob = false;
        this.horizontalCollision = false;
    }
}
