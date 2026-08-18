package ac.ghost.anticheat.prediction.bds.component;

import java.util.ArrayList;
import java.util.List;


public final class BlockCollisionEvaluationQueueComponent {
    public record Entry(int x, int y, int z, int dimension) {}
    private final List<Entry> entries = new ArrayList<>();
    public void enqueue(final int x, final int y, final int z, final int dimension) {
        entries.add(new Entry(x, y, z, dimension));
    }
    public List<Entry> drain() {
        final List<Entry> drained = List.copyOf(entries);
        entries.clear();
        return drained;
    }
    public boolean isEmpty() { return entries.isEmpty(); }
    public void clear() { entries.clear(); }
}
