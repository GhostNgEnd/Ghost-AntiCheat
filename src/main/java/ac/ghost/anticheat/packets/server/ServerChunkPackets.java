package ac.ghost.anticheat.packets.server;

import ac.ghost.anticheat.Ghost;
import ac.ghost.anticheat.compensated.world.PacketVisibleChunkCache;
import ac.ghost.anticheat.data.block.NetworkBlockStateRegistry;
import ac.ghost.anticheat.data.block.NetworkBlockState;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.protocol.BedrockProtocolCapabilities;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.level.util.BitArray;
import cn.nukkit.level.util.BitArrayVersion;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.BlockEntityDataPacket;
import cn.nukkit.network.protocol.BlockEventPacket;
import cn.nukkit.network.protocol.ChunkRadiusUpdatedPacket;
import cn.nukkit.network.protocol.LevelChunkPacket;
import cn.nukkit.network.protocol.NetworkChunkPublisherUpdatePacket;
import cn.nukkit.network.protocol.PlayStatusPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.ServerboundLoadingScreenPacket;
import cn.nukkit.network.protocol.SetLocalPlayerAsInitializedPacket;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import cn.nukkit.network.protocol.UpdateSubChunkBlocksPacket;
import cn.nukkit.utils.BinaryStream;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.IntBinaryOperator;

public class ServerChunkPackets implements Listener {
    







    @EventHandler(priority = EventPriority.LOWEST)
    public void onPacket(final DataPacketReceiveEvent event) {
        final GhostPlayer player = Ghost.getInstance().getPlayerManager()
                .get(event.getPlayer());
        if (player == null || !player.entityContext.playerLoadingScreenComponent.active) {
            return;
        }

        








        if (event.getPacket() instanceof SetLocalPlayerAsInitializedPacket packet) {
            if (player.entityContext.playerLoadingScreenComponent.screenId != null) {
                return;
            }
            if (packet.eid != player.runtimeEntityId) {
                return;
            }

            if (BedrockProtocolCapabilities.hasServerboundLoadingScreen(
                    event.getPlayer().protocol)) {
                










                if (player.entityContext.playerLoadingScreenComponent
                        .initialFallbackDeadlineNs == 0L) {
                    player.entityContext.playerLoadingScreenComponent
                            .initialFallbackDeadlineNs = System.nanoTime()
                            + 2_000_000_000L;
                }
                return;
            }

            completeLoading(player);
            return;
        }

        




        if (!(event.getPacket() instanceof ServerboundLoadingScreenPacket packet)
                || packet.loadingScreenType
                != ServerboundLoadingScreenPacket.TYPE_STOP_LOADING_SCREEN) {
            return;
        }

        final Integer expectedScreenId =
                player.entityContext.playerLoadingScreenComponent.screenId;
        if (!Objects.equals(expectedScreenId, packet.loadingScreenId)) {
            return;
        }

        completeLoading(player);
    }

    private static void completeLoading(final GhostPlayer player) {
        player.entityContext.playerLoadingScreenComponent.screenId = null;
        player.entityContext.playerLoadingScreenComponent.active = false;
        player.entityContext.playerLoadingScreenComponent.ticksSinceChange = 0;
        player.entityContext.playerLoadingScreenComponent.initialFallbackDeadlineNs = 0L;
    }

