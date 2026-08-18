
























package ac.ghost.anticheat.util;






public final class GhostTrigMath {
    private static final double TWO_PI = 2.0D * Math.PI;
    private static final int SIN_BITS = 22;
    private static final int SIN_SIZE = 1 << SIN_BITS;
    private static final int SIN_MASK = SIN_SIZE - 1;
    private static final float[] SIN_TABLE = new float[SIN_SIZE];
    private static final double SIN_CONVERSION_FACTOR = SIN_SIZE / TWO_PI;
    private static final int COS_OFFSET = SIN_SIZE / 4;

    static {
        for (int i = 0; i < SIN_SIZE; i++) {
            SIN_TABLE[i] = (float) Math.sin((i * TWO_PI) / SIN_SIZE);
        }
    }

    private GhostTrigMath() {
    }

    public static float sin(final double angle) {
        return SIN_TABLE[floor(angle * SIN_CONVERSION_FACTOR) & SIN_MASK];
    }

    public static float cos(final double angle) {
        return SIN_TABLE[(floor(angle * SIN_CONVERSION_FACTOR) + COS_OFFSET) & SIN_MASK];
    }

    
    private static int floor(final double value) {
        final int truncated = (int) value;
        return value < truncated ? truncated - 1 : truncated;
    }
}
