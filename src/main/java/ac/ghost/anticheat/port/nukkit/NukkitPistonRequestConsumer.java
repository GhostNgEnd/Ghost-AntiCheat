package ac.ghost.anticheat.port.nukkit;

import ac.ghost.anticheat.collision.bds.system.ActorMoveSystem;
import ac.ghost.anticheat.collision.bds.system.ActorSetPosSystem;
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
import ac.ghost.anticheat.compensated.cache.entity.EntityCache;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.system.block.BlockPosTrackerResetShouldTriggerStandOnSystem;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;


public final class NukkitPistonRequestConsumer {
    private static final float COLLISION_RESIDUAL = 0.400000006F;

    private NukkitPistonRequestConsumer() {
    }

    public static void tick(final GhostPlayer player) {
        final Vec3 push = player.ghostMovementBridgeState.pendingPistonMovement.take();
        if (push == null
                || !player.entityContext.shouldBeSimulatedComponent.isPresent()
                || !player.entityContext.pushableByBlockComponent.isPresent()) {
            return;
        }

        player.entityContext.movementInterpolatorComponent.reset(
                player.entityContext.stateVectorComponent.getPosition(),
                player.entityContext.actorRotationComponent.getPitch(),
                player.entityContext.actorRotationComponent.getYaw(),
                player.entityContext.actorRotationComponent.getHeadYaw());

        final Box shape = player.entityContext.aabbShapeComponent.getAABB();
        final float dimensionA = shape.maxX - shape.minX;
        final float dimensionB = shape.maxZ - shape.minZ;
        final float threshold = Math.max(dimensionA, dimensionB) * 0.5F + 0.5F;
        final float lengthSquared = push.lengthSquared();

        Vec3 collisionMove = push;
        if (lengthSquared >= threshold * threshold) {
            final float length = (float) Math.sqrt(lengthSquared);
            if (length > COLLISION_RESIDUAL) {
                final Vec3 direction = push.multiply(1.0F / length);
                final Vec3 prefix = direction.multiply(length - COLLISION_RESIDUAL);
                if (!isPrefixBlocked(player, shape, prefix)) {
                    player.entityContext.actorSetPositionRequestComponent.set(
                            player.entityContext.stateVectorComponent.getPosition().add(prefix));
                    ActorSetPosSystem.run(player);
                    collisionMove = direction.multiply(COLLISION_RESIDUAL);
                }
            }
        }

        player.entityContext.moveRequestComponent.begin(
                collisionMove, player.entityContext.aabbShapeComponent.getAABB());
        if (!NoClipOrNoBlockMoveFilterSystem.run(player)) {
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
        player.entityContext.serverPlayerCurrentMovementComponent.setBeforeCollision(
                player.entityContext.moveRequestComponent.movement());
        player.entityContext.serverPlayerCurrentMovementComponent.setAfterCollision(
                player.entityContext.moveRequestComponent.resolvedMovement());
        player.entityContext.moveRequestComponent.clear();
    }

    private static boolean isPrefixBlocked(final GhostPlayer player,
                                           final Box start,
                                           final Vec3 prefix) {
        final Box swept = start.union(start.offset(prefix));
        final int dimension = player.entityContext.blockSource.getDimension();

        for (final EntityCache candidate : player.entityRegistry.entities().values()) {
            if (candidate == null
                    || candidate.dimension() != dimension
                    || candidate.currentState() == null) {
                continue;
            }

            candidate.refreshAABBShapeComponent();
            if (candidate.aabbShapeComponent().isPresent()
                    && swept.intersects(candidate.aabbShapeComponent().getAABB())) {
                return true;
            }
        }
        return false;
    }

    private static void commitPosition(final GhostPlayer player) {
        final Box aabb = player.entityContext.moveRequestComponent.resolvedAABB();
        ActorSetPosSystem.setImmediate(player, new Vec3(
                (aabb.minX + aabb.maxX) * 0.5F,
                aabb.minY,
                (aabb.minZ + aabb.maxZ) * 0.5F), true);
    }
}
