package ac.ghost.anticheat.prediction.bds.system.travel;

import ac.ghost.anticheat.collision.bds.system.ActorSetPosSystem;
import ac.ghost.anticheat.collision.bds.system.ActorMoveSystem;
import ac.ghost.anticheat.collision.bds.system.AutoStepFilterSystem;
import ac.ghost.anticheat.collision.bds.system.AutoStepSystem;
import ac.ghost.anticheat.collision.bds.system.CollisionShapesCopySystem;
import ac.ghost.anticheat.collision.bds.system.ConfigureDepenetration;
import ac.ghost.anticheat.collision.bds.system.CopyCollisionShapesRewindSystem;
import ac.ghost.anticheat.collision.bds.system.FinalizeMoveSystem;
import ac.ghost.anticheat.collision.bds.system.FlagPlayersForCollisionSystem;
import ac.ghost.anticheat.collision.bds.system.MoveCollisionSystem;
import ac.ghost.anticheat.collision.bds.system.NoClipOrNoBlockMoveFilterSystem;
import ac.ghost.anticheat.collision.bds.system.ServerVariableMaxAutoStepSystem;
import ac.ghost.anticheat.collision.bds.system.SneakMovementSystem;
import ac.ghost.anticheat.collision.bds.system.UpdateDepenetration;
import ac.ghost.anticheat.collision.bds.system.UpdateOnewayCollisionsSystem;
import ac.ghost.anticheat.data.vanilla.StatusEffect;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MoveRequestComponent;
import ac.ghost.anticheat.prediction.bds.system.block.BlockMovementSlowdownMultiplierSystem;
import ac.ghost.anticheat.prediction.bds.system.attribute.UpdateAttributesSystem;
import ac.ghost.anticheat.prediction.bds.system.attribute.PlayerResetMovementSpeedSystem;
import ac.ghost.anticheat.prediction.bds.system.block.BlockPosTrackerResetShouldTriggerStandOnSystem;
import ac.ghost.anticheat.prediction.bds.system.block.BlockPosTrackerSystem;
import ac.ghost.anticheat.prediction.bds.system.block.BlockPosNotificationSystem.Filter;
import ac.ghost.anticheat.prediction.bds.system.block.BlockPosNotificationSystem.GenericOnStandOn;
import ac.ghost.anticheat.prediction.bds.system.block.BlockPosNotificationSystem.SpeedAlteringBlockStandOn;
import ac.ghost.anticheat.prediction.bds.system.block.CurrentlyStandingOnBlockSystem;
import ac.ghost.anticheat.prediction.bds.system.glide.GlideMoveSystem;
import ac.ghost.anticheat.prediction.bds.system.liquid.common.ClimbOutOfLiquidSystem;
import ac.ghost.anticheat.prediction.bds.system.liquid.lava.LavaResetFallDistanceSystem;
import ac.ghost.anticheat.prediction.bds.system.liquid.lava.LavaTravelSystem;
import ac.ghost.anticheat.prediction.bds.system.liquid.water.SwimControlSystem;
import ac.ghost.anticheat.prediction.bds.system.liquid.water.WaterTravelSystem;
import ac.ghost.anticheat.prediction.bds.system.restitution.ApplyGravityWithBounceCorrection;
import ac.ghost.anticheat.prediction.bds.system.restitution.ApplyRestitutionSystem.ApplyRestitution;
import ac.ghost.anticheat.prediction.bds.system.restitution.ComputeBlockRestitutionSystem;
import ac.ghost.anticheat.prediction.bds.system.restitution.RequestGravityCorrectionSystem;
import ac.ghost.anticheat.prediction.bds.system.restitution.ResetHorizontalVelocitySystem;
import ac.ghost.anticheat.prediction.bds.system.restitution.ResetVerticalVelocitySystem;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.entity.Entity;
import cn.nukkit.potion.Effect;


public final class MobTravelPlayerOrLocalFilterSystem {
    private static final float FLOAT_EPSILON = 1.1920929E-7F;

    private MobTravelPlayerOrLocalFilterSystem() {
    }

    public static void tick(final GhostPlayer player) {
        UpdateAttributesSystem.tick(player, player.entityContext.movementAttributesComponent);
        PlayerResetMovementSpeedSystem.tick(
                player.entityContext.movementAttributesComponent, player.entityContext.movementSpeedComponent);
        SwimControlSystem.tick(player);
                VerticalFlySpeedControlSystem.tick(player, player.entityContext.stateVectorComponent);
        player.entityContext.stateVectorComponent.setDelta(player.entityContext.stateVectorComponent.getDelta().clone());

        if (player.entityContext.playerFlyingTravelComponent.isPresent()) {
            tickFlyingPlayer(player);
        } else if ((player.entityContext.serverPlayerMovementComponent.getCurrentInputTick() != 1L && player.ghostMovementBridgeState.lavaSample.touching()) || player.ghostMovementBridgeState.waterSample.touching()) {
            tickInFluid(player);
        } else if (player.entityContext.actorDataFlagComponent.has(Entity.DATA_FLAG_GLIDING)) {
            player.entityContext.stateVectorComponent.setDelta(GlideMoveSystem.tick(player, player.entityContext.stateVectorComponent.getDelta()));
            move(player, player.entityContext.stateVectorComponent.getDelta().clone());
        } else {
            tickGroundOrAir(player);
        }

        
        
        
        
        
        runStandOnNotifications(player);
    }

