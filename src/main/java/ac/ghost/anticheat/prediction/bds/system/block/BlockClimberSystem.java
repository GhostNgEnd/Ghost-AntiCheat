package ac.ghost.anticheat.prediction.bds.system.block;

import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.world.LocalConstBlockSource;
import ac.ghost.anticheat.util.math.Box;
import cn.nukkit.entity.Entity;







public final class BlockClimberSystem {
    private BlockClimberSystem() {
    }

    public static void tick(final GhostPlayer player) {
        final Box aabb = player.entityContext.aabbShapeComponent.getAABB();
        final LocalConstBlockSource source =
                player.entityContext.localConstBlockSourceFactoryComponent.create();
        final int inY = floor(aabb.minY);
        final int overY = floor(aabb.minY - 1.0F);

        player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_IN_SCAFFOLDING,
                anyScaffolding(source, aabb, inY));
        player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_OVER_SCAFFOLDING,
                anyScaffolding(source, aabb, overY));
        player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_IN_ASCENDABLE_BLOCK,
                anyAscendable(player, source, aabb, inY));
        player.entityContext.actorDataFlagComponent.set(Entity.DATA_FLAG_OVER_DESCENDABLE_BLOCK,
                anyAscendable(player, source, aabb, overY));
    }

    private static boolean anyScaffolding(final LocalConstBlockSource source,
                                          final Box aabb,
                                          final int y) {
        final int minX = floor(aabb.minX);
        final int maxX = floor(aabb.maxX);
        final int minZ = floor(aabb.minZ);
        final int maxZ = floor(aabb.maxZ);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!source.isChunkLoaded(x, z)) {
                    continue;
                }
                if (source.getBlockState(x, y, z, 0).isScaffolding()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean anyAscendable(final GhostPlayer player,
                                         final LocalConstBlockSource source,
                                         final Box aabb,
                                         final int y) {
        final int minX = floor(aabb.minX);
        final int maxX = floor(aabb.maxX);
        final int minZ = floor(aabb.minZ);
        final int maxZ = floor(aabb.maxZ);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!source.isChunkLoaded(x, z)) {
                    continue;
                }

                final BlockLegacy state = source.getBlockState(x, y, z, 0);
                if (state.isPowderSnow()) {
                    if (player.entityContext.canStandOnSnowFlagComponent.isPresent()
                            || player.entityContext.hasLightweightFamilyFlagComponent.isPresent()) {
                        return true;
                    }
                    continue;
                }

                if (!state.isScaffolding()) {
                    continue;
                }

                final BlockLegacy below = source.getBlockState(x, y - 1, z, 0);
                if (!below.isAir() && !below.isWater()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int floor(final float value) {
        final int truncated = (int) value;
        return value < truncated ? truncated - 1 : truncated;
    }
}
