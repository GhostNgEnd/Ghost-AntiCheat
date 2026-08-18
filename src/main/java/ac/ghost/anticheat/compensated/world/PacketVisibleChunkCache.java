package ac.ghost.anticheat.compensated.world;

import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.data.block.NetworkBlockState;
import ac.ghost.anticheat.data.block.NetworkBlockStateRegistry;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Mutable;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@RequiredArgsConstructor
@Setter
@Getter
public class PacketVisibleChunkCache {
    private final GhostPlayer player;

    
    private final Long2ObjectMap<CachedChunk> chunks = new Long2ObjectOpenHashMap<>();

    private int dimension = Level.DIMENSION_OVERWORLD;
    private long revision;

    private final LongSet exemptedChunks = new LongOpenHashSet();
    private int viewDistance = 16;
    private long lastChunkClean = Long.MIN_VALUE;

    public Level getLevel() {
        return this.player.getSession().getLevel();
    }

    public void setDimension(final int dimension) {
        if (this.dimension != dimension) {
            this.dimension = dimension;
            this.player.entityContext.blockCollisionEvaluationQueueComponent.clear();
            this.revision++;
        }
    }

    public void setViewDistance(int viewDistance) {
        
        this.viewDistance = Math.max(1, viewDistance + 1);
    }

    public void cleanChunksAtPlayerPosition() {
        if (this.player == null) {
            return;
        }

        final int playerChunkX = (int) Math.floor(this.player.entityContext.stateVectorComponent.getPosition().x) >> 4;
        final int playerChunkZ = (int) Math.floor(this.player.entityContext.stateVectorComponent.getPosition().z) >> 4;
        final long playerChunk = chunkKey(playerChunkX, playerChunkZ);
        if (playerChunk == this.lastChunkClean) {
            return;
        }

        this.lastChunkClean = playerChunk;
        this.yeetOutOfRangeChunks();
    }

    




    public void retainNukkitUsedChunks(final long[] nukkitChunkHashes) {
        final java.util.HashSet<Long> retained =
                new java.util.HashSet<>(nukkitChunkHashes.length * 2 + 1);
        for (long hash : nukkitChunkHashes) {
            retained.add(chunkKey(Level.getHashX(hash), Level.getHashZ(hash)));
        }

        final int sizeBefore = this.chunks.size();
        this.chunks.keySet().removeIf(key -> {
            if (retained.contains(key) || this.exemptedChunks.contains(key)) {
                return false;
            }
            final int chunkX = (int) key;
            final int chunkZ = (int) (key >> 32);
            return this.isOutOfRadius(chunkX << 4, chunkZ << 4);
        });
        if (this.chunks.size() != sizeBefore) {
            this.revision++;
        }
    }

    public void yeetOutOfRangeChunks() {
        final int sizeBefore = this.chunks.size();
        this.chunks.keySet().removeIf(key -> {
            final int chunkX = (int) key;
            final int chunkZ = (int) (key >> 32);
            final boolean inView = !this.isOutOfRadius(chunkX << 4, chunkZ << 4);

            
            
            
            if (this.exemptedChunks.contains(key)) {
                if (inView) {
                    this.exemptedChunks.remove(key);
                }
                return false;
            }

            
            
            
            if (isNukkitUsedChunk(key)) {
                return false;
            }

            return !inView;
        });
        if (this.chunks.size() != sizeBefore) {
            this.revision++;
        }
    }

    public boolean isOutOfRadius(int blockX, int blockZ) {
        if (this.player == null) {
            return false;
        }

        final int chunkX = blockX >> 4;
        final int chunkZ = blockZ >> 4;
        final int playerChunkX = (int) Math.floor(this.player.entityContext.serverPlayerCurrentMovementComponent.getUnvalidatedPosition().x) >> 4;
        final int playerChunkZ = (int) Math.floor(this.player.entityContext.serverPlayerCurrentMovementComponent.getUnvalidatedPosition().z) >> 4;
        return !isChunkInView(this.viewDistance, chunkX, chunkZ, playerChunkX, playerChunkZ);
    }

    
    
    public static boolean isChunkInView(int viewDistance, int chunkX, int chunkZ,
                                 int playerChunkX, int playerChunkZ) {
        final long dx = Math.abs((long) playerChunkX - chunkX);
        final long dz = Math.abs((long) playerChunkZ - chunkZ);

        final long maxCoordinate = viewDistance + 1L;
        if (dx > maxCoordinate || dz > maxCoordinate) {
            return false;
        }

        final long distanceSquared = dx * dx + dz * dz;
        final float threshold = viewDistance + 1.5F + 1.7320508F;
        return distanceSquared < threshold * threshold;
    }

    private boolean isNukkitUsedChunk(long key) {
        if (this.player == null || this.player.getSession() == null) {
            return false;
        }
        final int chunkX = (int) key;
        final int chunkZ = (int) (key >> 32);
        return this.player.getSession().usedChunks.containsKey(Level.chunkHash(chunkX, chunkZ));
    }

