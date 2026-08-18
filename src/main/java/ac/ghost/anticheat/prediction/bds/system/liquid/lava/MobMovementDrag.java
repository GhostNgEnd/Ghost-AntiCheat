package ac.ghost.anticheat.prediction.bds.system.liquid.lava;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.system.restitution.ApplyGravityWithBounceCorrection;

public final class MobMovementDrag {
    private MobMovementDrag() {
    }

    public static void tick(final GhostPlayer player) {
        player.entityContext.stateVectorComponent.setDelta(player.entityContext.stateVectorComponent.getDelta().multiply(0.5F));
        final float gravity = player.entityContext.mobEffectsComponent.effectiveGravity(player.entityContext.stateVectorComponent.getDelta());
        if (gravity != 0.0F) {
            final float gravityDelta = ApplyGravityWithBounceCorrection.resolveGravityDelta(
                    player, -gravity / 4.0F);
            player.entityContext.stateVectorComponent.setDelta(player.entityContext.stateVectorComponent.getDelta().add(0.0F, gravityDelta, 0.0F));
        } else {
            ApplyGravityWithBounceCorrection.clear(player);
        }
    }
}
