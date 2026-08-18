package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.player.GhostPlayer;








public final class ExternalDataComponent {
    private GhostPlayer player;

    public record Snapshot(int dimension, long blockSourceRevision) {
        public boolean matches(final GhostPlayer player) {
            return this.dimension == player.packetVisibleChunkCache.getDimension()
                    && this.blockSourceRevision
                    == player.packetVisibleChunkCache.getRevision();
        }
    }

    public void bind(final GhostPlayer player) {
        if (this.player != null && this.player != player) {
            throw new IllegalStateException("ExternalDataComponent already bound");
        }
        this.player = player;
    }

    public GhostPlayer player() {
        return this.player;
    }

    public Snapshot capture(final GhostPlayer player) {
        return new Snapshot(
                player.packetVisibleChunkCache.getDimension(),
                player.packetVisibleChunkCache.getRevision());
    }
}
