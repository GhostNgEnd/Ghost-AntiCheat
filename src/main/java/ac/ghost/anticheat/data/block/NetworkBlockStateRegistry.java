package ac.ghost.anticheat.data.block;

import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.GameVersion;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.BlockPalette;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.Hash;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;


public final class NetworkBlockStateRegistry {
    private static final Map<GameVersion, Registry> REGISTRIES = new ConcurrentHashMap<>();

    private NetworkBlockStateRegistry() {
    }

    public static NetworkBlockState resolve(GhostPlayer player, int networkId) {
        NetworkBlockState state = tryResolve(player, networkId);
        if (state == null) {
            GameVersion version = player.getSession().getGameVersion();
            throw new IllegalArgumentException("Unknown block network id " + networkId
                    + " for protocol " + version.getProtocol());
        }
        return state;
    }

    









    public static NetworkBlockState tryResolve(GhostPlayer player, int networkId) {
        GameVersion version = player.getSession().getGameVersion();
        if (version.getProtocol() < ProtocolInfo.v1_2_13) {
            return new NetworkBlockState(networkId, networkId, null);
        }
        Registry registry = REGISTRIES.computeIfAbsent(version,
                NetworkBlockStateRegistry::load);
        return registry.tryResolve(version, networkId);
    }

    public static int airNetworkId(GhostPlayer player) {
        GameVersion version = player.getSession().getGameVersion();
        if (version.getProtocol() < ProtocolInfo.v1_2_13) {
            return 0;
        }
        Registry registry = REGISTRIES.computeIfAbsent(version,
                NetworkBlockStateRegistry::load);
        return registry.airNetworkId(version);
    }

    public static int networkIdForLegacy(GhostPlayer player, int legacyFullId) {
        GameVersion version = player.getSession().getGameVersion();
        if (version.getProtocol() < ProtocolInfo.v1_2_13) {
            
            
            
            return legacyFullId;
        }
        int blockId = legacyFullId >> Block.DATA_BITS;
        int blockData = legacyFullId & Block.DATA_MASK;
        if (GlobalBlockPalette.shouldUseHashedBlockNetworkIds(version)) {
            return GlobalBlockPalette.getOrCreateHashId(version, blockId, blockData);
        }
        return GlobalBlockPalette.getOrCreateRuntimeId(version, blockId, blockData);
    }

    public static boolean isAir(GhostPlayer player, int networkId) {
        NetworkBlockState state = resolve(player, networkId);
        return state.is("minecraft:air") || state.is("minecraft:cave_air") || state.is("minecraft:void_air")
                || state.blockId() == BlockID.AIR;
    }