    private static void tickGroundOrAir(final GhostPlayer player) {
                if (player.entityContext.onGroundFlagComponent.isPresent()) {
            GroundTravelTypeSystem.tick(player, player.entityContext.movementSpeedComponent);
        } else {
            MobTravelUpdateSpeedsSystem.tickAir(player, player.entityContext.movementSpeedComponent);
        }
        DefaultMoveSystems.tickGroundOrAir(
                player,
                player.entityContext.stateVectorComponent,
                player.entityContext.movementSpeedComponent,
                player.entityContext.mobTravelComponent);

        player.entityContext.stateVectorComponent.setDelta(player.entityContext.stateVectorComponent.getDelta().clone());
        move(player, player.entityContext.stateVectorComponent.getDelta().clone());

                final StatusEffect levitation = player.entityContext.mobEffectsComponent.get(Effect.LEVITATION);
        if (levitation != null) {
            LevitateSystem.tick(player.entityContext.stateVectorComponent, levitation.getAmplifier());
        } else if (ApplyGravityWithBounceCorrection.shouldApplyGroundOrAir(player)) {
            ApplyGravityWithBounceCorrection.tick(
                    player,
                    player.entityContext.stateVectorComponent,
                    -player.entityContext.mobEffectsComponent.effectiveGravity(player.entityContext.stateVectorComponent.getDelta()));
        }

        if (player.entityContext.autoClimbTravelFlagComponent.isPresent()) {
            MobMovementDrag.tickGroundOrAir(player.entityContext.stateVectorComponent);
        } else {
            MobMovementFriction.tickGroundOrAir(
                    player.entityContext.stateVectorComponent,
                    player.entityContext.mobTravelComponent);
        }
        AutoClimbSystem.tick(player, player.entityContext.stateVectorComponent);
        player.entityContext.stateVectorComponent.setDelta(player.entityContext.stateVectorComponent.getDelta().clone());
    }

    private static void tickFlyingPlayer(final GhostPlayer player) {
                PlayerFlyingMoveSpeed.tick(
                player,
                player.entityContext.movementSpeedComponent,
                player.entityContext.playerFlyingTravelComponent);
        DefaultMoveSystems.tickFlyingPlayer(
                player,
                player.entityContext.stateVectorComponent,
                player.entityContext.movementSpeedComponent,
                player.entityContext.mobTravelComponent);

        player.entityContext.stateVectorComponent.setDelta(player.entityContext.stateVectorComponent.getDelta().clone());
        move(player, player.entityContext.stateVectorComponent.getDelta().clone());

                MobMovementFriction.tickPlayerFlying(
                player,
                player.entityContext.stateVectorComponent,
                player.entityContext.playerFlyingTravelComponent);
        MobMovementDrag.tickPlayerFlying(player, player.entityContext.stateVectorComponent);
        player.entityContext.stateVectorComponent.setDelta(player.entityContext.stateVectorComponent.getDelta().clone());
    }

    private static void tickInFluid(final GhostPlayer player) {
        final float startY = player.entityContext.stateVectorComponent.getPosition().y;
        if ((player.entityContext.serverPlayerMovementComponent.getCurrentInputTick() != 1L && player.ghostMovementBridgeState.lavaSample.touching())) {
            player.entityContext.stateVectorComponent.setDelta(LavaTravelSystem.tick(player, player.entityContext.stateVectorComponent.getDelta()));
            move(player, player.entityContext.stateVectorComponent.getDelta().clone());
            ac.ghost.anticheat.prediction.bds.system.liquid.lava.MobMovementDrag.tick(player);
            LavaResetFallDistanceSystem.tick(player);
        } else {
            final WaterTravelSystem.Result result =
                    WaterTravelSystem.tick(player, player.entityContext.stateVectorComponent.getDelta());
            player.entityContext.stateVectorComponent.setDelta(result.velocity());
            move(player, player.entityContext.stateVectorComponent.getDelta().clone());
            ac.ghost.anticheat.prediction.bds.system.liquid.water.MobMovementDrag.tick(
                    player, result.depthStriderRatio());
        }
        ClimbOutOfLiquidSystem.tick(player, startY);
    }

