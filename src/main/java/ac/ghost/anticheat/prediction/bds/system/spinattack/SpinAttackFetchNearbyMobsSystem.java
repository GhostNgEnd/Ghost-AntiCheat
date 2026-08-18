package ac.ghost.anticheat.prediction.bds.system.spinattack;

import ac.ghost.anticheat.compensated.cache.entity.EntityCache;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Box;

import java.util.List;


public final class SpinAttackFetchNearbyMobsSystem {
    private SpinAttackFetchNearbyMobsSystem() {
    }

    public static void tick(final GhostPlayer player,
                            final Box previousBox,
                            final Box currentBox) {
        final Box sweptBox = previousBox.union(currentBox);
        final List<EntityCache> entities =
                player.entityRegistry.entities().values().stream().toList();

        boolean hitNearbyMob = false;
        for (final EntityCache entity : entities) {
            if (entity.getCurrent() == null) {
                continue;
            }
            if (entity.getCurrent().getBoundingBox().intersects(sweptBox)) {
                hitNearbyMob = true;
                break;
            }
        }

        player.entityContext.spinAttackResultsComponent.set(
                !entities.isEmpty(), hitNearbyMob, player.entityContext.horizontalCollisionFlagComponent.isPresent());
    }
}
