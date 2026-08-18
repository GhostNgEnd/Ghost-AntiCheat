package ac.ghost.anticheat.prediction.bds.system.movement;

import ac.ghost.anticheat.prediction.nukkit.NukkitEntityPositionAdapter;
import ac.ghost.anticheat.collision.bds.system.BlockCollisionsSystem;
import ac.ghost.anticheat.collision.bds.system.BlockCollisionsAddActorSetPosRequestSystem;
import ac.ghost.anticheat.collision.bds.system.RemoveBlockCollisionResolutionVectorSystem;
import ac.ghost.anticheat.collision.bds.system.ActorSetPosSystem;
import ac.ghost.anticheat.collision.bds.system.MoveTowardsClosestSpaceSystemFromActor;
import ac.ghost.anticheat.data.input.PredictionData;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.port.nukkit.NukkitPistonRequestConsumer;
import ac.ghost.anticheat.prediction.bds.component.PredictedMovementComponent;
import ac.ghost.anticheat.prediction.bds.component.PlayerTickStartVelocityComponent;
import ac.ghost.anticheat.prediction.bds.component.ServerPlayerMovementComponent;
import ac.ghost.anticheat.prediction.bds.system.block.BlockClimberSystem;
import ac.ghost.anticheat.prediction.bds.system.block.BlockMovementSlowdownMultiplierSystem;
import ac.ghost.anticheat.prediction.bds.system.block.BlockPosNotificationSystem.Cleanup;
import ac.ghost.anticheat.prediction.bds.system.block.EntityInsideSystem;
import ac.ghost.anticheat.prediction.bds.system.block.InsideHoneyBlockSystem;
import ac.ghost.anticheat.prediction.bds.system.block.InsidePowderSnowBlockSystem;
import ac.ghost.anticheat.prediction.bds.system.block.ScaffoldingActionSystem;
import ac.ghost.anticheat.prediction.bds.system.block.ScaffoldingIntentSystem;
import ac.ghost.anticheat.prediction.bds.system.block.SweetBerryBushMovementSlowdownSystem;
import ac.ghost.anticheat.prediction.bds.system.glide.GlideInputSystem;
import ac.ghost.anticheat.prediction.bds.system.input.JumpInputSystem;
import ac.ghost.anticheat.prediction.bds.system.item.ItemUseSlowdownClearSystem;
import ac.ghost.anticheat.prediction.bds.system.input.PlayerInputFilterServerSystem;
import ac.ghost.anticheat.prediction.bds.system.liquid.common.TravelTypeSensingSystem;
import ac.ghost.anticheat.prediction.bds.system.liquid.common.UpdateWaterStateRequestSystem;
import ac.ghost.anticheat.prediction.bds.system.liquid.water.CurrentSwimAmountSystem;
import ac.ghost.anticheat.prediction.bds.system.liquid.water.SwimTriggerSystem;
import ac.ghost.anticheat.prediction.bds.system.liquid.water.UnderWaterSensingSystem;
import ac.ghost.anticheat.prediction.bds.system.liquid.water.WaterSinkInputSystem;
import ac.ghost.anticheat.prediction.bds.system.restitution.ApplyRestitutionSystem.RemoveApplyRestitutionComponent;
import ac.ghost.anticheat.prediction.bds.system.restitution.RemoveBounceGravityCorrectionComponent;
import ac.ghost.anticheat.prediction.bds.system.spinattack.StartSpinAttackActionSystem;
import ac.ghost.anticheat.prediction.bds.system.travel.DecrementNoJumpDelaySystem;
import ac.ghost.anticheat.prediction.bds.system.travel.FlyingPlayerStuckOnGroundWorkaroundSystem;
import ac.ghost.anticheat.prediction.bds.system.travel.MobJumpSystem;
import ac.ghost.anticheat.prediction.bds.system.travel.MobTravelIntentSystem;
import ac.ghost.anticheat.prediction.bds.system.travel.MobTravelPlayerOrLocalFilterSystem;
import ac.ghost.anticheat.util.math.Vec3;