    private static Registry load(GameVersion version) {
        if (version.getProtocol() < ProtocolInfo.v1_13_0) {
            return loadLegacyRuntimeRegistry(version);
        }

        Map<Integer, NetworkBlockState> runtimeStates = new HashMap<>();
        Map<Integer, NetworkBlockState> hashStates = new HashMap<>();
        int airRuntime = Integer.MIN_VALUE;
        int airHash = Integer.MIN_VALUE;

        try {
            final int paletteProtocol;
            if (version.getProtocol() < ProtocolInfo.v1_14_0) {
                paletteProtocol = ProtocolInfo.v1_13_0;
            } else if (version.getProtocol() < ProtocolInfo.v1_16_0) {
                paletteProtocol = ProtocolInfo.v1_14_0;
            } else if (version.getProtocol() < ProtocolInfo.v1_16_100) {
                paletteProtocol = ProtocolInfo.v1_16_0;
            } else {
                BlockPalette palette = GlobalBlockPalette.getPaletteByProtocol(version);
                paletteProtocol = palette.getProtocol();
            }
            String resourceName = (version.isNetEase()
                    ? "runtime_block_states_netease_" : "runtime_block_states_")
                    + paletteProtocol + ".dat";
            InputStream selected = Server.class.getClassLoader()
                    .getResourceAsStream(resourceName);
            if (selected == null && version.isNetEase()) {
                
                
                resourceName = "runtime_block_states_" + paletteProtocol + ".dat";
                selected = Server.class.getClassLoader()
                        .getResourceAsStream(resourceName);
            }
            try (InputStream raw = selected) {
                if (raw != null) {
                    Object root = paletteProtocol == ProtocolInfo.v1_13_0
                            ? NBTIO.readNetwork(raw)
                            : NBTIO.readTag(new BufferedInputStream(
                                    new GZIPInputStream(raw)),
                                    ByteOrder.BIG_ENDIAN, false);
                    if (root instanceof ListTag<?> list) {
                        int runtimeIndex = 0;
                        for (Object object : list.getAll()) {
                            final int indexedRuntime = runtimeIndex++;
                            if (!(object instanceof CompoundTag source)) {
                                continue;
                            }
                            final int legacy;
                            final int runtime;
                            final CompoundTag networkStateTag;
                            if (paletteProtocol < ProtocolInfo.v1_16_0) {
                                
                                
                                
                                
                                if (!source.contains("meta")) {
                                    continue;
                                }
                                final int[] metadata = source.getIntArray("meta");
                                if (metadata.length == 0) {
                                    continue;
                                }
                                legacy = (source.getShort("id") & 0xFFFF)
                                        << Block.DATA_BITS
                                        | metadata[0] & Block.DATA_MASK;
                                runtime = indexedRuntime;
                                networkStateTag = source.getCompound("block").clone();
                            } else if (paletteProtocol < ProtocolInfo.v1_16_100) {
                                
                                
                                legacy = source.getInt("id") << Block.DATA_BITS
                                        | source.getShort("data") & Block.DATA_MASK;
                                runtime = indexedRuntime;
                                networkStateTag = source.getCompound("block").clone();
                            } else {
                                legacy = source.getInt("id") << Block.DATA_BITS
                                        | source.getShort("data") & Block.DATA_MASK;
                                runtime = source.getInt("runtimeId");
                                networkStateTag = source.clone()
                                        .remove("id")
                                        .remove("data")
                                        .remove("runtimeId")
                                        .remove("stateOverload");
                            }
                            int hash = Hash.hashBlock(networkStateTag);
                            NetworkBlockState runtimeState = new NetworkBlockState(runtime, legacy, networkStateTag);
                            runtimeStates.putIfAbsent(runtime, runtimeState);
                            hashStates.putIfAbsent(hash, new NetworkBlockState(hash, legacy, networkStateTag));
                            if ("minecraft:air".equals(networkStateTag.getString("name"))) {
                                airRuntime = runtime;
                                airHash = hash;
                            }
                        }
                    }
                }
            }
        } catch (IOException | RuntimeException | AssertionError ignored) {
            
        }

        return new Registry(Map.copyOf(runtimeStates), Map.copyOf(hashStates), airRuntime, airHash);
    }

    
    private static Registry loadLegacyRuntimeRegistry(GameVersion version) {
        Map<Integer, NetworkBlockState> runtimeStates = new HashMap<>();
        final int fallbackRuntime = GlobalBlockPalette.getOrCreateRuntimeId(
                version, BlockID.INFO_UPDATE, 0);

        for (int blockId = 0; blockId <= 0xFF; blockId++) {
            for (int blockData = 0; blockData <= 0x0F; blockData++) {
                final int runtimeId = GlobalBlockPalette.getOrCreateRuntimeId(
                        version, blockId, blockData);
                if (runtimeId == fallbackRuntime
                        && blockId != BlockID.INFO_UPDATE) {
                    continue;
                }
                final int legacy = blockId << Block.DATA_BITS | blockData;
                runtimeStates.putIfAbsent(runtimeId,
                        new NetworkBlockState(runtimeId, legacy, null));
            }
        }

        final int airRuntime = GlobalBlockPalette.getOrCreateRuntimeId(
                version, BlockID.AIR, 0);
        return new Registry(Map.copyOf(runtimeStates), Map.of(),
                airRuntime, Integer.MIN_VALUE);
    }

    private record Registry(Map<Integer, NetworkBlockState> runtimeStates,
                            Map<Integer, NetworkBlockState> hashStates,
                            int airRuntime,
                            int airHash) {
        NetworkBlockState tryResolve(GameVersion version, int networkId) {
            
            
            boolean hashed = version.getProtocol() >= ProtocolInfo.v1_19_80
                    && GlobalBlockPalette.shouldUseHashedBlockNetworkIds(version);
            NetworkBlockState state = (hashed ? hashStates : runtimeStates).get(networkId);
            if (state != null) {
                return state;
            }

            final int legacy;
            try {
                legacy = hashed
                        ? GlobalBlockPalette.getLegacyFullIdFromHashId(version, networkId)
                        : GlobalBlockPalette.getLegacyFullId(version, networkId);
            } catch (RuntimeException | AssertionError ignored) {
                return null;
            }
            return legacy < 0 ? null : new NetworkBlockState(networkId, legacy, null);
        }

        int airNetworkId(GameVersion version) {
            boolean hashed = version.getProtocol() >= ProtocolInfo.v1_19_80
                    && GlobalBlockPalette.shouldUseHashedBlockNetworkIds(version);
            int id = hashed ? airHash : airRuntime;
            if (id != Integer.MIN_VALUE) {
                return id;
            }
            return hashed
                    ? GlobalBlockPalette.getOrCreateHashId(version, BlockID.AIR, 0)
                    : GlobalBlockPalette.getOrCreateRuntimeId(version, BlockID.AIR, 0);
        }
    }
}
