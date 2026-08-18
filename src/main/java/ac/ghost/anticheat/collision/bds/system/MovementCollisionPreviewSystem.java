package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.DepenetrationComponent;
import ac.ghost.anticheat.prediction.bds.component.RewindCollisionShapesComponent;
import ac.ghost.anticheat.prediction.bds.system.travel.TravelMoveRequestSystem;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;

import java.util.ArrayList;
import java.util.List;





public final class MovementCollisionPreviewSystem {
    private MovementCollisionPreviewSystem() {
    }

    public static Result tick(final GhostPlayer player,
                              final Vec3 movement) {
        final Snapshot snapshot = Snapshot.capture(player);
        try {
            TravelMoveRequestSystem.tick(player, movement);
            if (player.entityContext.blockMovementSlowdownMultiplierComponent
                    .isPresent()) {
                player.entityContext.moveRequestComponent.multiplyMovement(
                        player.entityContext
                                .blockMovementSlowdownMultiplierComponent.value());
            }

            final boolean noClip = NoClipOrNoBlockMoveFilterSystem.run(player);
            if (!noClip) {
                ServerVariableMaxAutoStepSystem.run(player);
                FlagPlayersForCollisionSystem.run(player);
                CopyCollisionShapesRewindSystem.run(player);
                MoveCollisionSystem.run(player);
                
                
                
                SneakMovementSystem.run(player);
                UpdateOnewayCollisionsSystem.run(player);
                ConfigureDepenetration.run(player);
                ActorMoveSystem.run(player);
                AutoStepFilterSystem.run(player);
                AutoStepSystem.run(player);
                FinalizeMoveSystem.run(player);
            }

            return new Result(
                    player.entityContext.moveRequestComponent.movement(),
                    player.entityContext.moveRequestComponent.resolvedMovement(),
                    player.entityContext.horizontalCollisionFlagComponent
                            .isPresent(),
                    player.entityContext.verticalCollisionFlagComponent
                            .isPresent());
        } finally {
            snapshot.restore(player);
        }
    }

    public record Result(Vec3 submitted,
                         Vec3 resolved,
                         boolean horizontalCollision,
                         boolean verticalCollision) {
        public Result {
            submitted = submitted.clone();
            resolved = resolved.clone();
        }

        @Override
        public Vec3 submitted() {
            return submitted.clone();
        }

        @Override
        public Vec3 resolved() {
            return resolved.clone();
        }
    }

    private record Snapshot(
            Vec3 stateDelta,
            float maxAutoStep,
            boolean collidableMobNear,
            RewindCollisionShapesComponent rewindCollisionShapes,
            int depenetrationFlags,
            Vec3 depenetrationMagnitude,
            List<Box> depenetrationCollisionBoxes,
            Vec3 depenetrationCustomMagnitude,
            boolean depenetrationUsesCustomMagnitude,
            boolean autoStepRequested,
            boolean hasAutoStepped,
            boolean onGround,
            boolean horizontalCollision,
            boolean verticalCollision,
            boolean collision) {

        static Snapshot capture(final GhostPlayer player) {
            final DepenetrationComponent dep =
                    player.entityContext.depenetrationComponent;
            final ArrayList<Box> collisionBoxes = new ArrayList<>();
            for (final Box box : dep.collisionBoxes()) {
                if (box != null) {
                    collisionBoxes.add(box.clone());
                }
            }
            return new Snapshot(
                    player.entityContext.stateVectorComponent.getDelta().clone(),
                    player.entityContext.maxAutoStepComponent.value(),
                    player.entityContext.collidableMobNearFlagComponent
                            .isPresent(),
                    player.entityContext.rewindCollisionShapesComponent,
                    dep.flags(),
                    dep.magnitude(),
                    collisionBoxes,
                    dep.customMagnitude(),
                    dep.useCustomMagnitude(),
                    player.entityContext.autoStepRequestFlagComponent
                            .isPresent(),
                    player.entityContext.hasAutoSteppedComponent.isPresent(),
                    player.entityContext.onGroundFlagComponent.isPresent(),
                    player.entityContext.horizontalCollisionFlagComponent
                            .isPresent(),
                    player.entityContext.verticalCollisionFlagComponent
                            .isPresent(),
                    player.entityContext.collisionFlagComponent.isPresent());
        }

        void restore(final GhostPlayer player) {
            player.entityContext.stateVectorComponent.setDelta(
                    this.stateDelta.clone());
            player.entityContext.moveRequestComponent.clear();
            player.entityContext.maxAutoStepComponent.set(this.maxAutoStep);
            player.entityContext.collidableMobNearFlagComponent.setPresent(
                    this.collidableMobNear);
            player.entityContext.rewindCollisionShapesComponent =
                    this.rewindCollisionShapes;

            final DepenetrationComponent dep =
                    player.entityContext.depenetrationComponent;
            dep.setFlags(this.depenetrationFlags);
            dep.setMagnitude(this.depenetrationMagnitude);
            dep.collisionBoxes().clear();
            for (final Box box : this.depenetrationCollisionBoxes) {
                dep.collisionBoxes().add(box.clone());
            }
            if (this.depenetrationUsesCustomMagnitude) {
                dep.setCustomMagnitude(this.depenetrationCustomMagnitude);
            } else {
                dep.clearCustomMagnitude();
            }

            player.entityContext.autoStepRequestFlagComponent.setPresent(
                    this.autoStepRequested);
            player.entityContext.hasAutoSteppedComponent.setPresent(
                    this.hasAutoStepped);
            player.entityContext.onGroundFlagComponent.setPresent(
                    this.onGround);
            player.entityContext.horizontalCollisionFlagComponent.setPresent(
                    this.horizontalCollision);
            player.entityContext.verticalCollisionFlagComponent.setPresent(
                    this.verticalCollision);
            player.entityContext.collisionFlagComponent.setPresent(
                    this.collision);
        }
    }
}
