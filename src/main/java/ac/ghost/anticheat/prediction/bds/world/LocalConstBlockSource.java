package ac.ghost.anticheat.prediction.bds.world;

import ac.ghost.anticheat.data.FluidState;
import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.model.CollisionShapeEntry;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Mutable;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.List;


public final class LocalConstBlockSource {
    private final BlockSource source;

    public LocalConstBlockSource(final BlockSource source) {
        if (source == null) {
            throw new IllegalArgumentException("BlockSource cannot be null");
        }
        this.source = source;
    }

    public Level getLevel() {
        return this.source.getLevel();
    }

    public int getDimension() {
        return this.source.getDimension();
    }

    public boolean isChunkLoaded(final int blockX, final int blockZ) {
        return this.source.isChunkLoaded(blockX, blockZ);
    }

    public boolean isChunkLoadedAt(final float x, final float z) {
        return this.source.isChunkLoadedAt(x, z);
    }

    public boolean hasChunksAt(final int minX, final int minZ,
                               final int maxX, final int maxZ) {
        return this.source.hasChunksAt(minX, minZ, maxX, maxZ);
    }

    public int getMinY() {
        return this.source.getMinY();
    }

    public int getHeightY() {
        return this.source.getHeightY();
    }

    public BlockLegacy getBlockState(final Mutable position,
                                         final int layer) {
        return this.source.getBlockState(position, layer);
    }

    public BlockLegacy getBlockState(final BlockVector3 position,
                                         final int layer) {
        return this.source.getBlockState(position, layer);
    }

    public BlockLegacy getBlockState(final int x, final int y,
                                         final int z, final int layer) {
        return this.source.getBlockState(x, y, z, layer);
    }

    public Block getBlockAt(final int x, final int y, final int z) {
        return this.source.getBlockAt(x, y, z);
    }

    public int getRawBlockAt(final int x, final int y, final int z,
                             final int layer) {
        return this.source.getRawBlockAt(x, y, z, layer);
    }

    public CompoundTag getBlockEntityTag(final int x, final int y,
                                         final int z) {
        return this.source.getBlockEntityTag(x, y, z);
    }

    public FluidState getFluidState(final BlockVector3 position) {
        return this.source.getFluidState(position);
    }

    public FluidState getFluidState(final Mutable position) {
        return this.source.getFluidState(position);
    }

    public FluidState getFluidState(final int x, final int y, final int z) {
        return this.source.getFluidState(x, y, z);
    }

    public boolean noCollision(final Box box) {
        return this.source.noCollision(box);
    }

    public List<Box> collectColliders(final Box box) {
        return this.source.collectColliders(box);
    }

    public List<CollisionShapeEntry> collectColliderEntries(final Box box) {
        return this.source.collectColliderEntries(box);
    }


    public Box getTallestCollisionShape(
            final Box intersectTestBox,
            final float[] actualSurfaceOffset,
            final boolean withUnloadedChunks,
            final GhostPlayer collisionShapeContext) {
        return this.source.getTallestCollisionShape(
                intersectTestBox, actualSurfaceOffset,
                withUnloadedChunks, collisionShapeContext);
    }

}
