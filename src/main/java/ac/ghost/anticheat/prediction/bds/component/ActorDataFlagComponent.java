package ac.ghost.anticheat.prediction.bds.component;

import java.util.HashSet;
import java.util.Set;


public final class ActorDataFlagComponent {
    public static final int WORD_COUNT = 3;
    private final long[] words = new long[WORD_COUNT];

    public boolean has(final int flag) {
        if (flag < 0 || flag >= WORD_COUNT * Long.SIZE) {
            return false;
        }
        return (this.words[flag >>> 6] & (1L << (flag & 63))) != 0L;
    }

    public void set(final int flag, final boolean value) {
        if (flag < 0 || flag >= WORD_COUNT * Long.SIZE) {
            throw new IllegalArgumentException("ActorData flag must be in [0, 191]: " + flag);
        }
        final int word = flag >>> 6;
        final long bit = 1L << (flag & 63);
        if (value) {
            this.words[word] |= bit;
        } else {
            this.words[word] &= ~bit;
        }
    }

    public long getWord(final int index) {
        checkWord(index);
        return this.words[index];
    }

    public void setWord(final int index, final long value) {
        checkWord(index);
        this.words[index] = value;
    }

    public long[] copyWords() {
        return this.words.clone();
    }

    public void replaceWords(final long word0, final long word1, final long word2) {
        this.words[0] = word0;
        this.words[1] = word1;
        this.words[2] = word2;
    }

    public void applyMasked(final long desired0, final long desired1,
                            final long desired2, final long mask0,
                            final long mask1, final long mask2) {
        this.words[0] ^= (this.words[0] ^ desired0) & mask0;
        this.words[1] ^= (this.words[1] ^ desired1) & mask1;
        this.words[2] ^= (this.words[2] ^ desired2) & mask2;
    }

    public void clear() {
        this.words[0] = 0L;
        this.words[1] = 0L;
        this.words[2] = 0L;
    }

    public void replace(final Set<Integer> values) {
        clear();
        if (values == null) {
            return;
        }
        for (final Integer value : values) {
            if (value != null && value >= 0 && value < WORD_COUNT * Long.SIZE) {
                set(value, true);
            }
        }
    }

    public Set<Integer> copyFlags() {
        final Set<Integer> result = new HashSet<>();
        for (int word = 0; word < WORD_COUNT; word++) {
            long value = this.words[word];
            while (value != 0L) {
                final int bit = Long.numberOfTrailingZeros(value);
                result.add(word * Long.SIZE + bit);
                value &= value - 1L;
            }
        }
        return result;
    }

    private static void checkWord(final int index) {
        if (index < 0 || index >= WORD_COUNT) {
            throw new IndexOutOfBoundsException("ActorData word: " + index);
        }
    }
}
