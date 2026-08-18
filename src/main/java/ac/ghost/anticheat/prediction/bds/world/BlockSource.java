package ac.ghost.anticheat.prediction.bds.world;

import ac.ghost.anticheat.collision.util.CuboidBlockIterator;
import ac.ghost.anticheat.compensated.world.PacketVisibleChunkCache;
import ac.ghost.anticheat.data.FluidState;
import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.prediction.model.CollisionShapeEntry;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.prediction.bds.world.block.BlockType;
import ac.ghost.anticheat.util.math.Box;
import ac.ghost.anticheat.util.math.Mutable;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.ArrayList;
import java.util.List;








public final class BlockSource {
    private final GhostPlayer player;
    private final PacketVisibleChunkCache chunks;

    public BlockSource(final GhostPlayer player,
                       final PacketVisibleChunkCache chunks) {
        this.player = player;
        this.chunks = chunks;
    }

    public GhostPlayer player() {
        return this.player;
    }

    public PacketVisibleChunkCache packetVisibleChunks() {
        return this.chunks;
    }

    public Level getLevel() {
        return this.chunks.getLevel();
    }

    public int getDimension() {
        return this.chunks.getDimension();
    }

    public void setDimension(final int dimension) {
        this.chunks.setDimension(dimension);
    }

    public void clearChunks() {
        this.chunks.clearChunks();
    }

    public boolean isChunkLoaded(final int blockX, final int blockZ) {
        return this.chunks.isChunkLoaded(blockX, blockZ);
    }

    public boolean isChunkLoadedAt(final float x, final float z) {
        return this.chunks.isChunkLoadedAt(x, z);
    }

    public boolean hasChunksAt(final int minX, final int minZ,
                               final int maxX, final int maxZ) {
        return this.chunks.hasChunksAt(minX, minZ, maxX, maxZ);
    }

    public int getMinY() {
        return this.chunks.getMinY();
    }

    public int getHeightY() {
        return this.chunks.getHeightY();
    }

    public BlockLegacy getBlockState(final Mutable position, final int layer) {
        return this.chunks.getBlockState(position, layer);
    }

    public BlockLegacy getBlockState(final BlockVector3 position, final int layer) {
        return this.chunks.getBlockState(position, layer);
    }

    public BlockLegacy getBlockState(final int x, final int y, final int z,
                                         final int layer) {
        return this.chunks.getBlockState(x, y, z, layer);
    }

    public Block getBlockAt(final int x, final int y, final int z) {
        return this.chunks.getBlockAt(x, y, z);
    }

    public int getRawBlockAt(final int x, final int y, final int z, final int layer) {
        return this.chunks.getRawBlockAt(x, y, z, layer);
    }

    public void updateBlock(final BlockVector3 position, final int layer,
                            final int networkId) {
        this.chunks.updateBlock(position, layer, networkId);
    }

    public void updateLegacyBlock(final BlockVector3 position, final int layer,
                                  final int legacyFullId) {
        this.chunks.updateLegacyBlock(position, layer, legacyFullId);
    }

    public void updateLegacyBlock(final int x, final int y, final int z,
                                  final int layer, final int legacyFullId) {
        this.chunks.updateLegacyBlock(x, y, z, layer, legacyFullId);
    }

    public CompoundTag getBlockEntityTag(final int x, final int y, final int z) {
        return this.chunks.getBlockEntityTag(x, y, z);
    }

    public void updateBlockEntityTag(final int x, final int y, final int z,
                                     final CompoundTag tag) {
        this.chunks.updateBlockEntityTag(x, y, z, tag);
    }

    public FluidState getFluidState(final BlockVector3 position) {
        return this.getFluidState(position.getX(), position.getY(), position.getZ());
    }

    public FluidState getFluidState(final Mutable position) {
        return this.getFluidState(position.getX(), position.getY(), position.getZ());
    }

    public FluidState getFluidState(final int x, final int y, final int z) {
        if (this.getBlockState(x, y, z, 1).getBlock().isWater()) {
            return new FluidState(FluidState.FluidType.WATER, 8.0F / 9.0F, 8);
        }

        final Block state = this.getBlockState(x, y, z, 0).getBlock();
        final boolean water = state.isWater();
        if (!water && !Block.isLava(state.getId())) {
            return new FluidState(FluidState.FluidType.EMPTY, 0.0F, 0);
        }

        final FluidState.FluidType fluid = water
                ? FluidState.FluidType.WATER : FluidState.FluidType.LAVA;
        final int rawLevel = state.getDamage();
        if (rawLevel == 0 || rawLevel >= 8) {
            return new FluidState(fluid, 8.0F / 9.0F, rawLevel);
        }
        return new FluidState(fluid, (8.0F - rawLevel) / 9.0F, rawLevel);
    }

    public boolean noCollision(final Box box) {
        return this.collectColliders(box).isEmpty();
    }

    public List<Box> collectColliders(final Box box) {
        final List<CollisionShapeEntry> entries = collectColliderEntries(box);
        final List<Box> result = new ArrayList<>(entries.size());
        for (final CollisionShapeEntry entry : entries) {
            result.add(entry.shape());
        }
        return result;
    }

    





    public List<CollisionShapeEntry> collectColliderEntries(final Box box) {
        final List<CollisionShapeEntry> result = new ArrayList<>();
        final CuboidBlockIterator iterator = CuboidBlockIterator.iterator(box);
        while (iterator.step()) {
            final int x = iterator.getX();
            final int y = iterator.getY();
            final int z = iterator.getZ();
            if (!this.isChunkLoaded(x, z)) {
                continue;
            }

            final BlockLegacy state = this.getBlockState(x, y, z, 0);
            for (final Box shape : state.findCollision(
                    this.player, new BlockVector3(x, y, z), box, true)) {
                result.add(new CollisionShapeEntry(shape, state));
            }
        }
        return result;
    }

    








