package ac.ghost.anticheat.prediction.bds.system.input;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.ActorDataFlag;
import ac.ghost.anticheat.prediction.bds.component.MoveInputComponent;
import ac.ghost.anticheat.prediction.bds.component.PlayerActionComponent;
import ac.ghost.anticheat.util.MathUtil;









public final class SneakingSystem {
    private static final int BDS_DIRECT_SLOW_MASK =
            MoveInputComponent.SNEAK_DOWN | MoveInputComponent.DESCEND;
    private static final float BASE_SNEAK_SCALE = 0.3F;
    private static final int SWIFT_SNEAK_BASE_TICKS = 2;

    private SneakingSystem() {
    }

    public static void tick(final GhostPlayer player) {
        final MoveInputComponent input = player.entityContext.moveInputComponent;
        final boolean directSlowInput =
                (input.getFlags() & BDS_DIRECT_SLOW_MASK) != 0;
        final boolean startSneaking = player.entityContext.playerActionComponent
                .has(PlayerActionComponent.START_SNEAKING);
        final boolean stopSneaking = player.entityContext.playerActionComponent
                .has(PlayerActionComponent.STOP_SNEAKING);

        
        
        
        
        final boolean actorSneaking = player.entityContext.actorDataFlagComponent
                .has(ActorDataFlag.SNEAKING);
        final boolean slowInput = directSlowInput
                || startSneaking
                || stopSneaking
                || actorSneaking;

        if (!slowInput
                || player.entityContext.movementAbilitiesComponent.isFlying()
                || player.ghostMovementBridgeState.waterSample.touching()) {
            player.ghostMovementBridgeState.ticksSinceCanSlowdown = 0;
            player.entityContext.sneakingComponent.setMovementScale(BASE_SNEAK_SCALE);
            return;
        }

        player.ghostMovementBridgeState.ticksSinceCanSlowdown++;

        
        
        float scale = BASE_SNEAK_SCALE;
        if (player.ghostMovementBridgeState.ticksSinceCanSlowdown > SWIFT_SNEAK_BASE_TICKS) {
            scale += player.entityContext.swiftSneakEnchantComponent.getMovementScaleModifier();
        }
        scale = MathUtil.clamp(scale, 0.0F, 1.0F);
        player.entityContext.sneakingComponent.setMovementScale(scale);
    }
}