public final class PredictedMovementSystem {
    private PredictedMovementSystem() {
    }

    
    public static void tick(final GhostPlayer player) {
        player.ghostMovementBridgeState.resetPredictionDebugTrace(
                player.entityContext.serverPlayerMovementComponent.getCurrentInputTick());
        player.ghostMovementBridgeState.debugElasticTrace.tickStartPosition =
                player.entityContext.stateVectorComponent.getPosition().clone();
        player.ghostMovementBridgeState.debugElasticTrace.carriedStartVelocity =
                player.entityContext.stateVectorComponent.getDelta().clone();
        player.ghostMovementBridgeState.debugElasticTrace.onGroundAtTickStart =
                player.entityContext.onGroundFlagComponent.isPresent();
        NukkitPistonRequestConsumer.tick(player);
        final PlayerTickStartVelocityComponent.Candidates velocityCandidates =
                ConfigurePlayerTickStartVelocitySystem.tick(player);
        CurrentSwimAmountSystem.tick(player);
        prepare(player);
        move(player, velocityCandidates);

        player.ghostMovementBridgeState.debugElasticTrace.submittedMovement =
                player.entityContext.moveRequestComponent.movement().clone();
        player.ghostMovementBridgeState.debugElasticTrace.resolvedMovement =
                player.entityContext.moveRequestComponent.resolvedMovement().clone();
        player.ghostMovementBridgeState.debugElasticTrace.originalAabb =
                String.valueOf(player.entityContext.moveRequestComponent.originalAABB());
        player.ghostMovementBridgeState.debugElasticTrace.resolvedAabb =
                String.valueOf(player.entityContext.moveRequestComponent.resolvedAABB());
        player.ghostMovementBridgeState.debugElasticTrace.tickEndPosition =
                player.entityContext.stateVectorComponent.getPosition().clone();
        player.ghostMovementBridgeState.debugElasticTrace.tickEndVelocity =
                player.entityContext.stateVectorComponent.getDelta().clone();
        player.ghostMovementBridgeState.debugElasticTrace.onGroundAtTickEnd =
                player.entityContext.onGroundFlagComponent.isPresent();

        player.entityContext.serverPlayerCurrentMovementComponent.setPredictionResult(new PredictionData(
                player.entityContext.serverPlayerCurrentMovementComponent.getBeforeCollision().clone(),
                player.entityContext.serverPlayerCurrentMovementComponent.getAfterCollision().clone(),
                player.entityContext.stateVectorComponent.getDelta().clone()));
        player.entityContext.serverPlayerCurrentMovementComponent.setLastTickFinalVelocity(player.entityContext.stateVectorComponent.getDelta().clone());

        
        Cleanup.tick(player);
        RemoveApplyRestitutionComponent.tick(player);
        RemoveBounceGravityCorrectionComponent.tick(player);
        ItemUseSlowdownClearSystem.tick(player);
    }

    private static void prepare(final GhostPlayer player) {
        PlayerInputFilterServerSystem.tick(player);
        InsidePowderSnowBlockSystem.updateCanStandOnSnowFlag(player);
        BlockClimberSystem.tick(player);
        ScaffoldingIntentSystem.tick(player);

        player.entityContext.stateVectorComponent.beginTick();
        BlockCollisionsSystem.run(player);
        BlockCollisionsAddActorSetPosRequestSystem.run(player);
        RemoveBlockCollisionResolutionVectorSystem.run(player);
        ActorSetPosSystem.run(player);
        MoveTowardsClosestSpaceSystemFromActor.run(player);
        player.entityContext.wasInWaterFlagComponent.setPresent(
                player.entityContext.waterTravelFlagComponent.isPresent());
        player.entityContext.wasInLavaFlagComponent.setPresent(
                player.entityContext.lavaTravelFlagComponent.isPresent());
        TravelTypeSensingSystem.tick(player);
        UnderWaterSensingSystem.tick(player);

        player.entityContext.playerInputRequestComponent.sense(player);
        SwimTriggerSystem.tick(player);
        GlideInputSystem.tick(player);
        UpdateHorizontalPoseSystem.filterInvalidGlidingState(player.entityContext);
        PlayerBoundingBoxStateUpdateSystem.tick(player.entityContext);
        StartSpinAttackActionSystem.tick(player);
    }

