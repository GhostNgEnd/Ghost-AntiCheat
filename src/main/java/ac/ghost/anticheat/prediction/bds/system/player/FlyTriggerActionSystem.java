package ac.ghost.anticheat.prediction.bds.system.player;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.AbilitiesComponent;


public final class FlyTriggerActionSystem {
    private FlyTriggerActionSystem() {
    }

    






    public static void tick(final GhostPlayer player,
                            final boolean startFlying,
                            final boolean stopFlying) {
        if (startFlying && player.entityContext.permissionFlyFlagComponent.isPresent()) {
            player.entityContext.abilitiesRequestComponent.appendBoolean(
                    AbilitiesComponent.FLYING, true);
        } else if (stopFlying) {
            player.entityContext.abilitiesRequestComponent.appendBoolean(
                    AbilitiesComponent.FLYING, false);
        }
    }
}
