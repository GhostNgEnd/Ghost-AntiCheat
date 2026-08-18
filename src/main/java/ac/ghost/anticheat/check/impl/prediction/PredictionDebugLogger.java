package ac.ghost.anticheat.check.impl.prediction;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.data.input.PredictionData;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.player.state.GhostMovementBridgeState;
import ac.ghost.anticheat.prediction.bds.component.MoveInputComponent;
import ac.ghost.anticheat.prediction.bds.component.PlayerActionComponent;
import ac.ghost.anticheat.prediction.bds.component.ServerPlayerCurrentMovementComponent;
import ac.ghost.anticheat.prediction.bds.system.liquid.common.LiquidPhysicsSystem;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.entity.Entity;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.network.protocol.types.AuthInputAction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Set;


public final class PredictionDebugLogger {
    private static final Object FILE_LOCK = new Object();
    private static final String FILE_NAME = "prediction-debug.log";

    private PredictionDebugLogger() {
    }

    public static void logFailure(final GhostPlayer player,
                                  final float offset,
                                  final float threshold) {
        if (player == null || Ghost.getConfig() == null || !Ghost.getConfig().debugMode()) {
            return;
        }

        try {
            final String line = buildLine(player, offset, threshold);
            writeToFile(line);

            final PluginBase plugin = Ghost.getPluginInstance();
            if (plugin != null) {
                plugin.getLogger().info(line);
            }
        } catch (Throwable throwable) {
            
            final PluginBase plugin = Ghost.getPluginInstance();
            if (plugin != null) {
                plugin.getLogger().warning("[PredictionDebug] failed to write snapshot: "
                        + throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
            }
        }
    }

    private static String buildLine(final GhostPlayer player,
                                    final float offset,
                                    final float threshold) {
        final ServerPlayerCurrentMovementComponent movement =
                player.entityContext.serverPlayerCurrentMovementComponent;
        final PredictionData prediction = movement.getPredictionResult();
        final MoveInputComponent input = player.entityContext.moveInputComponent;
        final PlayerActionComponent action = player.entityContext.playerActionComponent;
        final Set<AuthInputAction> actions = action.actions();
        final LiquidPhysicsSystem.Sample water = player.ghostMovementBridgeState.waterSample;

        final Vec3 predictedPos = player.entityContext.stateVectorComponent.getPosition();
        final Vec3 actualPos = movement.getUnvalidatedPosition();
        final Vec3 previousActualPos = movement.getPreviousUnvalidatedPosition();
        final Vec3 predictedDelta = predictedPos.subtract(previousActualPos);
        final Vec3 actualDelta = actualPos.subtract(previousActualPos);
        final Vec3 tickEnd = player.entityContext.stateVectorComponent.getDelta();
        final float eotOffset = movement.getUnvalidatedTickEnd().distanceTo(tickEnd);

        final BlockLegacy trackedBlock = player.entityContext.blockPosTrackerComponent.currentBlock();
        final BlockVector3 trackedPos = player.entityContext.blockPosTrackerComponent.currentPosition();
        final BlockLegacy feetBlock = blockAt(player, predictedPos, 0);
        final BlockLegacy belowBlock = blockAt(player, predictedPos, -1);

        final boolean startSwimming = actions.contains(AuthInputAction.START_SWIMMING);
        final boolean stopSwimming = actions.contains(AuthInputAction.STOP_SWIMMING);
        final boolean startSprinting = actions.contains(AuthInputAction.START_SPRINTING);
        final boolean stopSprinting = actions.contains(AuthInputAction.STOP_SPRINTING);
        final boolean startJumping = actions.contains(AuthInputAction.START_JUMPING);
        final boolean swimming = player.entityContext.actorDataFlagComponent.has(Entity.DATA_FLAG_SWIMMING);
        final boolean sneaking = player.entityContext.actorDataFlagComponent.has(Entity.DATA_FLAG_SNEAKING);
        final boolean crawling = player.entityContext.actorDataFlagComponent.has(Entity.DATA_FLAG_CRAWLING);
        final boolean sprinting = player.entityContext.actorDataFlagComponent.has(Entity.DATA_FLAG_SPRINTING);
        final boolean gliding = player.entityContext.actorDataFlagComponent.has(Entity.DATA_FLAG_GLIDING);
        final boolean jumping = input.hasStateFlag(MoveInputComponent.STATE_JUMPING);
        final boolean autoJumping = input.hasStateFlag(MoveInputComponent.STATE_AUTO_JUMPING_IN_WATER);
        final boolean waterTransitionCandidate = water.touching()
                && (startSwimming || stopSwimming || startSprinting || stopSprinting
                || startJumping || jumping || autoJumping
                || player.ghostMovementBridgeState.wasPredictionSwimming != swimming);
        final boolean slimeCandidate = isSlime(trackedBlock) || isSlime(feetBlock) || isSlime(belowBlock)
                || player.ghostMovementBridgeState.debugStandSpeedAlteringApplied
                || player.ghostMovementBridgeState.debugRestitutionApplied
                || (prediction != null && prediction.before() != null
                && prediction.after() != null
                && prediction.before().y < -0.08F && prediction.after().y > 0.0F);

        final StringBuilder out = new StringBuilder(1400);
        out.append("[PredictionDebug]")
                .append(" time=").append(Instant.now())
                .append(" player=").append(safeName(player))
                .append(" protocol=").append(player.getSession().protocol)
                .append(" tick=").append(player.entityContext.serverPlayerMovementComponent.getCurrentInputTick())
                .append(" offset=").append(offset)
                .append(" threshold=").append(threshold)
                .append(" eotOffset=").append(eotOffset)
                .append(" actualTickEnd=").append(vec(movement.getUnvalidatedTickEnd()))
                .append(" previousActualTickEnd=").append(vec(
                        movement.getPreviousUnvalidatedTickEnd()))
                .append(" waterTransitionCandidate=").append(waterTransitionCandidate)
                .append(" slimeCandidate=").append(slimeCandidate)
                .append(" predictedPos=").append(vec(predictedPos))
                .append(" actualPos=").append(vec(actualPos))
                .append(" previousActualPos=").append(vec(previousActualPos))
                .append(" predictedDelta=").append(vec(predictedDelta))
                .append(" actualDelta=").append(vec(actualDelta))
                .append(" stateDelta=").append(vec(tickEnd))
                .append(" lastFinalVelocity=").append(vec(movement.getLastTickFinalVelocity()))
                .append(" velocitySource=").append(
                        player.entityContext.playerTickStartVelocityComponent.selectedType())
                .append(" tickStartVelocity=").append(vec(
                        player.entityContext.playerTickStartVelocityComponent.selectedVelocity()))
                .append(" velocityCandidatesCompared=").append(
                        player.entityContext.playerTickStartVelocityComponent.comparedCandidates())
                .append(" uncertainDistanceSquared=").append(
                        player.entityContext.playerTickStartVelocityComponent.uncertainDistanceSquared())
                .append(" ordinaryDistanceSquared=").append(
                        player.entityContext.playerTickStartVelocityComponent.ordinaryDistanceSquared())
                .append(" pendingUncertainVelocity=").append(
                        player.entityContext.playerTickStartVelocityComponent.hasUncertainVelocity())
                .append(" pendingCertainVelocity=").append(
                        player.entityContext.playerTickStartVelocityComponent.hasCertainVelocity());

        if (prediction != null) {
            out.append(" preCollisionVelocity=").append(vec(prediction.before()))
                    .append(" postCollisionVelocity=").append(vec(prediction.after()))
                    .append(" predictedTickEndVelocity=").append(vec(prediction.tickEnd()));
        }

        out.append(" onGround=").append(player.entityContext.onGroundFlagComponent.isPresent())
                .append(" movementCapturedOnGround=").append(movement.isOnGround())
                .append(" horizontalCollision=").append(player.entityContext.horizontalCollisionFlagComponent.isPresent())
                .append(" verticalCollision=").append(player.entityContext.verticalCollisionFlagComponent.isPresent())
                .append(" collisionResponse=").append(player.entityContext.moveRequestComponent.collisionResponse())
                .append(" submittedMove=").append(vec(player.entityContext.moveRequestComponent.movement()))
                .append(" resolvedMove=").append(vec(player.entityContext.moveRequestComponent.resolvedMovement()))
                .append(" fallDistance=").append(player.entityContext.fallDistanceComponent.getValue())
                .append(" trackedBlock=").append(block(trackedBlock, trackedPos))
                .append(" standOnFilterResult=")
                .append(player.entityContext.blockPosTrackerComponent.shouldTriggerStandOn())
                .append(" feetBlock=").append(block(feetBlock, null))
                .append(" belowBlock=").append(block(belowBlock, null))
                .append(" standSpeedAlteringNow=")
                .append(player.entityContext.standOnSpeedAlteringBlockFlagComponent.isPresent())
                .append(" standSpeedApplied=").append(player.ghostMovementBridgeState.debugStandSpeedAlteringApplied)
                .append(" standSpeedY=").append(player.ghostMovementBridgeState.debugStandSpeedVerticalSpeed)
                .append(" standSpeedScale=").append(player.ghostMovementBridgeState.debugStandSpeedHorizontalScale)
                .append(" restitutionImpactY=").append(player.ghostMovementBridgeState.debugRestitutionImpactY)
                .append(" restitutionCandidates=").append(player.ghostMovementBridgeState.debugRestitutionCollisionCandidateCount)
                .append(" restitutionBlock=").append(player.ghostMovementBridgeState.debugRestitutionSelectedBlock)
                .append(" bdsCorrectionResult=").append(player.ghostMovementBridgeState.debugBdsCorrectionResult)
                .append(" restitutionApplied=").append(player.ghostMovementBridgeState.debugRestitutionApplied)
                .append(" restitutionVelocity=").append(vec(player.ghostMovementBridgeState.debugRestitutionVelocity))
                .append(" bounceGravityCorrection=").append(player.ghostMovementBridgeState.debugBounceGravityCorrection)
                .append(" bounceRequestedY=").append(player.ghostMovementBridgeState.debugBounceRequestedY)
                .append(" bounceResolvedY=").append(player.ghostMovementBridgeState.debugBounceResolvedY)
                .append(" bounceAppliedGravity=").append(player.ghostMovementBridgeState.debugBounceAppliedGravity)
                .append(" elasticCurrent=").append(elastic(
                        player.ghostMovementBridgeState.debugElasticTrace))
                .append(" elasticPrevious=").append(elastic(
                        player.ghostMovementBridgeState.debugPreviousElasticTrace))
                .append(" movementMetadata={sneaking=").append(sneaking)
                .append(",crawling=").append(crawling)
                .append(",sprinting=").append(sprinting)
                .append(",swimming=").append(swimming)
                .append(",gliding=").append(gliding).append('}')
                .append(" acknowledgedMetadata={sneaking=").append(
                        player.entityContext.synchedActorDataComponent.has(Entity.DATA_FLAG_SNEAKING))
                .append(",crawling=").append(
                        player.entityContext.synchedActorDataComponent.has(Entity.DATA_FLAG_CRAWLING))
                .append(",sprinting=").append(
                        player.entityContext.synchedActorDataComponent.has(Entity.DATA_FLAG_SPRINTING))
                .append(",swimming=").append(
                        player.entityContext.synchedActorDataComponent.has(Entity.DATA_FLAG_SWIMMING))
                .append(",gliding=").append(
                        player.entityContext.synchedActorDataComponent.has(Entity.DATA_FLAG_GLIDING))
                .append('}')
                .append(" actions=").append(actions)
                .append(" moveFlags=0x").append(Integer.toHexString(input.getFlags()))
                .append(" stateFlags=0x").append(Integer.toHexString(input.getStateFlags()))
                .append(" axis=").append('(').append(input.getAxisX()).append(',').append(input.getAxisY()).append(')')
                .append(" effective=").append('(').append(input.getEffectiveX()).append(',').append(input.getEffectiveY()).append(')')
                .append(" jumpDown=").append(input.hasFlag(MoveInputComponent.JUMP_DOWN))
                .append(" jumping=").append(jumping)
                .append(" autoJumpingInWater=").append(autoJumping)
                .append(" mobIsJumping=").append(player.entityContext.mobIsJumpingFlagComponent.isPresent())
                .append(" wasPredictionSwimming=").append(player.ghostMovementBridgeState.wasPredictionSwimming)
                .append(" swimStartRequested=").append(player.ghostMovementBridgeState.debugSwimStartRequested)
                .append(" swimStartAccepted=").append(player.ghostMovementBridgeState.debugSwimStartAccepted)
                .append(" swimStopTriggered=").append(player.ghostMovementBridgeState.debugSwimStopTriggered)
                .append(" swimming=").append(swimming)
                .append(" sprintStartAction=").append(startSprinting)
                .append(" sprintStopAction=").append(stopSprinting)
                .append(" sprinting=").append(sprinting)
                .append(" sprintStateFlag=").append(input.hasStateFlag(MoveInputComponent.STATE_SPRINTING))
                .append(" sprintDown=").append(input.hasFlag(MoveInputComponent.SPRINT_DOWN))
                .append(" swimAmount=").append(player.entityContext.swimAmountComponent.getPrevious())
                .append("->").append(player.entityContext.swimAmountComponent.getCurrent())
                .append(" waterTouching=").append(water.touching())
                .append(" waterSurfaceY=").append(water.surfaceHeight())
                .append(" waterPush=").append(vec(water.appliedPush()))
                .append(" wasInWater=").append(player.entityContext.wasInWaterFlagComponent.isPresent())
                .append(" waterTravel=").append(player.entityContext.waterTravelFlagComponent.isPresent())
                .append(" headInWater=").append(player.entityContext.actorHeadInWaterFlagComponent.isPresent())
                .append(" breathingInAir=").append(player.entityContext.playerInputRequestComponent.isBreathingInAir())
                .append(" breathingInLiquid=").append(player.entityContext.playerInputRequestComponent.isBreathingInLiquid())
                .append(" pitch=").append(player.entityContext.actorRotationComponent.getPitch())
                .append(" yaw=").append(player.entityContext.actorRotationComponent.getYaw());
        return out.toString();
    }

    private static String elastic(
            final GhostMovementBridgeState.ElasticPhysicsDebugTrace trace) {
        if (trace == null) {
            return "null";
        }
        return "{tick=" + trace.inputTick
                + ",startPos=" + vec(trace.tickStartPosition)
                + ",carriedStart=" + vec(trace.carriedStartVelocity)
                + ",selectedStart=" + vec(trace.selectedStartVelocity)
                + ",groundStart=" + trace.onGroundAtTickStart
                + ",groundBeforeTravel=" + trace.onGroundBeforeTravel
                + ",submitted=" + vec(trace.submittedMovement)
                + ",resolved=" + vec(trace.resolvedMovement)
                + ",originalAabb=" + trace.originalAabb
                + ",resolvedAabb=" + trace.resolvedAabb
                + ",endPos=" + vec(trace.tickEndPosition)
                + ",endVelocity=" + vec(trace.tickEndVelocity)
                + ",groundEnd=" + trace.onGroundAtTickEnd
                + ",impactY=" + trace.restitutionImpactY
                + ",candidates=" + trace.collisionCandidateCount
                + ",actorAabb=" + trace.actorAabb
                + ",collisionEntries=" + trace.collisionEntries
                + ",selectedShape=" + trace.selectedShape
                + ",block=" + trace.selectedBlock
                + ",coefficient=" + trace.restitutionCoefficient
                + ",requestChecked=" + trace.gravityRequestChecked
                + ",requestRestitutionY=" + trace.gravityRequestRestitutionY
                + ",requestMovementY=" + trace.gravityRequestMovementY
                + ",requestResolvedY=" + trace.gravityRequestResolvedY
                + ",requestCreated=" + trace.gravityRequestCreated
                + ",correctionApplied=" + trace.gravityCorrectionApplied
                + ",ordinaryGravityY=" + trace.ordinaryGravityY
                + ",correctionRequestedY=" + trace.correctionRequestedY
                + ",correctionResolvedY=" + trace.correctionResolvedY
                + ",gravityMagnitude=" + trace.gravityMagnitude
                + ",resolvedMagnitude=" + trace.resolvedMagnitude
                + ",radicand=" + trace.radicand
                + ",impactSpeed=" + trace.impactSpeed
                + ",impactFraction=" + trace.impactFraction
                + ",remainingTick=" + trace.remainingTick
                + ",appliedGravityY=" + trace.appliedGravityY + '}';
    }

    private static BlockLegacy blockAt(final GhostPlayer player,
                                       final Vec3 position,
                                       final int yOffset) {
        if (position == null) {
            return null;
        }
        return player.entityContext.localConstBlockSourceFactoryComponent.create().getBlockState(
                (int) Math.floor(position.x),
                (int) Math.floor(position.y) + yOffset,
                (int) Math.floor(position.z),
                0);
    }

    private static boolean isSlime(final BlockLegacy block) {
        return block != null && block.isSlimeBlock();
    }

    private static String block(final BlockLegacy block,
                                final BlockVector3 positionOverride) {
        if (block == null) {
            return "null";
        }
        final String identifier = block.getNetworkState() == null
                ? "" : block.getNetworkState().identifier();
        final String name = identifier == null || identifier.isBlank()
                ? "legacy:" + block.getBlock().getId() + ':' + block.getBlock().getDamage()
                : identifier;
        final BlockVector3 pos = positionOverride == null ? block.getPosition() : positionOverride;
        return name + '@' + (pos == null ? "?" : pos.toString())
                + "{restitution=" + block.getCoefficientOfRestitution()
                + ",friction=" + block.getFriction() + '}';
    }

    private static String vec(final Vec3 value) {
        if (value == null) {
            return "null";
        }
        return '(' + Float.toString(value.x) + ',' + Float.toString(value.y) + ','
                + Float.toString(value.z) + ')';
    }

    private static String safeName(final GhostPlayer player) {
        try {
            return player.getSession().getName().replace(' ', '_');
        } catch (Throwable ignored) {
            return "unknown";
        }
    }

    private static void writeToFile(final String line) throws IOException {
        final PluginBase plugin = Ghost.getPluginInstance();
        if (plugin == null || plugin.getDataFolder() == null) {
            return;
        }
        final Path directory = plugin.getDataFolder().toPath();
        final Path file = directory.resolve(FILE_NAME);
        synchronized (FILE_LOCK) {
            Files.createDirectories(directory);
            Files.write(file,
                    (line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND);
        }
    }
}
