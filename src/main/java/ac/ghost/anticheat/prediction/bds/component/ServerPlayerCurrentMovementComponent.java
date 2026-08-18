package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.data.input.PredictionData;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Vec3;


public final class ServerPlayerCurrentMovementComponent {
    private Vec3 position = Vec3.ZERO.clone();
    private Vec3 velocity = Vec3.ZERO.clone();
    private Vec3 lastTickFinalVelocity = Vec3.ZERO.clone();
    private Vec3 unvalidatedPosition = Vec3.ZERO.clone();
    private Vec3 previousUnvalidatedPosition = Vec3.ZERO.clone();
    private Vec3 unvalidatedTickEnd = Vec3.ZERO.clone();
    private Vec3 previousUnvalidatedTickEnd = Vec3.ZERO.clone();
    private Vec3 beforeCollision = Vec3.ZERO.clone();
    private Vec3 afterCollision = Vec3.ZERO.clone();
    private boolean onGround;
    private boolean horizontalCollision;
    private boolean verticalCollision;
    private PredictionData predictionResult =
            new PredictionData(Vec3.ZERO, Vec3.ZERO, Vec3.ZERO);

    public void capture(final GhostPlayer player) {
        this.position = player.entityContext.stateVectorComponent.getPosition().clone();
        this.velocity = player.entityContext.stateVectorComponent.getDelta().clone();
        this.onGround = player.entityContext.onGroundFlagComponent.isPresent();
        this.horizontalCollision = player.entityContext.horizontalCollisionFlagComponent.isPresent();
        this.verticalCollision = player.entityContext.verticalCollisionFlagComponent.isPresent();
    }

    public Vec3 getPosition() { return position.clone(); }
    public Vec3 getVelocity() { return velocity.clone(); }
    public Vec3 getLastTickFinalVelocity() { return lastTickFinalVelocity.clone(); }
    public void setLastTickFinalVelocity(final Vec3 value) { lastTickFinalVelocity = copy(value); }
    public Vec3 getUnvalidatedPosition() { return unvalidatedPosition.clone(); }
    public void setUnvalidatedPosition(final Vec3 value) { unvalidatedPosition = copy(value); }
    public Vec3 getPreviousUnvalidatedPosition() { return previousUnvalidatedPosition.clone(); }
    public void setPreviousUnvalidatedPosition(final Vec3 value) { previousUnvalidatedPosition = copy(value); }
    public Vec3 getUnvalidatedTickEnd() { return unvalidatedTickEnd.clone(); }
    public void setUnvalidatedTickEnd(final Vec3 value) { unvalidatedTickEnd = copy(value); }
    public Vec3 getPreviousUnvalidatedTickEnd() { return previousUnvalidatedTickEnd.clone(); }
    public void setPreviousUnvalidatedTickEnd(final Vec3 value) {
        previousUnvalidatedTickEnd = copy(value);
    }
    public Vec3 getBeforeCollision() { return beforeCollision.clone(); }
    public void setBeforeCollision(final Vec3 value) { beforeCollision = copy(value); }
    public Vec3 getAfterCollision() { return afterCollision.clone(); }
    public void setAfterCollision(final Vec3 value) { afterCollision = copy(value); }
    public boolean isOnGround() { return onGround; }
    public boolean isHorizontalCollision() { return horizontalCollision; }
    public boolean isVerticalCollision() { return verticalCollision; }
    public PredictionData getPredictionResult() { return predictionResult; }
    public void setPredictionResult(final PredictionData value) {
        predictionResult = value == null
                ? new PredictionData(Vec3.ZERO, Vec3.ZERO, Vec3.ZERO)
                : value;
    }

    private static Vec3 copy(final Vec3 value) {
        return value == null ? Vec3.ZERO.clone() : value.clone();
    }
}
