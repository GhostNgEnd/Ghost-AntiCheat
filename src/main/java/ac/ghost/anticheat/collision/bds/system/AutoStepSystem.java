package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.MoveRequestComponent;
import ac.ghost.anticheat.prediction.bds.component.RewindCollisionShapesComponent;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class AutoStepSystem {
    private AutoStepSystem() {
    }

    public static void run(final GhostPlayer player) {
        if (!player.entityContext.autoStepRequestFlagComponent.isPresent()) {
            return;
        }

        final MoveRequestComponent request = player.entityContext.moveRequestComponent;
        final RewindCollisionShapesComponent cache = player.entityContext.rewindCollisionShapesComponent;
        final List<Box> allCollisions = cache == null
                ? Collections.emptyList() : cache.collisionShapes();
        final Box originalAABB = request.originalAABB();
        final ArrayList<Box> stepCollisions = new ArrayList<>(allCollisions.size());
        for (final Box collider : allCollisions) {
            if (collider.minY < originalAABB.maxY) {
                stepCollisions.add(collider);
            }
        }

        final Vec3 submitted = request.movement();
        final ActorMoveSystem.SolveResult upHorizontal =
                ActorMoveSystem.solveSegments(
                        originalAABB,
                        stepCollisions,
                        request.depenetrationMagnitude(),
                        new Vec3(0.0F, player.entityContext.maxAutoStepComponent.value(), 0.0F),
                        new Vec3(submitted.x, 0.0F, 0.0F),
                        new Vec3(0.0F, 0.0F, submitted.z));
        final float requestedDown = -upHorizontal.movement.y;
        final ActorMoveSystem.SolveResult down =
                ActorMoveSystem.solveSegments(
                        upHorizontal.aabb,
                        stepCollisions,
                        request.depenetrationMagnitude(),
                        new Vec3(0.0F, requestedDown, 0.0F));

        final Vec3 candidate = upHorizontal.movement.add(down.movement);
        final Box candidateAABB = down.aabb;
        final boolean finalOverlap = ActorMoveSystem.hasCollision(
                candidateAABB, allCollisions);
        final boolean downwardContact = requestedDown < 0.0F
                && Math.abs(requestedDown - down.movement.y)
                > ActorMoveSystem.FLOAT_EPSILON;
        request.addOverlapDepth(upHorizontal.overlapDepth + down.overlapDepth);

        final boolean accepted = !finalOverlap
                && ActorMoveSystem.isReasonable(candidate)
                && ActorMoveSystem.isReasonable(candidateAABB)
                && candidate.horizontalLengthSquared()
                > request.ordinaryMovement().horizontalLengthSquared();
        if (!accepted) {
            return;
        }

        request.setResolvedResult(candidate, candidateAABB);
        player.entityContext.hasAutoSteppedComponent.setPresent(true);
        if (downwardContact) {
            player.entityContext.onGroundFlagComponent.setPresent(true);
        }
    }
}
