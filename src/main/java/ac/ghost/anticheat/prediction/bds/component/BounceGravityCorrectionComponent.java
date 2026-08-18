package ac.ghost.anticheat.prediction.bds.component;





public final class BounceGravityCorrectionComponent {
    private final float requestedY;
    private final float resolvedY;

    public BounceGravityCorrectionComponent(final float requestedY,
                                            final float resolvedY) {
        this.requestedY = requestedY;
        this.resolvedY = resolvedY;
    }

    public float requestedY() {
        return requestedY;
    }

    public float resolvedY() {
        return resolvedY;
    }
}