    public Box getTallestCollisionShape(
            final Box intersectTestBox,
            final float[] actualSurfaceOffset,
            final boolean withUnloadedChunks,
            final GhostPlayer collisionShapeContext) {
        if (actualSurfaceOffset != null && actualSurfaceOffset.length != 0) {
            actualSurfaceOffset[0] = 0.0F;
        }

        final Box invalidResult = Box.invalid();
        if (!isValidSupportQuery(intersectTestBox)) {
            return invalidResult;
        }

        final Vec3 matchingHeightTarget = new Vec3(
                (intersectTestBox.minX + intersectTestBox.maxX) * 0.5F,
                (intersectTestBox.minY + intersectTestBox.maxY) * 0.5F,
                (intersectTestBox.minZ + intersectTestBox.maxZ) * 0.5F);
        final GhostPlayer shapeContext = collisionShapeContext == null
                ? this.player : collisionShapeContext;
        Box tallest = invalidResult;
        float matchingHeightDistanceSquared = Float.MAX_VALUE;

        




        if (withUnloadedChunks) {
            final int minChunkX = floor(intersectTestBox.minX) >> 4;
            final int maxChunkX = (floor(intersectTestBox.maxX) + 16) >> 4;
            final int minChunkZ = floor(intersectTestBox.minZ) >> 4;
            final int maxChunkZ = (floor(intersectTestBox.maxZ) + 16) >> 4;

            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    final int blockX = chunkX << 4;
                    final int blockZ = chunkZ << 4;
                    if (this.isChunkLoaded(blockX, blockZ)) {
                        continue;
                    }

                    final Box candidate = new Box(
                            blockX, -100000.0F, blockZ,
                            blockX + 16.0F, 100000.0F, blockZ + 16.0F);
                    final float candidateDistanceSquared = squaredDistance(
                            matchingHeightTarget,
                            (candidate.minX + candidate.maxX) * 0.5F,
                            (candidate.minY + candidate.maxY) * 0.5F,
                            (candidate.minZ + candidate.maxZ) * 0.5F);
                    final boolean selected = shouldReplaceTallest(
                            candidate, candidateDistanceSquared,
                            tallest, matchingHeightDistanceSquared);
                    if (selected) {
                        tallest = candidate;
                        matchingHeightDistanceSquared =
                                candidateDistanceSquared;
                    }
                }
            }
        }

        
        if (this.getMinY() > intersectTestBox.maxY) {
            final float currentMinX = tallest.isValid()
                    ? tallest.minX : Float.MAX_VALUE;
            final float currentMinZ = tallest.isValid()
                    ? tallest.minZ : Float.MAX_VALUE;
            final float currentMaxX = tallest.isValid()
                    ? tallest.maxX : -Float.MAX_VALUE;
            final float currentMaxZ = tallest.isValid()
                    ? tallest.maxZ : -Float.MAX_VALUE;
            tallest = new Box(
                    Math.min(intersectTestBox.minX, currentMinX),
                    -Float.MAX_VALUE,
                    Math.min(intersectTestBox.minZ, currentMinZ),
                    Math.max(intersectTestBox.maxX, currentMaxX),
                    this.getMinY() - 40.0F,
                    Math.max(intersectTestBox.maxZ, currentMaxZ));
        }

        




        final int minX = floor(intersectTestBox.minX);
        final int maxX = floor(intersectTestBox.maxX + 1.0F);
        final int minZ = floor(intersectTestBox.minZ);
        final int maxZ = floor(intersectTestBox.maxZ + 1.0F);
        final int minY = floor(matchingHeightTarget.y - 1.0F);
        final int maxYExclusive = ceil(matchingHeightTarget.y + 1.0F);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!this.isChunkLoaded(x, z)) {
                    continue;
                }

                for (int y = maxYExclusive - 1; y >= minY; y--) {
                    final BlockVector3 blockPosition =
                            new BlockVector3(x, y, z);
                    final BlockLegacy block =
                            this.getBlockState(blockPosition, 0);
                    final BlockType.TallestCollisionShapeUpdate update =
                            BlockType.updateTallestCollisionShape(
                                    block,
                                    shapeContext,
                                    blockPosition,
                                    intersectTestBox,
                                    tallest,
                                    matchingHeightTarget,
                                    matchingHeightDistanceSquared);

                    if (update.selected()) {
                        tallest = update.shape();
                        matchingHeightDistanceSquared =
                                update.matchingHeightDistanceSquared();
                    }
                }
            }
        }

        return tallest;
    }

    private static boolean shouldReplaceTallest(
            final Box candidate,
            final float candidateDistanceSquared,
            final Box current,
            final float currentDistanceSquared) {
        if (!current.isValid() || candidate.maxY > current.maxY) {
            return true;
        }
        return candidate.maxY == current.maxY
                && candidateDistanceSquared < currentDistanceSquared;
    }

    private static float squaredDistance(
            final Vec3 from, final float x, final float y, final float z) {
        final float deltaX = from.x - x;
        final float deltaY = from.y - y;
        final float deltaZ = from.z - z;
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    private static boolean isValidSupportQuery(final Box query) {
        return query != null
                && query.minX < query.maxX
                && query.minZ < query.maxZ
                && query.minY == query.maxY;
    }

    private static int floor(final float value) {
        return (int) Math.floor(value);
    }

    private static int ceil(final float value) {
        return (int) Math.ceil(value);
    }

}
