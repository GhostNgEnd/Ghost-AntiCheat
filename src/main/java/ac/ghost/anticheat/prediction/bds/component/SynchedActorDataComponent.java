package ac.ghost.anticheat.prediction.bds.component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;







public final class SynchedActorDataComponent {
    private Set<Integer> flags = Collections.emptySet();

    public void setFlags(final Set<Integer> flags) {
        this.flags = flags == null
                ? Collections.emptySet()
                : Collections.unmodifiableSet(new HashSet<>(flags));
    }

    public boolean has(final int flag) {
        return this.flags.contains(flag);
    }

    public boolean hasFlag(final int flag) {
        return has(flag);
    }

    public void setFlag(final int flag, final boolean value) {
        final Set<Integer> next = new HashSet<>(this.flags);
        if (value) {
            next.add(flag);
        } else {
            next.remove(flag);
        }
        this.flags = Collections.unmodifiableSet(next);
    }

    public Set<Integer> getFlags() {
        return this.flags;
    }

    public void clear() {
        this.flags = Collections.emptySet();
    }
}