    public void put(int x, int z, CachedChunk chunk) {
        if (chunk == null) {
            return;
        }

        final long key = chunkKey(x, z);
        this.chunks.put(key, chunk);
        this.revision++;
        updateChunkExemption(key, x, z);
    }

    private void updateChunkExemption(long key, int chunkX, int chunkZ) {
        if (this.isOutOfRadius(chunkX << 4, chunkZ << 4)) {
            this.exemptedChunks.add(key);
        } else {
            this.exemptedChunks.remove(key);
        }
    }

    public void clearChunks() {
        if (!this.chunks.isEmpty()) {
            this.revision++;
        }
        this.chunks.clear();
        this.player.entityContext.blockCollisionEvaluationQueueComponent.clear();
        this.exemptedChunks.clear();
        this.lastChunkClean = Long.MIN_VALUE;
    }

    public void removeFromCache(int x, int z) {
        final long key = chunkKey(x, z);
        if (this.chunks.remove(key) != null) {
            this.revision++;
        }
        this.exemptedChunks.remove(key);
    }

    public boolean isChunkLoaded(int blockX, int blockZ) {
        return getChunk(blockX >> 4, blockZ >> 4) != null;
    }

    



    public boolean isChunkLoadedAt(float x, float z) {
        return isChunkLoaded((int) Math.floor(x), (int) Math.floor(z));
    }

