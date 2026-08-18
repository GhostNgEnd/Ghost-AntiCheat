package ac.ghost.anticheat.prediction.bds.component;

import ac.ghost.anticheat.data.input.PredictionData;
import ac.ghost.anticheat.util.math.Vec3;







public record PredictedMovementComponent(
        long inputTick,
        Vec3 position,
        Vec3 previousPosition,
        Vec3 delta,
        boolean onGround,
        boolean horizontalCollision,
        boolean verticalCollision,
        float vehiclePitch,
        float vehicleYaw,
        boolean vehicle,
        float packetYOffset,
        PredictionData predictionData) {

    public PredictedMovementComponent {
        position = position.clone();
        previousPosition = previousPosition.clone();
        delta = delta.clone();
    }

    @Override
    public Vec3 position() {
        return position.clone();
    }

    @Override
    public Vec3 previousPosition() {
        return previousPosition.clone();
    }

    @Override
    public Vec3 delta() {
        return delta.clone();
    }

    public PredictedMovementComponent withPosition(
            final Vec3 acceptedPosition) {
        return new PredictedMovementComponent(
                inputTick, acceptedPosition.clone(), previousPosition, delta,
                onGround, horizontalCollision, verticalCollision,
                vehiclePitch, vehicleYaw, vehicle, packetYOffset,
                predictionData);
    }
}
