package ac.ghost.anticheat.prediction.bds.system.block;

import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.world.LocalConstBlockSource;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.BlockVector3;


public final class CurrentlyStandingOnBlockSystem {
    private static final float SUPPORT_QUERY_OFFSET =
            Float.intBitsToFloat(0xBE4CCCCD); 

    private CurrentlyStandingOnBlockSystem() {
    }

    public static void tick(final GhostPlayer player) {
        final Box actorBox = player.entityContext.aabbShapeComponent.getAABB();
        final float queryY = actorBox.minY + SUPPORT_QUERY_OFFSET;

        
        
        final Box query = new Box(
                actorBox.minX, queryY, actorBox.minZ,
                actorBox.maxX, queryY, actorBox.maxZ);
        final LocalConstBlockSource source =
                player.entityContext.localConstBlockSourceFactoryComponent.create();
        final Box tallestCollisionShape = source.getTallestCollisionShape(
                query, null, true, player);

        final Vec3 lookup = isValid(tallestCollisionShape)
                ? new Vec3(
                        tallestCollisionShape.minX,
                        tallestCollisionShape.minY,
                        tallestCollisionShape.minZ)
                : player.entityContext.stateVectorComponent.getPosition().clone();
        final BlockVector3 blockPosition = floor(lookup);
        final BlockVector3 blockAbovePosition = new BlockVector3(
                blockPosition.getX(),
                blockPosition.getY() + 1,
                blockPosition.getZ());

        final BlockLegacy blockAtCollisionShape =
                source.getBlockState(blockPosition, 0);
        final BlockLegacy blockAboveCollisionShape =
                source.getBlockState(blockAbovePosition, 0);
        player.entityContext.currentlyStandingOnBlockComponent.set(
                blockAtCollisionShape, blockAboveCollisionShape);
    }

    private static boolean isValid(final Box shape) {
        return shape != null && shape.isValid();
    }

    private static BlockVector3 floor(final Vec3 position) {
        return new BlockVector3(
                (int) Math.floor(position.x),
                (int) Math.floor(position.y),
                (int) Math.floor(position.z));
    }
}