    @EventHandler
    public void onPacket(final DataPacketSendEvent event) {
        final GhostPlayer player = Ghost.getInstance().getPlayerManager().get(event.getPlayer());
        if (player == null) {
            return;
        }
        final PacketVisibleChunkCache world = player.packetVisibleChunkCache;

        






        if (isLegacyInitializationPacket(event.getPacket(),
                event.getPlayer().protocol)
                && player.entityContext.playerLoadingScreenComponent.active
                && player.entityContext.playerLoadingScreenComponent.screenId == null) {
            completeLoading(player);
            return;
        }

        
        
        
        
        if (event.getPacket() instanceof BatchPacket packet) {
            for (LevelChunkPacket levelChunk : decodeLevelChunks(event.getPlayer(), packet)) {
                handleLevelChunk(player, world, levelChunk);
            }
        }

        if (event.getPacket() instanceof ChunkRadiusUpdatedPacket packet) {
            final int radius = packet.radius;
            player.latencyAdapter.sendLatencyStack(() -> world.setViewDistance(radius));
            return;
        }

        if (event.getPacket() instanceof NetworkChunkPublisherUpdatePacket) {
            
            
            
            final long[] usedChunkHashes = event.getPlayer().usedChunks.keySet().stream()
                    .mapToLong(Long::longValue)
                    .toArray();
            player.latencyAdapter.sendLatencyStack(() -> world.retainNukkitUsedChunks(usedChunkHashes));
            return;
        }

        if (event.getPacket() instanceof UpdateSubChunkBlocksPacket packet) {
            final List<BlockUpdate> updates = new ArrayList<>(
                    packet.standardBlocks.size() + packet.extraBlocks.size()
            );
            collectUpdates(player, packet.standardBlocks, 0, updates);
            collectUpdates(player, packet.extraBlocks, 1, updates);

            for (BlockUpdate update : updates) {
                tracePistonUpdateSent(player, world, update.x, update.y, update.z,
                        update.layer, update.networkId, "SUBCHUNK_UPDATE_SENT");
                traceLiquidUpdateSent(player, world, update.x, update.y, update.z,
                        update.layer, update.networkId, "SUBCHUNK_UPDATE_SENT");
            }

            if (Math.abs(player.entityContext.stateVectorComponent.getPosition().x - packet.position.getX()) <= 16
                    || Math.abs(player.entityContext.stateVectorComponent.getPosition().z - packet.position.getZ()) <= 16) {
                player.latencyAdapter.sendLatencyStack();
            }
            player.latencyAdapter.latencyUtil().queue(() -> {
                
                
                
                for (BlockUpdate update : updates) {
                    tracePistonUpdateApplied(player, world, update.x, update.y, update.z,
                            update.layer, update.networkId, "SUBCHUNK_UPDATE_APPLIED");
                    traceLiquidUpdateApplied(player, world, update.x, update.y, update.z,
                            update.layer, update.networkId, "SUBCHUNK_UPDATE_APPLIED");
                    world.updateBlock(update.x, update.y, update.z, update.layer, update.networkId);
                }
            });
            return;
        }

        if (event.getPacket() instanceof LevelChunkPacket packet) {
            handleLevelChunk(player, world, packet);
            return;
        }

        if (event.getPacket() instanceof UpdateBlockPacket packet) {
            final int x = packet.x;
            final int y = packet.y;
            final int z = packet.z;
            final int layer = packet.dataLayer;
            final int networkId = validNetworkId(player, packet.blockRuntimeId,
                    packet.blockId, packet.blockData);

            tracePistonUpdateSent(player, world, x, y, z, layer, networkId,
                    "BLOCK_UPDATE_SENT");
            traceLiquidUpdateSent(player, world, x, y, z, layer, networkId,
                    "BLOCK_UPDATE_SENT");

            if (layer == 0
                    && Ghost.getConfig().ignoreGhostBlock()
                    && !player.entityContext.playerLoadingScreenComponent.active
                    && player.entityContext.playerLoadingScreenComponent.ticksSinceChange >= 2) {
                final boolean newBlockIsAir = NetworkBlockStateRegistry.isAir(player, networkId);
                final boolean oldBlockIsAir = NetworkBlockStateRegistry.isAir(
                        player, world.getRawBlockAt(x, y, z, 0));
                if (newBlockIsAir && !oldBlockIsAir) {
                    final int distance = Math.abs(y - (int) Math.floor(
                            player.entityContext.stateVectorComponent.getPosition().y - 1));
                    if (distance <= 1) {
                        world.updateBlock(x, y, z, layer, networkId);
                    }
                }
            }

            if (player.entityContext.stateVectorComponent.getPosition().distanceTo(new Vec3(x, y, z)) <= 16) {
                player.latencyAdapter.sendLatencyStack();
            }
            player.latencyAdapter.latencyUtil().queue(() -> {
                tracePistonUpdateApplied(player, world, x, y, z, layer, networkId,
                        "BLOCK_UPDATE_APPLIED");
                traceLiquidUpdateApplied(player, world, x, y, z, layer, networkId,
                        "BLOCK_UPDATE_APPLIED");
                world.updateBlock(x, y, z, layer, networkId);
            });
            return;
        }

        if (event.getPacket() instanceof BlockEventPacket packet) {
            final int oldId = world.getRawBlockAt(packet.x, packet.y, packet.z, 0);
            final NetworkBlockState state = NetworkBlockStateRegistry.resolve(player, oldId);
            if (isPiston(state) && isNearPlayer(player, packet.x, packet.y, packet.z)) {
                markPistonTrace(player, packet.x, packet.y, packet.z, "BLOCK_EVENT_SENT");
            }
            return;
        }

        if (event.getPacket() instanceof BlockEntityDataPacket packet) {
            final CompoundTag tag = decodeBlockEntityTag(packet.namedTag);
            if (tag == null) {
                return;
            }
            final boolean pistonBlockEntity = isPistonBlockEntity(tag)
                    && isNearPlayer(player, packet.x, packet.y, packet.z);
            if (pistonBlockEntity) {
                markPistonTrace(player, packet.x, packet.y, packet.z,
                        "BLOCK_ENTITY_DATA_SENT");
            }
            player.latencyAdapter.sendLatencyStack();
            player.latencyAdapter.latencyUtil().queue(() -> {
                world.updateBlockEntityTag(packet.x, packet.y, packet.z, tag);
                if (pistonBlockEntity) {
                    markPistonTrace(player, packet.x, packet.y, packet.z,
                            "BLOCK_ENTITY_DATA_APPLIED");
                }
            });
        }
    }

