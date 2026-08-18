package ac.ghost.anticheat.prediction.bds.system.player;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.AbilitiesComponent;


public final class FlyTriggerIntentSystem {
    private FlyTriggerIntentSystem() {
    }

    public static void tick(final GhostPlayer player) {
        player.entityContext.permissionFlyFlagComponent.setPresent(
                player.entityContext.abilitiesComponent.getBoolean(AbilitiesComponent.MAY_FLY));
    }
}
