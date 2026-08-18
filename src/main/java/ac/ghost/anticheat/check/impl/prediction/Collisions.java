package ac.ghost.anticheat.check.impl.prediction;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.player.GhostPlayer;


@CheckInfo(name = "Collisions")
public final class Collisions extends PredictionOffsetCheck {
    public Collisions(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final float offset) {
        final float threshold = Ghost.getConfig().predictionThreshold();
        if (isServerVelocityTick() || !canCheck(offset, threshold)) {
            return;
        }

        if (player.entityContext.serverPlayerCurrentMovementComponent.getUnvalidatedTickEnd()
                .distanceTo(player.entityContext.stateVectorComponent.getDelta()) < threshold) {
            failPredictionWithSetback(
                    "Collisions", offset, Ghost.getConfig().collisionsSetbackVl());
        }
    }
}