    private static void move(
            final GhostPlayer player,
            final PlayerTickStartVelocityComponent.Candidates velocityCandidates) {
        MovementTickResetTemporaryComponentsSystem.tick(player);
        UpdateWaterStateRequestSystem.tick(player);
        WaterSinkInputSystem.tick(player);
        MobTravelIntentSystem.tick(player);

                ScaffoldingActionSystem.tick(player, player.entityContext.stateVectorComponent);
        FlyingPlayerStuckOnGroundWorkaroundSystem.tick(
                player, player.entityContext.stateVectorComponent);
        JumpInputSystem.tick(player);
        DecrementNoJumpDelaySystem.tick(player);
        player.entityContext.stateVectorComponent.setDelta(player.entityContext.stateVectorComponent.getDelta().clone());
        MobJumpSystem.tick(player.entityContext);

        UpdateHorizontalPoseSystem.tick(player.entityContext);
        PlayerBoundingBoxStateUpdateSystem.tick(player.entityContext);

        SelectPlayerTickStartVelocitySystem.tick(player, velocityCandidates);
        player.ghostMovementBridgeState.debugElasticTrace.selectedStartVelocity =
                player.entityContext.playerTickStartVelocityComponent
                        .selectedVelocity();
        player.ghostMovementBridgeState.debugElasticTrace.onGroundBeforeTravel =
                player.entityContext.onGroundFlagComponent.isPresent();

        final PlayerPreMobTravelStorePositionSystem.Snapshot beforeTravel =
                PlayerPreMobTravelStorePositionSystem.tick(player);
        MobTravelPlayerOrLocalFilterSystem.tick(player);

        EntityInsideSystem.tickSetEntityInside(player);

        if (!player.entityContext.insideBubbleColumnBlockComponent.isEmpty()) {
            player.entityContext.insideBubbleColumnBlockComponent.applyTo(
                    player.entityContext.stateVectorComponent);
            player.entityContext.stateVectorComponent.setDelta(
                    player.entityContext.stateVectorComponent.getDelta().clone());
        }

        if (!player.entityContext.insideHoneyBlockComponent.isEmpty()) {
                                                InsideHoneyBlockSystem.fireEventsSystem(
                    player.entityContext.insideHoneyBlockComponent,
                    player.entityContext.aabbShapeComponent,
                    player.entityContext.stateVectorComponent,
                    player.entityContext.fallDistanceComponent);
            player.entityContext.stateVectorComponent.setDelta(player.entityContext.stateVectorComponent.getDelta().clone());
                    }

        SweetBerryBushMovementSlowdownSystem.tick(player);
        InsidePowderSnowBlockSystem.applyMovementSlowdown(player);
        EntityInsideSystem.applyWebMovementSlowdown(player);
        BlockMovementSlowdownMultiplierSystem.resistantMob(player);
        BlockMovementSlowdownMultiplierSystem.immunePlayer(player);

        ActorPostAiStepTickSystem.tick(player, beforeTravel);
        BlockClimberSystem.tick(player);

                player.entityContext.serverPlayerCurrentMovementComponent.capture(player);
    }

    
    public static PredictedMovementComponent tick(final GhostPlayer player,
                                                  final long inputTick) {
        final PredictedMovementComponent snapshot = new PredictedMovementComponent(
                inputTick,
                player.entityContext.stateVectorComponent.getPosition().clone(),
                player.entityContext.stateVectorComponent.getPreviousPosition().clone(),
                player.entityContext.stateVectorComponent.getDelta().clone(),
                player.entityContext.serverPlayerCurrentMovementComponent.isOnGround(),
                player.entityContext.serverPlayerCurrentMovementComponent.isHorizontalCollision(),
                player.entityContext.serverPlayerCurrentMovementComponent.isVerticalCollision(),
                player.entityContext.actorRotationComponent.getPitch(),
                player.entityContext.actorRotationComponent.getYaw(),
                player.entityContext.vehicleComponent.value != null,
                NukkitEntityPositionAdapter.getYOffset(player),
                player.entityContext.serverPlayerCurrentMovementComponent.getPredictionResult());

        final ServerPlayerMovementComponent.HistoryRecord processing =
                player.entityContext.serverPlayerMovementComponent.processingRecord();
        final ServerPlayerMovementComponent.HistoryRecord record =
                processing != null && processing.inputTick() == inputTick
                        ? processing
                        : player.entityContext.serverPlayerMovementComponent.find(inputTick);
        if (record != null) {
            record.setAuthoritativeSnapshot(snapshot);
        }
        return snapshot;
    }

}
