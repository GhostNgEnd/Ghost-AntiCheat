package ac.ghost.anticheat.prediction.bds.component;


public final class ActorRotationComponent {
    private float pitch;
    private float yaw;
    private float headYaw;
    private float previousPitch;
    private float previousYaw;
    private boolean initialized;

    public void initialize(final float pitch, final float yaw, final float headYaw) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.headYaw = headYaw;
        this.previousPitch = pitch;
        this.previousYaw = yaw;
        this.initialized = true;
    }

    public void set(final float pitch, final float yaw, final float headYaw) {
        if (!this.initialized) {
            initialize(pitch, yaw, headYaw);
            return;
        }
        this.previousPitch = this.pitch;
        this.previousYaw = this.yaw;
        this.pitch = pitch;
        this.yaw = yaw;
        this.headYaw = headYaw;
    }

    
    public void snap(final float pitch, final float yaw, final float headYaw) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.headYaw = headYaw;
        this.previousPitch = pitch;
        this.previousYaw = yaw;
        this.initialized = true;
    }

    public float getPitch() { return pitch; }
    public float getYaw() { return yaw; }
    public float getHeadYaw() { return headYaw; }
    public float getPreviousPitch() { return previousPitch; }
    public float getPreviousYaw() { return previousYaw; }
}
