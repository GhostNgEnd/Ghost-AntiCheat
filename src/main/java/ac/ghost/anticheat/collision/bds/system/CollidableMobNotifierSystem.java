package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.compensated.cache.entity.EntityCache;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.AABBShapeComponent;
import ac.ghost.anticheat.util.math.Box;







public final class CollidableMobNotifierSystem {
    private static final float NOTIFICATION_RADIUS = 2.0F;

    private CollidableMobNotifierSystem() {
    }

    public static void run(final GhostPlayer player) {
        final Box query = player.entityContext.aabbShapeComponent.getAABB().expand(NOTIFICATION_RADIUS);
        final int playerDimension = player.entityContext.blockSource.getDimension();
        boolean collidableMobNear = false;

        for (final EntityCache candidate : player.entityRegistry.entities().values()) {
            if (candidate == null
                    || candidate.dimension() != playerDimension
                    || candidate.currentState() == null
                    || !candidate.collidableMobFlagComponent().isPresent()) {
                continue;
            }

            candidate.refreshAABBShapeComponent();
            final AABBShapeComponent shape = candidate.aabbShapeComponent();
            if (!shape.isPresent()) {
                continue;
            }

            if (query.intersects(shape.getAABB())) {
                collidableMobNear = true;
                break;
            }
        }

        player.entityContext.collidableMobNearFlagComponent.setPresent(collidableMobNear);
    }
}
