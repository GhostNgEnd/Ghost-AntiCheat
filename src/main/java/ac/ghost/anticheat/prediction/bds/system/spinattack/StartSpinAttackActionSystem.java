package ac.ghost.anticheat.prediction.bds.system.spinattack;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.math.BdsMovementMath;
import ac.ghost.anticheat.prediction.bds.system.movement.UpdateHorizontalPoseSystem;
import ac.ghost.anticheat.prediction.bds.system.movement.PlayerBoundingBoxStateUpdateSystem;
import ac.ghost.anticheat.prediction.bds.math.BdsTrigMath;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.entity.Entity;




public final class StartSpinAttackActionSystem {
    private static final float NORMALIZATION_EPSILON =
            Float.intBitsToFloat(0x38D1B717); 
    private static final float QUARTER = Float.intBitsToFloat(0x3E800000);
    private static final float THREE = Float.intBitsToFloat(0x40400000);
    private static final float GROUND_GRAVITY = Float.intBitsToFloat(0x3DA3D70A);
    private static final float WATER_DRAG = Float.intBitsToFloat(0x3F4CCCCD);
    private static final float GROUND_DRAG = Float.intBitsToFloat(0x3F7AE148);

    private StartSpinAttackActionSystem() {
    }

    public static void tick(final GhostPlayer player) {
        
        
        
        
        
        if (!player.entityContext.riptideTridentSpinAttackComponent.isPresent()) {
            return;
        }

        final int riptideLevel =
                player.entityContext.riptideTridentSpinAttackComponent.consumeRiptideLevel();

        player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_SPIN_ATTACK, true);
        player.entityContext.shouldUpdateBoundingBoxRequestComponent.request();
        if (player.entityContext.shouldUpdateBoundingBoxRequestComponent.consume()) {
            UpdateHorizontalPoseSystem.tick(player.entityContext);
            PlayerBoundingBoxStateUpdateSystem.tick(player.entityContext);
        }

        final float pitchRadians = player.entityContext.actorRotationComponent.getPitch()
                * BdsMovementMath.DEGREES_TO_RADIANS;
        final float yawRadians = player.entityContext.actorRotationComponent.getYaw()
                * BdsMovementMath.DEGREES_TO_RADIANS;

        
        
        final float negativeSinYaw = -BdsTrigMath.sin(yawRadians);
        final float cosPitch = BdsTrigMath.cos(pitchRadians);
        float impulseX = negativeSinYaw * cosPitch;
        final float sinPitch = BdsTrigMath.sin(pitchRadians);
        float impulseZ = cosPitch * BdsTrigMath.cos(yawRadians);

        final float xSquared = impulseX * impulseX;
        final float ySquared = sinPitch * sinPitch;
        final float xySquared = ySquared + xSquared;
        final float zSquared = impulseZ * impulseZ;
        final float lengthSquared = zSquared + xySquared;
        final float length = BdsMovementMath.sqrtf(lengthSquared);

        float impulseY;
        if (length < NORMALIZATION_EPSILON) {
            impulseX = 0.0F;
            impulseY = 0.0F;
            impulseZ = 0.0F;
        } else {
            impulseX = impulseX / length;
            impulseY = -sinPitch;
            impulseY = impulseY / length;
            impulseZ = impulseZ / length;
        }

        float magnitude = (float) riptideLevel;
        magnitude = magnitude + BdsMovementMath.ONE;
        magnitude = magnitude * QUARTER;
        magnitude = magnitude * THREE;

        impulseX = impulseX * magnitude;
        impulseY = impulseY * magnitude;
        impulseZ = impulseZ * magnitude;

        
        
        
        final boolean onGround = player.entityContext.onGroundFlagComponent.isPresent();
        final boolean wasInWater = player.entityContext.wasInWaterFlagComponent.isPresent();
        final boolean headInWater = player.entityContext.actorHeadInWaterFlagComponent.isPresent();
        if (onGround) {
            final boolean leavingWaterAtHead = wasInWater && !headInWater;
            if (!leavingWaterAtHead) {
                impulseY = impulseY + GROUND_GRAVITY;
            } else {
                impulseY = impulseY / WATER_DRAG;
                impulseY = impulseY * GROUND_DRAG;
            }
        }

        final Vec3 velocity = player.entityContext.stateVectorComponent.getDelta();
        final float velocityX = impulseX + velocity.x;
        final float velocityY = impulseY + velocity.y;
        final float velocityZ = impulseZ + velocity.z;
        player.entityContext.stateVectorComponent.setDelta(new Vec3(velocityX, velocityY, velocityZ));


        player.entityContext.riptideTridentSpinAttackComponent.setRemainingTicks(20);
        player.entityContext.riptideTridentSpinAttackComponent.setStartedThisTick(true);
        player.entityContext.riptideTridentSpinAttackComponent.setStartedOnGroundThisTick(
                player.entityContext.onGroundFlagComponent.isPresent());
    }
}