    public boolean hasChunksAt(int minX, int minZ, int maxX, int maxZ) {
        for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
            for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
                if (getChunk(chunkX, chunkZ) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    public void updateBlock(final BlockVector3 position, int layer, int networkId) {
        updateBlock(position.getX(), position.getY(), position.getZ(), layer, networkId);
    }

    public void updateBlock(int x, int y, int z, int layer, int networkId) {
        final CachedChunk chunk = getChunk(x >> 4, z >> 4);
        if (chunk != null) {
            chunk.setFullBlock(x & 15, y, z & 15, layer, networkId);
            this.revision++;
        }
    }

    
    public void updateLegacyBlock(BlockVector3 position, int layer, int legacyFullId) {
        updateLegacyBlock(position.getX(), position.getY(), position.getZ(), layer, legacyFullId);
    }

    public void updateLegacyBlock(int x, int y, int z, int layer, int legacyFullId) {
        updateBlock(x, y, z, layer, NetworkBlockStateRegistry.networkIdForLegacy(player, legacyFullId));
    }

    
    public void updateBlock(int x, int y, int z, int layer, int blockId, int blockData) {
        final int legacyFullId = blockId << Block.DATA_BITS | blockData & Block.DATA_MASK;
        updateLegacyBlock(x, y, z, layer, legacyFullId);
    }

    public BlockLegacy getBlockState(Mutable vector3i, int layer) {
        return getBlockState(vector3i.getX(), vector3i.getY(), vector3i.getZ(), layer);
    }

    public BlockLegacy getBlockState(BlockVector3 vector3i, int layer) {
        return getBlockState(vector3i.getX(), vector3i.getY(), vector3i.getZ(), layer);
    }

    public BlockLegacy getBlockState(int x, int y, int z, int layer) {
        final int networkId = getRawBlockAt(x, y, z, layer);
        final NetworkBlockState networkState = NetworkBlockStateRegistry.resolve(player, networkId);
        final Block state = getNukkitBlock(networkState, x, y, z, layer);
        final BlockVector3 pos = new BlockVector3(x, y, z);

        return new BlockLegacy(state, networkState, pos, layer);
    }

    public Block getBlockAt(int x, int y, int z) {
        return getBlockState(x, y, z, 0).getBlock();
    }

    private Block getNukkitBlock(NetworkBlockState state, int x, int y, int z, int layer) {
        final Level level = getLevel();
        if (level == null) {
            return Block.get(BlockID.AIR);
        }
        return Block.get(state.blockId(), state.blockData(), level, x, y, z, layer);
    }

    
    public int getRawBlockAt(int x, int y, int z, int layer) {
        final CachedChunk chunk = getChunk(x >> 4, z >> 4);
        if (chunk == null || y < getMinY() || y >= getHeightY()) {
            return NetworkBlockStateRegistry.airNetworkId(player);
        }
        return chunk.getFullBlock(x & 15, y, z & 15, layer);
    }

    
    public int getBlockAt(int x, int y, int z, int layer) {
        return NetworkBlockStateRegistry.resolve(player, getRawBlockAt(x, y, z, layer)).blockId();
    }

    public CompoundTag getBlockEntityTag(int x, int y, int z) {
        final CachedChunk chunk = getChunk(x >> 4, z >> 4);
        return chunk == null ? null : chunk.getBlockEntityTag(x, y, z);
    }

    public void updateBlockEntityTag(int x, int y, int z, CompoundTag tag) {
        final CachedChunk chunk = getChunk(x >> 4, z >> 4);
        if (chunk != null) {
            chunk.setBlockEntityTag(x, y, z, tag);
            this.revision++;
        }
    }

    public CachedChunk getChunk(int chunkX, int chunkZ) {
        return this.chunks.get(chunkKey(chunkX, chunkZ));
    }

    
    public int getMinY() {
        return minYForDimension(this.dimension,
                this.player.getSession().protocol);
    }

    
    public int getHeightY() {
        return maxYForDimension(this.dimension,
                this.player.getSession().protocol);
    }

    public static int minYForDimension(int dimension) {
        return minYForDimension(dimension, ProtocolInfo.CURRENT_PROTOCOL);
    }

    public static int minYForDimension(int dimension, int protocol) {
        return dimension == Level.DIMENSION_OVERWORLD
                && protocol >= ProtocolInfo.v1_18_0 ? -64 : 0;
    }

    public static int maxYForDimension(int dimension) {
        return maxYForDimension(dimension, ProtocolInfo.CURRENT_PROTOCOL);
    }

    public static int maxYForDimension(int dimension, int protocol) {
        if (dimension == 1) {
            return 128;
        }
        if (dimension == 2) {
            return 256;
        }
        return protocol >= ProtocolInfo.v1_18_0 ? 320 : 256;
    }

    public static long chunkKey(int x, int z) {
        return (x & 0xffffffffL) | ((long) z << 32);
    }

    public static final class CachedChunk {
        private final int minY;
        private final int maxY;
        private final int airNetworkId;
        private final NetworkSection[] networkSections;
        private final Long2ObjectMap<CompoundTag> blockEntities = new Long2ObjectOpenHashMap<>();

        private CachedChunk(int minY, int maxY, int airNetworkId, int sectionCount) {
            this.minY = minY;
            this.maxY = maxY;
            this.airNetworkId = airNetworkId;
            this.networkSections = new NetworkSection[Math.max(0, sectionCount)];
        }

        public static CachedChunk network(int minY, int maxY, int sectionCount, int airNetworkId) {
            return new CachedChunk(minY, maxY, airNetworkId, sectionCount);
        }

        public void setNetworkSection(int sectionY, int[][] layers) {
            if (sectionY < 0 || sectionY >= networkSections.length) {
                return;
            }
            networkSections[sectionY] = new NetworkSection(layers, airNetworkId);
        }

        int getFullBlock(int x, int y, int z, int layer) {
            if (layer < 0 || y < minY || y >= maxY) {
                return airNetworkId;
            }
            final int sectionY = (y - minY) >> 4;
            if (sectionY < 0 || sectionY >= networkSections.length) {
                return airNetworkId;
            }
            final NetworkSection section = networkSections[sectionY];
            return section == null ? airNetworkId : section.getFullBlock(x & 15, y & 15, z & 15, layer);
        }

        void setFullBlock(int x, int y, int z, int layer, int networkId) {
            if (layer < 0 || y < minY || y >= maxY) {
                return;
            }
            final int sectionY = (y - minY) >> 4;
            if (sectionY < 0 || sectionY >= networkSections.length) {
                return;
            }

            NetworkSection section = networkSections[sectionY];
            if (section == null) {
                
                
                if (networkId == 0) {
                    return;
                }
                section = NetworkSection.empty(airNetworkId, 2);
                networkSections[sectionY] = section;
            }
            section.setFullBlock(x & 15, y & 15, z & 15, layer, networkId);
        }

        CompoundTag getBlockEntityTag(int x, int y, int z) {
            return blockEntities.get(blockKey(x, y, z));
        }

        void setBlockEntityTag(int x, int y, int z, CompoundTag tag) {
            long key = blockKey(x, y, z);
            if (tag == null) {
                blockEntities.remove(key);
            } else {
                blockEntities.put(key, tag.clone());
            }
        }

        private static long blockKey(int x, int y, int z) {
            return ((long) x & 0x3ffffffL) << 38
                    | ((long) z & 0x3ffffffL) << 12
                    | (long) y & 0xfffL;
        }


        private static final class NetworkSection {
            private final int[][] layers;
            private final int airNetworkId;

            private NetworkSection(int[][] layers, int airNetworkId) {
                this.layers = layers;
                this.airNetworkId = airNetworkId;
            }

            private static NetworkSection empty(int airNetworkId, int layerCount) {
                int[][] layers = new int[Math.max(0, layerCount)][];
                for (int layer = 0; layer < layers.length; layer++) {
                    layers[layer] = new int[4096];
                    java.util.Arrays.fill(layers[layer], airNetworkId);
                }
                return new NetworkSection(layers, airNetworkId);
            }

            private int getFullBlock(int x, int y, int z, int layer) {
                if (layers == null || layer < 0 || layer >= layers.length) {
                    return airNetworkId;
                }
                final int[] blocks = layers[layer];
                if (blocks == null) {
                    return airNetworkId;
                }
                return blocks[(x << 8) | (z << 4) | y];
            }

            private void setFullBlock(int x, int y, int z, int layer, int networkId) {
                if (layers == null || layer < 0 || layer >= layers.length || layers[layer] == null) {
                    return;
                }
                layers[layer][(x << 8) | (z << 4) | y] = networkId;
            }
        }
    }
}