    private static void move(final GhostPlayer player, final Vec3 movement) {
        TravelMoveRequestSystem.tick(player, movement);

                BlockMovementSlowdownMultiplierSystem.adjustFallDistance(
                player.entityContext.blockMovementSlowdownAppliedComponent,
                player.entityContext.fallDistanceComponent);
                        BlockMovementSlowdownMultiplierSystem.applySlowdownOnMove(
                player.entityContext.blockMovementSlowdownMultiplierComponent,
                player.entityContext.moveRequestComponent,
                player.entityContext.stateVectorComponent);
        player.entityContext.stateVectorComponent.setDelta(player.entityContext.stateVectorComponent.getDelta().clone());
        BlockMovementSlowdownMultiplierSystem.cleanupSystem(
                player.entityContext.blockMovementSlowdownAppliedComponent);

        final boolean noClip = NoClipOrNoBlockMoveFilterSystem.run(player);
        if (!noClip) {
            ServerVariableMaxAutoStepSystem.run(player);
            FlagPlayersForCollisionSystem.run(player);
            CopyCollisionShapesRewindSystem.run(player);
            MoveCollisionSystem.run(player);
            CollisionShapesCopySystem.run(player);
            SneakMovementSystem.run(player);
            UpdateOnewayCollisionsSystem.run(player);
            ConfigureDepenetration.run(player);
            ActorMoveSystem.run(player);
            AutoStepFilterSystem.run(player);
            AutoStepSystem.run(player);
            FinalizeMoveSystem.run(player);
            UpdateDepenetration.run(player);
        }

        BlockPosTrackerResetShouldTriggerStandOnSystem.tick(player);
        commitPosition(player);

        final MoveRequestComponent request = player.entityContext.moveRequestComponent;
        final Vec3 submitted = request.movement();
        final Vec3 resolved = request.resolvedMovement();
        final boolean collisionX = Math.abs(submitted.x - resolved.x) > FLOAT_EPSILON;
        final boolean collisionZ = Math.abs(submitted.z - resolved.z) > FLOAT_EPSILON;

        if (noClip) {
            player.entityContext.blockMovementSlowdownMultiplierComponent.clear();
            player.entityContext.blockMovementSlowdownAppliedComponent.clear();
            player.entityContext.horizontalCollisionFlagComponent.setPresent(false);
            player.entityContext.verticalCollisionFlagComponent.setPresent(false);
            player.entityContext.onGroundFlagComponent.setPresent(false);
            player.entityContext.horizontalCollisionFlagComponent.setPresent(false);
            player.entityContext.verticalCollisionFlagComponent.setPresent(false);
            player.entityContext.collisionFlagComponent.setPresent(false);
        } else {

            if ((Math.abs(player.entityContext.stateVectorComponent.getDelta().y) > 0.0F && player.entityContext.verticalCollisionFlagComponent.isPresent())
                    || player.entityContext.horizontalCollisionFlagComponent.isPresent()) {
                if (player.entityContext.playerFlyingTravelComponent.isPresent()) {
                    player.entityContext.stateVectorComponent.setDelta(new Vec3(
                            collisionX ? 0.0F : player.entityContext.stateVectorComponent.getDelta().x,
                            player.entityContext.verticalCollisionFlagComponent.isPresent() ? 0.0F : player.entityContext.stateVectorComponent.getDelta().y,
                            collisionZ ? 0.0F : player.entityContext.stateVectorComponent.getDelta().z));
                } else {
                    final Vec3 preResetVelocity =
                            player.entityContext.stateVectorComponent.getDelta().clone();

                    
                    
                    
                    
                    
                    ComputeBlockRestitutionSystem.tick(player, preResetVelocity);
                    RequestGravityCorrectionSystem.tick(player);

                    Vec3 resetVelocity =
                            ResetHorizontalVelocitySystem.tick(player, preResetVelocity);
                    resetVelocity = ResetVerticalVelocitySystem.tick(player, resetVelocity);
                    player.entityContext.stateVectorComponent.setDelta(
                            ApplyRestitution.tick(player, resetVelocity));
                }
            }
        }

        CurrentlyStandingOnBlockSystem.tick(player);
        BlockPosTrackerSystem.tick(player);

        player.entityContext.serverPlayerCurrentMovementComponent.setBeforeCollision(request.movement());
        player.entityContext.serverPlayerCurrentMovementComponent.setAfterCollision(request.resolvedMovement());
    }

    private static void runStandOnNotifications(final GhostPlayer player) {
        Filter.tick(player);
        SpeedAlteringBlockStandOn.tick(player);
        GenericOnStandOn.tick(player);
    }

    private static void commitPosition(final GhostPlayer player) {
        final Box aabb = player.entityContext.moveRequestComponent.resolvedAABB();
        ActorSetPosSystem.setImmediate(player, new Vec3(
                (aabb.minX + aabb.maxX) * 0.5F,
                aabb.minY,
                (aabb.minZ + aabb.maxZ) * 0.5F), true);
    }

}
