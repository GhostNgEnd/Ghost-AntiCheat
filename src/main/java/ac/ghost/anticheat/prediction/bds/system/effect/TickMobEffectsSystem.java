package ac.ghost.anticheat.prediction.bds.system.effect;

import ac.ghost.anticheat.data.vanilla.StatusEffect;
import ac.ghost.anticheat.player.GhostPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public final class TickMobEffectsSystem {
    private TickMobEffectsSystem() {
    }

    public static void tick(final GhostPlayer player) {
        final List<Integer> expired = new ArrayList<>();
        for (final Map.Entry<Integer, StatusEffect> entry
                : player.entityContext.mobEffectsComponent.view().entrySet()) {
            entry.getValue().tick();
            if (entry.getValue().getDuration() == 0) {
                expired.add(entry.getKey());
            }
        }
        for (final int effectId : expired) {
            player.entityContext.mobEffectsComponent.remove(effectId);
        }
    }
}