    private static void handleLevelChunk(final GhostPlayer player,
                                         final PacketVisibleChunkCache world,
                                         final LevelChunkPacket packet) {
        final int subChunkCount = packet.subChunkCount;
        final int packetDimension = packet.protocol >= ProtocolInfo.v1_20_60
                ? packet.dimension : world.getDimension();
        if (subChunkCount <= -2 || packetDimension < 0 || packetDimension > 2) {
            return;
        }

        final int blockX = packet.chunkX << 4;
        final int blockZ = packet.chunkZ << 4;
        if (Math.abs(player.entityContext.stateVectorComponent.getPosition().x - blockX) <= 16
                || Math.abs(player.entityContext.stateVectorComponent.getPosition().z - blockZ) <= 16) {
            player.latencyAdapter.sendLatencyStack();
        }

        final PacketVisibleChunkCache.CachedChunk chunk = readNukkitLevelChunk(player, packet, packetDimension);
        player.latencyAdapter.latencyUtil().queue(() -> {
            
            
            
            if (packetDimension != world.getDimension()) {
                return;
            }
            world.put(packet.chunkX, packet.chunkZ, chunk);
        });
    }

    static boolean isLegacyInitializationPacket(final Object packet,
                                                final int protocol) {
        return packet instanceof PlayStatusPacket status
                && status.status == PlayStatusPacket.PLAYER_SPAWN
                && !BedrockProtocolCapabilities.hasLocalPlayerInitializedPacket(
                protocol);
    }

    private static List<LevelChunkPacket> decodeLevelChunks(final Player nukkitPlayer,
                                                             final BatchPacket packet) {
        final List<LevelChunkPacket> chunks = new ArrayList<>();
        if (packet.payload == null || packet.payload.length == 0) {
            return chunks;
        }

        try {
            final byte[] payload = nukkitPlayer.getNetworkSession()
                    .getCompression().decompress(packet.payload);
            final BinaryStream batch = new BinaryStream(payload);
            final int protocol = nukkitPlayer.protocol;
            final int levelChunkPacketId = expectedLevelChunkPacketId(
                    nukkitPlayer);

            while (!batch.feof()) {
                final long encodedLength = batch.getUnsignedVarInt();
                if (encodedLength <= 0 || encodedLength > Integer.MAX_VALUE) {
                    break;
                }

                final byte[] encodedPacket = batch.get((int) encodedLength);
                if (encodedPacket.length != (int) encodedLength) {
                    break;
                }

                final LevelChunkPacket levelChunk = decodeLevelChunkEnvelope(
                        encodedPacket, protocol, levelChunkPacketId,
                        nukkitPlayer.getLevel().getDimension());
                if (levelChunk != null) {
                    levelChunk.gameVersion = nukkitPlayer.getGameVersion();
                    chunks.add(levelChunk);
                }
            }
        } catch (Exception ignored) {
            
        }
        return chunks;
    }

