package ac.ghost.anticheat.prediction.bds.math;





public final class BdsTrigMath {
    private static final int TABLE_BITS = 16;
    private static final int TABLE_SIZE = 1 << TABLE_BITS;
    private static final int TABLE_MASK = TABLE_SIZE - 1;
    private static final int COS_OFFSET = TABLE_SIZE / 4;
    private static final float RADIANS_TO_INDEX = 10430.3779296875F;
    private static final float[] SIN_TABLE = new float[TABLE_SIZE];

    static {
        for (int index = 0; index < TABLE_SIZE; index++) {
            SIN_TABLE[index] = (float) Math.sin(
                    index * Math.PI * 2.0D / TABLE_SIZE);
        }
    }

    private BdsTrigMath() {
    }

    public static float sin(final float radians) {
        return SIN_TABLE[((int) (radians * RADIANS_TO_INDEX)) & TABLE_MASK];
    }

    public static float cos(final float radians) {
        return SIN_TABLE[((int) (radians * RADIANS_TO_INDEX + COS_OFFSET))
                & TABLE_MASK];
    }
}
