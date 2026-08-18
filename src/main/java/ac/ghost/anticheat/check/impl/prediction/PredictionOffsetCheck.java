package ac.ghost.anticheat.check.impl.prediction;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.check.api.impl.OffsetHandlerCheck;
import ac.ghost.anticheat.player.GhostPlayer;


public abstract class PredictionOffsetCheck extends OffsetHandlerCheck {
    protected PredictionOffsetCheck(final GhostPlayer player) {
        super(player);
    }

    protected final boolean isServerVelocityTick() {
        return player.entityContext.playerTickStartVelocityComponent.selectedServerVelocity();
    }

    protected final boolean canCheck(final float offset) {
        return canCheck(offset, Ghost.getConfig().predictionThreshold());
    }

    protected final boolean canCheck(final float offset, final float threshold) {
        if (player.entityContext.serverPlayerMovementComponent.getCurrentInputTick() < 10) {
            return false;
        }
        if (!Float.isFinite(offset) || offset <= Math.max(0.0F, threshold)) {
            return false;
        }
        return shouldDoFail();
    }

    protected final int failPrediction(final String checkName, final float offset) {
        final String verbose = "offset=" + offset;
        fail(verbose);
        return violationLevel();
    }

    protected final void failPredictionWithSetback(
            final String checkName,
            final float offset,
            final int setbackVl) {
        final int currentVl = failPrediction(checkName, offset);
        if (currentVl >= Math.max(1, setbackVl)) {
            requestSetback();
        }
    }

    protected final void failPredictionWithImmediateSetback(
            final String checkName,
            final float offset) {
        failPrediction(checkName, offset);
        requestSetback();
    }

    protected final void requestSetback() {
        player.entityContext.serverPlayerMovementSyncComponent.requestCorrection();
    }

    private boolean shouldDoFail() {
        return !player.entityContext.unloadedChunkTimerComponent.insideUnloadedChunk
                && !player.getTeleportUtil().isTeleporting()
                && player.entityContext.playerLoadingScreenComponent.ticksSinceChange > 5
                && player.entityContext.blockSource.isChunkLoadedAt(
                        player.entityContext.stateVectorComponent.getPosition().x,
                        player.entityContext.stateVectorComponent.getPosition().z);
    }
}