    private static int expectedLevelChunkPacketId(final Player player) {
        if (player.protocol >= ProtocolInfo.v1_2_0) {
            return ProtocolInfo.toNewProtocolID(LevelChunkPacket.NETWORK_ID);
        }
        try {
            return player.getServer().getNetwork()
                    .getPacketPool(player.getGameVersion())
                    .getPacketId(LevelChunkPacket.class);
        } catch (RuntimeException ignored) {
            return ProtocolInfo.toNewProtocolID(LevelChunkPacket.NETWORK_ID);
        }
    }

    
    static LevelChunkPacket decodeLevelChunkEnvelope(
            final byte[] encodedPacket,
            final int protocol,
            final int expectedPacketId,
            final int fallbackDimension) {
        if (encodedPacket == null || encodedPacket.length == 0) {
            return null;
        }

        final BinaryStream stream = new BinaryStream(encodedPacket);
        final int packetId;
        if (protocol <= ProtocolInfo.v1_5_0) {
            packetId = stream.getByte() & 0xFF;
            if (protocol >= ProtocolInfo.v1_2_0) {
                if (!stream.isReadable(2)) {
                    return null;
                }
                stream.skip(2); 
            }
        } else {
            packetId = (int) stream.getUnsignedVarInt() & 0x3FF;
        }
        if (packetId != expectedPacketId) {
            return null;
        }

        final LevelChunkPacket levelChunk = new LevelChunkPacket();
        levelChunk.protocol = protocol;
        levelChunk.chunkX = stream.getVarInt();
        levelChunk.chunkZ = stream.getVarInt();
        levelChunk.dimension = protocol >= ProtocolInfo.v1_20_60
                ? stream.getVarInt() : fallbackDimension;

        if (protocol >= ProtocolInfo.v1_12_0) {
            levelChunk.subChunkCount = (int) stream.getUnsignedVarInt();
            if (protocol >= ProtocolInfo.v1_26_40) {
                levelChunk.requestSubChunks = stream.getBoolean();
                if (levelChunk.requestSubChunks) {
                    levelChunk.subChunkLimit = stream.getVarInt();
                }
                levelChunk.cacheEnabled = stream.getBoolean();
                readBlobIds(stream, levelChunk, true);
            } else {
                if (levelChunk.subChunkCount == -1) {
                    levelChunk.requestSubChunks = true;
                    levelChunk.subChunkLimit = -1;
                } else if (levelChunk.subChunkCount == -2) {
                    levelChunk.requestSubChunks = true;
                    levelChunk.subChunkLimit = stream.getLShort();
                }
                levelChunk.cacheEnabled = stream.getBoolean();
                readBlobIds(stream, levelChunk,
                        levelChunk.cacheEnabled);
            }
        }

        levelChunk.data = stream.getByteArray();
        if (protocol < ProtocolInfo.v1_12_0) {
            levelChunk.subChunkCount = levelChunk.data.length == 0
                    ? 0 : levelChunk.data[0] & 0xFF;
            levelChunk.cacheEnabled = false;
            levelChunk.blobIds = new long[0];
        }
        return levelChunk;
    }

    private static void readBlobIds(final BinaryStream stream,
                                    final LevelChunkPacket packet,
                                    final boolean present) {
        if (!present) {
            packet.blobIds = new long[0];
            return;
        }
        final int blobCount = (int) stream.getUnsignedVarInt();
        if (blobCount < 0 || blobCount > 4096
                || !stream.isReadable(blobCount * Long.BYTES)) {
            throw new IllegalArgumentException("Invalid LevelChunk blob count "
                    + blobCount);
        }
        packet.blobIds = new long[blobCount];
        for (int i = 0; i < blobCount; i++) {
            packet.blobIds[i] = stream.getLLong();
        }
    }

