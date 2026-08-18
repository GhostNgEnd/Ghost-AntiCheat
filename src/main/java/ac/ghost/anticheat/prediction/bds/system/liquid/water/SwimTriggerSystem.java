package ac.ghost.anticheat.prediction.bds.system.liquid.water;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.NukkitAdapter;
import ac.ghost.anticheat.prediction.bds.component.PlayerInputRequestComponent;
import ac.ghost.anticheat.prediction.bds.system.movement.UpdateHorizontalPoseSystem;
import ac.ghost.anticheat.prediction.bds.system.movement.PlayerBoundingBoxStateUpdateSystem;
import ac.ghost.anticheat.util.MathUtil;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.entity.Entity;
import cn.nukkit.network.protocol.types.AuthInputAction;


public final class SwimTriggerSystem {
    private SwimTriggerSystem() {
    }

    public static void tick(final GhostPlayer player) {
        final boolean startRequested =
                NukkitAdapter.hasInput(player, AuthInputAction.START_SWIMMING);
        final PlayerInputRequestComponent request =
                player.entityContext.playerInputRequestComponent;
        final Vec3 view = MathUtil.getRotationVector(
                player.entityContext.actorRotationComponent.getPitch(),
                player.entityContext.actorRotationComponent.getYaw());

        player.ghostMovementBridgeState.debugSwimStartRequested = startRequested;

        if (startRequested && !player.ghostMovementBridgeState.wasPredictionSwimming) {
            final boolean startGliding =
                    NukkitAdapter.hasInput(player, AuthInputAction.START_GLIDING);
            final boolean accepted = player.entityContext.actorHeadInWaterFlagComponent.isPresent()
                    && !player.entityContext.movementAbilitiesComponent.isFlying()
                    && player.entityContext.vehicleComponent.value == null
                    && !startGliding
                    && (!request.isBreathingInAir() && player.ghostMovementBridgeState.waterSample.touching()
                    || view.y < 0.15F);
            player.ghostMovementBridgeState.debugSwimStartAccepted = accepted;
            if (!accepted) {
                setSwimmingState(player, false);
            }
            
            
            return;
        }

        if (!player.entityContext.actorDataFlagComponent.has(Entity.DATA_FLAG_SWIMMING)) {
            return;
        }

        
        
        
        
        boolean stop = NukkitAdapter.hasInput(player, AuthInputAction.START_GLIDING)
                || NukkitAdapter.hasInput(player, AuthInputAction.STOP_SWIMMING);

        if (!stop && player.entityContext.wasInWaterFlagComponent.isPresent()
                && request.isBreathingInAir() && view.y < 0.0F) {
            final float horizontalSquared = view.x * view.x + view.z * view.z;
            final float downAngle = (float) Math.toDegrees(
                    Math.acos(MathUtil.clamp(horizontalSquared, 0.0F, 1.0F)));
            stop = downAngle > 45.0F;
        }

        if (stop || player.entityContext.vehicleComponent.value != null) {
            player.ghostMovementBridgeState.debugSwimStopTriggered = true;
            setSwimmingState(player, false);
        }
    }

    private static void setSwimmingState(final GhostPlayer player,
                                         final boolean swimming) {
        player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_SWIMMING, swimming);
        UpdateHorizontalPoseSystem.tick(player.entityContext);
        PlayerBoundingBoxStateUpdateSystem.tick(player.entityContext);
    }
}
