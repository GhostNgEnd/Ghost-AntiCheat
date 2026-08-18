package ac.ghost.anticheat.collision.bds.system;

import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.component.BlockCollisionEvaluationQueueComponent;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.BlockVector3;

import java.util.List;


public final class BlockCollisionsSystem {
    
    public static final class BlockCollisionResolutionVectorComponent {
        private Vec3 value = Vec3.ZERO.clone();
        private boolean present;

        public boolean isPresent() {
            return present;
        }

        public Vec3 value() {
            return value.clone();
        }

        public void set(final Vec3 value) {
            this.value = value == null ? Vec3.ZERO.clone() : value.clone();
            this.present = true;
        }

        public void clear() {
            this.value = Vec3.ZERO.clone();
            this.present = false;
        }
    }

    private BlockCollisionsSystem() {}
    public static void run(final GhostPlayer player) {
        final List<BlockCollisionEvaluationQueueComponent.Entry> entries =
                player.entityContext.blockCollisionEvaluationQueueComponent.drain();
        if (entries.isEmpty() || player.entityContext.blockCollisionResolutionVectorComponent.isPresent()) return;

                final Box actor = player.entityContext.aabbShapeComponent.getAABB();
        final int currentDimension = player.packetVisibleChunkCache.getDimension();
        for (final BlockCollisionEvaluationQueueComponent.Entry entry : entries) {
            if (entry.dimension() != currentDimension) continue;
            final BlockLegacy state = player.entityContext.localConstBlockSourceFactoryComponent.create()
                    .getBlockState(entry.x(), entry.y(), entry.z(), 0);
            final BlockVector3 pos = new BlockVector3(entry.x(), entry.y(), entry.z());
            final Box cell = new Box(entry.x(), entry.y(), entry.z(),
                    entry.x() + 1.0F, entry.y() + 1.0F, entry.z() + 1.0F);
            float topY = -Float.MAX_VALUE;
            for (final Box shape : state.findCollision(player, pos, cell, true)) {
                if (shape.intersects(actor)) topY = Math.max(topY, shape.maxY);
            }
            if (topY == -Float.MAX_VALUE) continue;
            final float dy = topY - actor.minY;
            if (dy <= 0.0F) continue;
            player.entityContext.blockCollisionResolutionVectorComponent.set(new Vec3(0.0F, dy, 0.0F));
            return;
        }
    }
}
