package ac.ghost.anticheat.prediction.nukkit.data;

import cn.nukkit.math.Vector2f;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.types.AuthInputAction;
import cn.nukkit.network.protocol.types.InputMode;

import java.util.HashSet;
import java.util.Set;









public final class ReplayableActorInput {
    private final long inputTick;
    private final Vector3f position;
    private final Vector3f positionDelta;
    private final float pitch;
    private final float yaw;
    private final float headYaw;
    private final Vector2f rawMoveVector;
    private final Vector2f interactRotation;
    private final InputMode inputMode;
    private final Set<AuthInputAction> inputData;
    private final long predictedVehicle;

    private ReplayableActorInput(final long inputTick,
                                 final Vector3f position,
                                 final Vector3f positionDelta,
                                 final float pitch,
                                 final float yaw,
                                 final float headYaw,
                                 final Vector2f rawMoveVector,
                                 final Vector2f interactRotation,
                                 final InputMode inputMode,
                                 final Set<AuthInputAction> inputData,
                                 final long predictedVehicle) {
        this.inputTick = inputTick;
        this.position = position;
        this.positionDelta = positionDelta;
        this.pitch = pitch;
        this.yaw = yaw;
        this.headYaw = headYaw;
        this.rawMoveVector = rawMoveVector;
        this.interactRotation = interactRotation;
        this.inputMode = inputMode;
        this.inputData = inputData;
        this.predictedVehicle = predictedVehicle;
    }

    public static ReplayableActorInput capture(final PlayerAuthInputPacket source) {
        final Vector3f position = source.getPosition();
        final Vector3f delta = source.getDelta();
        return new ReplayableActorInput(
                source.getTick(),
                copy(position),
                copy(delta),
                source.getPitch(),
                source.getYaw(),
                source.getHeadYaw(),
                copy(source.getRawMoveVector()),
                copy(source.getInteractRotation()),
                source.getInputMode(),
                new HashSet<>(source.getInputData()),
                source.getPredictedVehicle()
        );
    }

    
    public PlayerAuthInputPacket packet() {
        final PlayerAuthInputPacket packet = new PlayerAuthInputPacket();
        packet.setTick(this.inputTick);
        packet.setPosition(copy(this.position));
        packet.setDelta(copy(this.positionDelta));
        packet.setPitch(this.pitch);
        packet.setYaw(this.yaw);
        packet.setHeadYaw(this.headYaw);
        if (this.rawMoveVector != null) {
            packet.setRawMoveVector(copy(this.rawMoveVector));
        }
        if (this.interactRotation != null) {
            packet.setInteractRotation(copy(this.interactRotation));
        }
        packet.setInputMode(this.inputMode);
        packet.setInputData(new HashSet<>(this.inputData));
        packet.setPredictedVehicle(this.predictedVehicle);
        return packet;
    }

    public long inputTick() {
        return inputTick;
    }

    public long predictedVehicle() {
        return predictedVehicle;
    }

    public Set<AuthInputAction> inputData() {
        return new HashSet<>(inputData);
    }

    public void writeInputDataTo(final PlayerAuthInputPacket target,
                                 final Set<AuthInputAction> actions) {
        target.setInputData(new HashSet<>(actions));
    }

    private static Vector3f copy(final Vector3f value) {
        return value == null ? null
                : new Vector3f(value.getX(), value.getY(), value.getZ());
    }

    private static Vector2f copy(final Vector2f value) {
        return value == null ? null : new Vector2f(value.getX(), value.getY());
    }
}
