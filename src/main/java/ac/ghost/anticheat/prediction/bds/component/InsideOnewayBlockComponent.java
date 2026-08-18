package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.util.math.Box;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;









public final class InsideOnewayBlockComponent {
    private final List<Box> boxes = new ArrayList<>();

    public void add(final Box box) {
        if (box == null || containsExact(box)) {
            return;
        }
        this.boxes.add(box);
    }

    public List<Box> boxes() {
        return Collections.unmodifiableList(this.boxes);
    }

    public boolean isEmpty() {
        return this.boxes.isEmpty();
    }

    public void clear() {
        this.boxes.clear();
    }

    private boolean containsExact(final Box candidate) {
        for (Box box : this.boxes) {
            if (Float.floatToIntBits(box.minX) == Float.floatToIntBits(candidate.minX)
                    && Float.floatToIntBits(box.minY) == Float.floatToIntBits(candidate.minY)
                    && Float.floatToIntBits(box.minZ) == Float.floatToIntBits(candidate.minZ)
                    && Float.floatToIntBits(box.maxX) == Float.floatToIntBits(candidate.maxX)
                    && Float.floatToIntBits(box.maxY) == Float.floatToIntBits(candidate.maxY)
                    && Float.floatToIntBits(box.maxZ) == Float.floatToIntBits(candidate.maxZ)) {
                return true;
            }
        }
        return false;
    }
}
