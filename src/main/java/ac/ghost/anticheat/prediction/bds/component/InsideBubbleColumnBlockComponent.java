package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.prediction.bds.math.BdsMovementMath;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.BlockVector3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;










public final class InsideBubbleColumnBlockComponent {
    private static final float DOWN_ACCELERATION =
            Float.intBitsToFloat(0xBCF5C28F);
    private static final float ABOVE_DOWN_CAP =
            Float.intBitsToFloat(0xBF666666);
    private static final float ABOVE_UP_ACCELERATION =
            Float.intBitsToFloat(0x3DCCCCCD);
    private static final float ABOVE_UP_CAP =
            Float.intBitsToFloat(0x3FE66666);
    private static final float INSIDE_DOWN_CAP =
            Float.intBitsToFloat(0xBE99999A);
    private static final float INSIDE_UP_ACCELERATION =
            Float.intBitsToFloat(0x3D75C28F);
    private static final float INSIDE_UP_CAP =
            Float.intBitsToFloat(0x3F333333);
    public enum ContactState {
        ABOVE(0),
        INSIDE(1);

        private final int nativeValue;

        ContactState(final int nativeValue) {
            this.nativeValue = nativeValue;
        }

        public int nativeValue() {
            return nativeValue;
        }
    }

    public static final class Entry {
        private final BlockVector3 position;
        private final boolean dragDown;
        private final ContactState contactState;

        private Entry(final BlockVector3 position,
                      final boolean dragDown,
                      final ContactState contactState) {
            this.position = position;
            this.dragDown = dragDown;
            this.contactState = contactState;
        }

        public BlockVector3 position() {
            return position;
        }

        public boolean dragDown() {
            return dragDown;
        }

        public ContactState contactState() {
            return contactState;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    public void clear() {
        entries.clear();
    }

    public void add(final BlockVector3 position,
                    final boolean dragDown,
                    final ContactState contactState) {
        entries.add(new Entry(position, dragDown, contactState));
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public List<Entry> entries() {
        return Collections.unmodifiableList(entries);
    }
    
    public void applyTo(final StateVectorComponent stateVector) {
        if (isEmpty()) {
            return;
        }

        final Vec3 velocity = stateVector.getDelta();
        for (Entry entry : entries) {
            final float currentY = velocity.y;
            final float candidate;
            final float result;

            switch (entry.contactState()) {
                case ABOVE -> {
                    if (entry.dragDown()) {
                        candidate = currentY + DOWN_ACCELERATION;
                        result = BdsMovementMath.maxss(ABOVE_DOWN_CAP, candidate);
                    } else {
                        candidate = currentY + ABOVE_UP_ACCELERATION;
                        result = BdsMovementMath.minss(ABOVE_UP_CAP, candidate);
                    }
                }
                case INSIDE -> {
                    if (entry.dragDown()) {
                        candidate = currentY + DOWN_ACCELERATION;
                        result = BdsMovementMath.maxss(INSIDE_DOWN_CAP, candidate);
                    } else {
                        candidate = currentY + INSIDE_UP_ACCELERATION;
                        result = BdsMovementMath.minss(INSIDE_UP_CAP, candidate);
                    }
                }
                default -> throw new IllegalStateException(
                        "Unexpected bubble-column contact state: "
                                + entry.contactState().nativeValue());
            }

            velocity.y = result;
        }
    }

}
