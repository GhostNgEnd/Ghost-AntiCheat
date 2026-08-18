package ac.ghost.anticheat.prediction.bds.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class AbilitiesRequestComponent {
    public static final byte BOOLEAN = 0;
    public static final byte FLOAT = 1;

    private final List<Request> requests = new ArrayList<>();

    public void appendBoolean(final int ability, final boolean value) {
        this.requests.add(Request.booleanRequest(ability, value));
    }

    public void appendFloat(final int ability, final float value) {
        this.requests.add(Request.floatRequest(ability, value));
    }

    public boolean isPresent() {
        return !this.requests.isEmpty();
    }

    public List<Request> requests() {
        return Collections.unmodifiableList(this.requests);
    }

    public void clear() {
        this.requests.clear();
    }

    public static final class Request {
        private final int ability;
        private final byte type;
        private final boolean booleanValue;
        private final float floatValue;

        private Request(final int ability,
                        final byte type,
                        final boolean booleanValue,
                        final float floatValue) {
            this.ability = ability;
            this.type = type;
            this.booleanValue = booleanValue;
            this.floatValue = floatValue;
        }

        public static Request booleanRequest(final int ability,
                                             final boolean value) {
            return new Request(ability, BOOLEAN, value, 0.0F);
        }

        public static Request floatRequest(final int ability,
                                           final float value) {
            return new Request(ability, FLOAT, false, value);
        }

        public int ability() {
            return this.ability;
        }

        public byte type() {
            return this.type;
        }

        public boolean booleanValue() {
            return this.booleanValue;
        }

        public float floatValue() {
            return this.floatValue;
        }
    }
}
