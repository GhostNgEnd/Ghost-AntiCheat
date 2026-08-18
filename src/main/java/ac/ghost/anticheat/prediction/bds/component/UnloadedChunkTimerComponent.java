package ac.ghost.anticheat.prediction.bds.component;


public final class UnloadedChunkTimerComponent {
    public boolean insideUnloadedChunk;
    public int ticks;

    public void update(final boolean inside) {
        this.insideUnloadedChunk = inside;
        this.ticks = inside ? this.ticks + 1 : 0;
    }
}
