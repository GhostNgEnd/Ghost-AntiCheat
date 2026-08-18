package ac.ghost.anticheat.check.impl.prediction;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.player.GhostPlayer;


@CheckInfo(name = "AntiKB")
public final class AntiKB extends PredictionOffsetCheck {
    public AntiKB(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final float offset) {
        if (!isServerVelocityTick()) {
            return;
        }

        final float threshold = Ghost.getConfig().knockbackThreshold();
        if (!canCheck(offset, threshold)) {
            return;
        }

        failPredictionWithSetback(
                "AntiKB", offset, Ghost.getConfig().knockbackSetbackVl());
    }
}
