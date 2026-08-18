package ac.ghost.anticheat.prediction.bds.system.block.BlockPosNotificationSystem;

import ac.ghost.anticheat.player.GhostPlayer;


public final class SpeedAlteringBlockStandOn {
    private static final float VERTICAL_SPEED_THRESHOLD =
            Float.intBitsToFloat(0x3DCCCCCD); 
    private static final float VERTICAL_SPEED_SCALE =
            Float.intBitsToFloat(0x3E4CCCCD); 
    private static final float BASE_HORIZONTAL_SCALE =
            Float.intBitsToFloat(0x3ECCCCCD); 

    private SpeedAlteringBlockStandOn() {
    }

    public static void tick(final GhostPlayer entity) {
        if (!entity.entityContext.standOnSpeedAlteringBlockFlagComponent.isPresent()) {
            return;
        }

        final float verticalSpeed = entity.entityContext.stateVectorComponent.getDelta().y;
        if (verticalSpeed >= VERTICAL_SPEED_THRESHOLD) {
            return;
        }

        final float horizontalScale = BASE_HORIZONTAL_SCALE
                + Math.abs(verticalSpeed) * VERTICAL_SPEED_SCALE;

        entity.ghostMovementBridgeState.debugStandSpeedAlteringApplied = true;
        entity.ghostMovementBridgeState.debugStandSpeedVerticalSpeed = verticalSpeed;
        entity.ghostMovementBridgeState.debugStandSpeedHorizontalScale = horizontalScale;
        entity.entityContext.stateVectorComponent.setDelta(
                entity.entityContext.stateVectorComponent.getDelta().multiply(
                        horizontalScale, 1.0F, horizontalScale));
    }
}
