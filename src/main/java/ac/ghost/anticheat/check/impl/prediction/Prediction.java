package ac.ghost.anticheat.check.impl.prediction;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.impl.movement.NoSlow;
import ac.ghost.anticheat.player.GhostPlayer;


@CheckInfo(name = "Prediction")
public final class Prediction extends PredictionOffsetCheck {
    public Prediction(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final float offset) {
        if (isServerVelocityTick()
                || !canCheck(offset, Ghost.getConfig().predictionThreshold())) {
            return;
        }

        
        
        
        if (NoSlow.isNoSlowSample(player)) {
            return;
        }

        final float threshold = Ghost.getConfig().predictionThreshold();
        PredictionDebugLogger.logFailure(player, offset, threshold);
        failPredictionWithImmediateSetback("Prediction", offset);
    }
}
