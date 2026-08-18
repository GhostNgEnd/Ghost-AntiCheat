package ac.ghost.anticheat.prediction.bds.math;









public final class BdsMovementMath {
    public static final float INPUT_EPSILON_SQUARED = Float.intBitsToFloat(0x38D1B717);
    public static final float DEGREES_TO_RADIANS = Float.intBitsToFloat(0x3C8EFA35);
    public static final float ONE = Float.intBitsToFloat(0x3F800000);
    public static final float FLOAT_EPSILON = Float.intBitsToFloat(0x34000000);

    private BdsMovementMath() {
    }

    




    public static float sqrtf(final float value) {
        if (Float.isNaN(value) || value < 0.0F) {
            return Float.NaN;
        }
        if (value == 0.0F || value == Float.POSITIVE_INFINITY) {
            return value;
        }

        float candidate = (float) StrictMath.sqrt((double) value);
        final double exactInput = (double) value;

        
        
        while (true) {
            final float lower = Math.nextDown(candidate);
            final double lowerMidpoint = ((double) lower + (double) candidate) * 0.5D;
            final double lowerBoundary = lowerMidpoint * lowerMidpoint;
            final boolean candidateIsOdd = (Float.floatToRawIntBits(candidate) & 1) != 0;
            if (exactInput < lowerBoundary
                    || exactInput == lowerBoundary && candidateIsOdd) {
                candidate = lower;
                continue;
            }

            final float upper = Math.nextUp(candidate);
            final double upperMidpoint = ((double) candidate + (double) upper) * 0.5D;
            final double upperBoundary = upperMidpoint * upperMidpoint;
            if (exactInput > upperBoundary
                    || exactInput == upperBoundary && candidateIsOdd) {
                candidate = upper;
                continue;
            }
            return candidate;
        }
    }

    



    public static float maxss(final float destination, final float source) {
        if (Float.isNaN(destination) || Float.isNaN(source)) {
            return source;
        }
        return destination > source ? destination : source;
    }

    



    public static float minss(final float destination, final float source) {
        if (Float.isNaN(destination) || Float.isNaN(source)) {
            return source;
        }
        return destination < source ? destination : source;
    }

    





    public static float sinf(final float value) {
        return (float) Math.sin(value);
    }

    
    public static float cosf(final float value) {
        return (float) Math.cos(value);
    }
}
