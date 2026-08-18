package ac.ghost.anticheat.prediction.bds.system.restitution;

import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.ApplyRestitutionComponent;
import ac.ghost.anticheat.prediction.model.CollisionShapeEntry;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.entity.Entity;

import java.util.List;










public final class ComputeBlockRestitutionSystem {
    
    private static final float MIN_DOWNWARD_IMPACT =
            0.08000011742115021F;
    private static final float FLOAT_EPSILON = 1.1920929E-7F;
    private static final float FOOT_PLANE_OFFSET = -0.20000000298023224F;
    private static final float HALF = 0.5F;

    private ComputeBlockRestitutionSystem() {
    }

    




    public static void tick(final GhostPlayer player,
                            final Vec3 preResetVelocity) {
        if (player == null || preResetVelocity == null) {
            return;
        }
        if (player.entityContext.actorDataFlagComponent.has(Entity.DATA_FLAG_SNEAKING)) {
            return;
        }
        player.ghostMovementBridgeState.debugRestitutionImpactY = preResetVelocity.y;
        player.ghostMovementBridgeState.debugRestitutionCollisionCandidateCount =
                player.entityContext.moveRequestComponent.collisionShapeEntries().size();
        player.ghostMovementBridgeState.debugElasticTrace.restitutionImpactY =
                preResetVelocity.y;
        player.ghostMovementBridgeState.debugElasticTrace.collisionCandidateCount =
                player.entityContext.moveRequestComponent.collisionShapeEntries().size();
        player.ghostMovementBridgeState.debugElasticTrace.actorAabb =
                String.valueOf(player.entityContext.aabbShapeComponent.getAABB());
        player.ghostMovementBridgeState.debugElasticTrace.collisionEntries =
                describeCollisionEntries(
                        player.entityContext.moveRequestComponent.collisionShapeEntries());
        if (preResetVelocity.y >= 0.0F) {
            return;
        }
        
        
        
        
        if (Math.abs(preResetVelocity.y) < MIN_DOWNWARD_IMPACT) {
            return;
        }

        






        final CollisionShapeEntry selected = selectCollisionShape(
                player.entityContext.aabbShapeComponent.getAABB(),
                player.entityContext.moveRequestComponent.collisionShapeEntries());
        if (selected == null) {
            return;
        }
        player.ghostMovementBridgeState.debugElasticTrace.selectedShape =
                String.valueOf(selected.shape());

        final BlockLegacy support = selected.block();
        if (support == null) {
            return;
        }
        player.ghostMovementBridgeState.debugRestitutionSelectedBlock =
                support.getNetworkState() == null
                        ? "legacy:" + support.getBlock().getId() + ':' + support.getBlock().getDamage()
                        : String.valueOf(support.getNetworkState().identifier());
        player.ghostMovementBridgeState.debugElasticTrace.selectedBlock =
                player.ghostMovementBridgeState.debugRestitutionSelectedBlock;

        final float coefficient = support.getCoefficientOfRestitution();
        player.ghostMovementBridgeState.debugElasticTrace.restitutionCoefficient =
                coefficient;
        if (!Float.isFinite(coefficient)
                || Math.abs(coefficient) <= FLOAT_EPSILON) {
            return;
        }

        ApplyRestitutionComponent output =
                player.entityContext.applyRestitutionComponent;
        if (output == null) {
            output = new ApplyRestitutionComponent();
            player.entityContext.applyRestitutionComponent = output;
        }

        if (coefficient == -1.0F) {
            if (output.velocity().y > 0.0F) {
                output.velocity().y = 0.0F;
            }
            return;
        }

        final float candidateY = -preResetVelocity.y * coefficient;
        output.velocity().y = Math.max(output.velocity().y, candidateY);
    }

    
    private static CollisionShapeEntry selectCollisionShape(
            final Box actorAABB,
            final List<CollisionShapeEntry> collisionShapes) {
        if (actorAABB == null || !actorAABB.isValid()
                || collisionShapes == null || collisionShapes.isEmpty()) {
            return null;
        }

        final float targetX = midpoint(actorAABB.minX, actorAABB.maxX);
        final float targetY = actorAABB.minY + FOOT_PLANE_OFFSET;
        final float targetZ = midpoint(actorAABB.minZ, actorAABB.maxZ);
        CollisionShapeEntry selected = null;
        float selectedVerticalDistance = Float.MAX_VALUE;
        float selectedDistanceSquared = Float.MAX_VALUE;

        for (final CollisionShapeEntry entry : collisionShapes) {
            if (entry == null || !entry.shape().isValid()) {
                continue;
            }
            final Box candidate = entry.shape();
            final float candidateCenterY = midpoint(candidate.minY, candidate.maxY);
            final float verticalDistance = targetY - candidateCenterY;
            if (verticalDistance < 0.0F) {
                continue;
            }
            if (selected == null || selectedVerticalDistance > verticalDistance) {
                selected = entry;
                selectedVerticalDistance = verticalDistance;
                selectedDistanceSquared = distanceSquared(targetX, targetY, targetZ, candidate);
                continue;
            }
            if (selectedVerticalDistance != verticalDistance) {
                continue;
            }
            final float candidateDistanceSquared =
                    distanceSquared(targetX, targetY, targetZ, candidate);
            if (selectedDistanceSquared > candidateDistanceSquared) {
                selected = entry;
                selectedDistanceSquared = candidateDistanceSquared;
            }
        }
        return selected;
    }

    private static float midpoint(final float minimum, final float maximum) {
        return (maximum - minimum) * HALF + minimum;
    }

    private static float distanceSquared(
            final float targetX,
            final float targetY,
            final float targetZ,
            final Box candidate) {
        final float deltaX = midpoint(candidate.minX, candidate.maxX) - targetX;
        final float deltaY = midpoint(candidate.minY, candidate.maxY) - targetY;
        final float deltaZ = midpoint(candidate.minZ, candidate.maxZ) - targetZ;
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    private static String describeCollisionEntries(
            final List<CollisionShapeEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return "[]";
        }
        final StringBuilder result = new StringBuilder("[");
        for (int index = 0; index < entries.size(); index++) {
            if (index != 0) {
                result.append(',');
            }
            final CollisionShapeEntry entry = entries.get(index);
            if (entry == null) {
                result.append("null");
                continue;
            }
            result.append("{shape=").append(entry.shape());
            final BlockLegacy block = entry.block();
            if (block == null) {
                result.append(",block=non-block}");
                continue;
            }
            result.append(",block=");
            if (block.getNetworkState() == null) {
                result.append("legacy:")
                        .append(block.getBlock().getId())
                        .append(':')
                        .append(block.getBlock().getDamage());
            } else {
                result.append(block.getNetworkState().identifier());
            }
            result.append(",restitution=")
                    .append(block.getCoefficientOfRestitution())
                    .append('}');
        }
        return result.append(']').toString();
    }

}
