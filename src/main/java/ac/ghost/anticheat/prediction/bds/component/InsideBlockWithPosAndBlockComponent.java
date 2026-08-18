package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.data.block.BlockLegacy;
import cn.nukkit.math.BlockVector3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;






public final class InsideBlockWithPosAndBlockComponent<T> {
    public static final class Entry<T> {
        private final BlockVector3 position;
        private final BlockLegacy block;

        private Entry(final BlockVector3 position,
                      final BlockLegacy block) {
            this.position = position;
            this.block = block;
        }

        public BlockVector3 position() {
            return position;
        }

        public BlockLegacy block() {
            return block;
        }
    }

    private final List<Entry<T>> entries = new ArrayList<>();
    private boolean replayPresence;

    public void clear() {
        entries.clear();
        replayPresence = false;
    }

    public void add(final BlockVector3 position,
                    final BlockLegacy block) {
        entries.add(new Entry<>(position, block));
    }

    public void markReplayPresence() {
        replayPresence = true;
    }

    public boolean isEmpty() {
        return entries.isEmpty() && !replayPresence;
    }

    public List<Entry<T>> entries() {
        return Collections.unmodifiableList(entries);
    }
}
