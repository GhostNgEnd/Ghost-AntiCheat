package ac.ghost.anticheat.prediction.bds.system.player;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.AbilitiesComponent;
import ac.ghost.anticheat.prediction.bds.component.AbilitiesRequestComponent;


public final class ProcessRequestAbilitiesSystem {
    private ProcessRequestAbilitiesSystem() {
    }

    public static void tick(final GhostPlayer player) {
        final AbilitiesRequestComponent requests =
                player.entityContext.abilitiesRequestComponent;
        if (!requests.isPresent()) {
            return;
        }

        final AbilitiesComponent abilities = player.entityContext.abilitiesComponent;
        for (final AbilitiesRequestComponent.Request request
                : requests.requests()) {
            if (request.type() == AbilitiesRequestComponent.BOOLEAN) {
                abilities.setBoolean(request.ability(),
                        request.booleanValue());
            } else if (request.type() == AbilitiesRequestComponent.FLOAT) {
                abilities.setFloat(request.ability(), request.floatValue());
            }
        }

        
        
        requests.clear();
    }
}
