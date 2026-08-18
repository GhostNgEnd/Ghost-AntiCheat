package ac.ghost.anticheat.check.impl.prediction;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.network.protocol.types.AuthInputAction;
import cn.nukkit.network.protocol.ProtocolInfo;


@CheckInfo(name = "Phase")
public final class Phase extends PredictionOffsetCheck {
    public Phase(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final float offset) {
        if (isServerVelocityTick()
                || !canCheck(offset, Ghost.getConfig().predictionThreshold())) {
            return;
        }

        if (player.getSession().protocol < ProtocolInfo.v1_21_50) {
            
            
            
            
            if (player.entityContext.verticalCollisionFlagComponent.isPresent()
                    || player.entityContext.horizontalCollisionFlagComponent
                    .isPresent()) {
                failPredictionWithSetback(
                        "Phase", offset, Ghost.getConfig().phaseSetbackVl());
            }
            return;
        }

        final boolean claimedHorizontal = player.entityContext.playerActionComponent.actions()
                .contains(AuthInputAction.HORIZONTAL_COLLISION);
        final boolean claimedVertical = player.entityContext.playerActionComponent.actions()
                .contains(AuthInputAction.VERTICAL_COLLISION);
        if (claimedVertical != player.entityContext.verticalCollisionFlagComponent.isPresent()
                || claimedHorizontal != player.entityContext.horizontalCollisionFlagComponent.isPresent()) {
            failPredictionWithSetback(
                    "Phase", offset, Ghost.getConfig().phaseSetbackVl());
        }
    }
}