    private static CompoundTag decodeBlockEntityTag(byte[] namedTag) {
        if (namedTag == null || namedTag.length == 0) {
            return null;
        }
        try {
            return NBTIO.read(namedTag, ByteOrder.LITTLE_ENDIAN, true);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static PacketVisibleChunkCache.CachedChunk readNukkitLevelChunk(GhostPlayer player,
                                                                    LevelChunkPacket packet,
                                                                    int packetDimension) {
        final int protocol = packet.protocol;
        final int minY = PacketVisibleChunkCache.minYForDimension(
                packetDimension, protocol);
        final int maxY = PacketVisibleChunkCache.maxYForDimension(
                packetDimension, protocol);
        final int sectionCount = (maxY - minY) >> 4;
        final int air = NetworkBlockStateRegistry.airNetworkId(player);
        final PacketVisibleChunkCache.CachedChunk chunk = PacketVisibleChunkCache.CachedChunk.network(
                minY, maxY, sectionCount, air);

        if (packet.data == null || packet.data.length == 0 || packet.subChunkCount <= 0) {
            return chunk;
        }

        final BinaryStream stream = new BinaryStream(packet.data);
        try {
            int subChunkCount = packet.subChunkCount;
            if (protocol < ProtocolInfo.v1_12_0) {
                if (!stream.isReadable(1)) {
                    return chunk;
                }
                subChunkCount = stream.getByte() & 0xFF;
            }

            final IntBinaryOperator legacyNetworkId = protocol < ProtocolInfo.v1_13_0
                    ? legacyNetworkIdResolver(player) : null;
            for (int sectionY = 0; sectionY < subChunkCount && sectionY < sectionCount; sectionY++) {
                final int formatVersion = stream.getByte() & 0xFF;
                if (protocol < ProtocolInfo.v1_13_0) {
                    chunk.setNetworkSection(sectionY,
                            readLegacySection(stream, protocol, formatVersion,
                                    legacyNetworkId));
                    continue;
                }
                final int layerCount = stream.getByte() & 0xFF;
                
                
                
                if (formatVersion >= 9) {
                    stream.getByte();
                }

                final int[][] layers = new int[layerCount][];
                for (int layer = 0; layer < layerCount; layer++) {
                    layers[layer] = readNukkitLayer(player, stream, air);
                }
                chunk.setNetworkSection(sectionY, layers);
            }
        } catch (Exception ignored) {
            
        }
        return chunk;
    }

    private static IntBinaryOperator legacyNetworkIdResolver(
            final GhostPlayer player) {
        final int[] resolved = new int[256 * 16];
        Arrays.fill(resolved, Integer.MIN_VALUE);
        return (blockId, blockData) -> {
            final int index = (blockId & 0xFF) << 4
                    | blockData & 0x0F;
            int networkId = resolved[index];
            if (networkId != Integer.MIN_VALUE) {
                return networkId;
            }
            final int legacyFullId = (blockId & 0xFF) << Block.DATA_BITS
                    | blockData & Block.DATA_MASK;
            networkId = NetworkBlockStateRegistry.networkIdForLegacy(
                    player, legacyFullId);
            resolved[index] = networkId;
            return networkId;
        };
    }

    
    static int[][] readLegacySection(final BinaryStream stream,
                                     final int protocol,
                                     final int formatVersion,
                                     final IntBinaryOperator networkIdResolver) {
        if (formatVersion != 0) {
            throw new IllegalArgumentException(
                    "Unsupported legacy sub-chunk version " + formatVersion);
        }
        final int lightBytes = protocol < ProtocolInfo.v1_2_0
                ? 4096 : 0;
        if (!stream.isReadable(4096 + 2048 + lightBytes)) {
            throw new IllegalArgumentException("Truncated legacy sub-chunk");
        }

        final byte[] blockIds = stream.get(4096);
        final byte[] blockData = stream.get(2048);
        if (lightBytes != 0) {
            
            
            
            stream.skip(lightBytes);
        }

        final int[] blocks = new int[4096];
        for (int index = 0; index < blocks.length; index++) {
            final int packedData = blockData[index >> 1] & 0xFF;
            final int data = (index & 1) == 0
                    ? packedData & 0x0F : packedData >>> 4;
            blocks[index] = networkIdResolver.applyAsInt(
                    blockIds[index] & 0xFF, data);
        }
        return new int[][]{blocks};
    }

    private static int[] readNukkitLayer(GhostPlayer player, BinaryStream stream, int airNetworkId) {
        final int paletteHeader = stream.getByte() & 0xFF;
        final int versionId = paletteHeader >> 1;
        if (versionId == 127) {
            return null;
        }

        final BitArrayVersion version = BitArrayVersion.get(versionId, true);
        final BitArray bitArray;
        if (version == BitArrayVersion.V0) {
            bitArray = version.createPalette(4096);
        } else {
            final int[] words = new int[version.getWordsForSize(4096)];
            for (int i = 0; i < words.length; i++) {
                words[i] = stream.getLInt();
            }
            bitArray = version.createPalette(4096, words);
        }

        final int paletteSize = version == BitArrayVersion.V0 ? 1 : stream.getVarInt();
        final int[] palette = new int[Math.max(1, paletteSize)];
        for (int i = 0; i < paletteSize; i++) {
            
            
            
            final int networkId = stream.getVarInt();
            palette[i] = NetworkBlockStateRegistry.tryResolve(player, networkId) != null
                    ? networkId : airNetworkId;
        }
        if (paletteSize == 0) {
            palette[0] = airNetworkId;
        }

        final int[] blocks = new int[4096];
        for (int i = 0; i < blocks.length; i++) {
            final int paletteIndex = bitArray.get(i);
            blocks[i] = paletteIndex >= 0 && paletteIndex < palette.length
                    ? palette[paletteIndex] : airNetworkId;
        }
        return blocks;
    }

    private static void collectUpdates(
            GhostPlayer player,
            List<cn.nukkit.network.protocol.types.BlockChangeEntry> entries,
            int layer, List<BlockUpdate> output) {
        for (var entry : entries) {
            final int networkId = (int) entry.runtimeID();
            
            
            
            if (NetworkBlockStateRegistry.tryResolve(player, networkId) == null) {
                continue;
            }
            final var pos = entry.blockPos();
            output.add(new BlockUpdate(
                    pos.getX(), pos.getY(), pos.getZ(), layer, networkId
            ));
        }
    }

    private static int validNetworkId(GhostPlayer player, int networkId,
                                      int fallbackBlockId, int fallbackBlockData) {
        if (player.getSession().protocol < ProtocolInfo.v1_2_13) {
            final int legacy = fallbackBlockId << Block.DATA_BITS
                    | fallbackBlockData & Block.DATA_MASK;
            return NetworkBlockStateRegistry.networkIdForLegacy(player,
                    legacy);
        }
        if (NetworkBlockStateRegistry.tryResolve(player, networkId) != null) {
            return networkId;
        }

        final int legacy = fallbackBlockId << Block.DATA_BITS
                | fallbackBlockData & Block.DATA_MASK;
        return NetworkBlockStateRegistry.networkIdForLegacy(player, legacy);
    }

    private static void tracePistonUpdateSent(GhostPlayer player, PacketVisibleChunkCache world,
                                              int x, int y, int z, int layer, int newId,
                                              String event) {
        final NetworkBlockState oldState = NetworkBlockStateRegistry.resolve(
                player, world.getRawBlockAt(x, y, z, layer));
        final NetworkBlockState newState = NetworkBlockStateRegistry.resolve(player, newId);
        if ((!isPiston(oldState) && !isPiston(newState)) || !isNearPlayer(player, x, y, z)) {
            return;
        }
        markPistonTrace(player, x, y, z, event);
    }

    private static void tracePistonUpdateApplied(GhostPlayer player, PacketVisibleChunkCache world,
                                                 int x, int y, int z, int layer, int newId,
                                                 String event) {
        final NetworkBlockState oldState = NetworkBlockStateRegistry.resolve(
                player, world.getRawBlockAt(x, y, z, layer));
        final NetworkBlockState newState = NetworkBlockStateRegistry.resolve(player, newId);
        if ((!isPiston(oldState) && !isPiston(newState)) || !isNearPlayer(player, x, y, z)) {
            return;
        }
        markPistonTrace(player, x, y, z, event);
    }

    private static boolean isPiston(NetworkBlockState state) {
        return state != null && (state.identifierContains("piston")
                || state.is("minecraft:moving_block"));
    }

    private static boolean isPistonBlockEntity(CompoundTag tag) {
        final String id = tag == null ? "" : tag.getString("id");
        return "PistonArm".equalsIgnoreCase(id)
                || "MovingBlock".equalsIgnoreCase(id)
                || id.toLowerCase(java.util.Locale.ROOT).contains("piston");
    }

    private static boolean isNearPlayer(GhostPlayer player, int x, int y, int z) {
        return player.entityContext.stateVectorComponent.getPosition().distanceTo(new Vec3(x, y, z)) <= 8.0F;
    }

    private static void markPistonTrace(GhostPlayer player, int x, int y, int z, String event) {
        player.ghostDebugState.pistonTicks = Math.max(player.ghostDebugState.pistonTicks, 20);
        player.ghostDebugState.pistonPosition = new Vec3(x, y, z);
        player.ghostDebugState.pistonEvent = event;
    }

    private static String describeState(NetworkBlockState state) {
        if (state == null) {
            return "null";
        }
        return "{identifier=" + state.identifier()
                + ",networkId=" + state.networkId()
                + ",legacyId=" + state.blockId()
                + ",data=" + state.blockData()
                + ",states=" + state.stateTag() + '}';
    }

    private static void traceLiquidUpdateSent(GhostPlayer player, PacketVisibleChunkCache world,
                                              int x, int y, int z, int layer, int newId,
                                              String event) {
        if (!player.entityContext.actorDataFlagComponent.has(cn.nukkit.entity.Entity.DATA_FLAG_SWIMMING)) {
            return;
        }
        final NetworkBlockState oldState = NetworkBlockStateRegistry.resolve(
                player, world.getRawBlockAt(x, y, z, layer));
        final NetworkBlockState newState = NetworkBlockStateRegistry.resolve(player, newId);
        if ((!isLiquid(oldState) && !isLiquid(newState))
                || !isNearPlayer(player, x, y, z)) {
            return;
        }
        markLiquidTrace(player, x, y, z, event);
    }

    private static void traceLiquidUpdateApplied(GhostPlayer player, PacketVisibleChunkCache world,
                                                 int x, int y, int z, int layer, int newId,
                                                 String event) {
        if (!player.entityContext.actorDataFlagComponent.has(cn.nukkit.entity.Entity.DATA_FLAG_SWIMMING)) {
            return;
        }
        final NetworkBlockState oldState = NetworkBlockStateRegistry.resolve(
                player, world.getRawBlockAt(x, y, z, layer));
        final NetworkBlockState newState = NetworkBlockStateRegistry.resolve(player, newId);
        if ((!isLiquid(oldState) && !isLiquid(newState))
                || !isNearPlayer(player, x, y, z)) {
            return;
        }
        markLiquidTrace(player, x, y, z, event);
    }

    private static boolean isLiquid(NetworkBlockState state) {
        return state != null && (state.is("minecraft:water")
                || state.is("minecraft:flowing_water")
                || state.is("minecraft:lava")
                || state.is("minecraft:flowing_lava")
                || state.blockId() == cn.nukkit.block.BlockID.WATER
                || state.blockId() == cn.nukkit.block.BlockID.STILL_WATER
                || state.blockId() == cn.nukkit.block.BlockID.LAVA
                || state.blockId() == cn.nukkit.block.BlockID.STILL_LAVA);
    }

    private static boolean isFallingLiquid(NetworkBlockState state, int layer) {
        
        return layer == 0 && isLiquid(state) && state.blockData() >= 8;
    }

    private static String describeLiquidKind(NetworkBlockState state, int layer) {
        if (!isLiquid(state)) {
            return "NONE";
        }
        if (layer == 1) {
            return "WATERLOGGED";
        }
        if (state.blockData() >= 8) {
            return "DOWNWARD";
        }
        if (state.blockData() == 0) {
            return "SOURCE";
        }
        return "HORIZONTAL_FLOW";
    }

    private static void markLiquidTrace(GhostPlayer player, int x, int y, int z, String event) {
        player.ghostDebugState.liquidTicks = Math.max(player.ghostDebugState.liquidTicks, 20);
        player.ghostDebugState.liquidPosition = new Vec3(x, y, z);
        player.ghostDebugState.liquidEvent = event;
    }

    private record BlockUpdate(int x, int y, int z, int layer, int networkId) {
    }

}
