package ac.ghost.anticheat.packets.server;

import cn.nukkit.network.protocol.LevelChunkPacket;
import cn.nukkit.network.protocol.PlayStatusPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ServerChunkPacketsTest {
    private static final int LEVEL_CHUNK_ID =
            ProtocolInfo.toNewProtocolID(LevelChunkPacket.NETWORK_ID);

    @Test
    void legacyInitializationUsesOnlyTheRawSpawnStatusBoundary() {
        final PlayStatusPacket spawn = new PlayStatusPacket();
        spawn.status = PlayStatusPacket.PLAYER_SPAWN;
        assertTrue(ServerChunkPackets.isLegacyInitializationPacket(
                spawn, ProtocolInfo.v1_5_0));

        final PlayStatusPacket login = new PlayStatusPacket();
        login.status = PlayStatusPacket.LOGIN_SUCCESS;
        assertFalse(ServerChunkPackets.isLegacyInitializationPacket(
                login, ProtocolInfo.v1_5_0));
        assertFalse(ServerChunkPackets.isLegacyInitializationPacket(
                spawn, ProtocolInfo.v1_6_0_5));
    }

    @Test
    void decodesModernVarIntLevelChunkEnvelope() {
        BinaryStream wire = new BinaryStream();
        wire.putUnsignedVarInt(LEVEL_CHUNK_ID);
        wire.putVarInt(-3);
        wire.putVarInt(9);
        wire.putUnsignedVarInt(2);
        wire.putBoolean(false);
        wire.putByteArray(new byte[]{8, 0, 8, 0});

        LevelChunkPacket packet = ServerChunkPackets.decodeLevelChunkEnvelope(
                wire.getBuffer(), ProtocolInfo.v1_18_30,
                LEVEL_CHUNK_ID, 2);

        assertNotNull(packet);
        assertEquals(-3, packet.chunkX);
        assertEquals(9, packet.chunkZ);
        assertEquals(2, packet.subChunkCount);
        assertEquals(2, packet.dimension);
        assertFalse(packet.cacheEnabled);
        assertArrayEquals(new byte[]{8, 0, 8, 0}, packet.data);
    }

    @Test
    void decodesPre112PayloadCountAndLegacySubClientHeader() {
        BinaryStream wire = new BinaryStream();
        wire.putByte(LevelChunkPacket.NETWORK_ID);
        wire.putShort(0);
        wire.putVarInt(7);
        wire.putVarInt(-11);
        wire.putByteArray(new byte[]{3, 0, 1, 2});

        LevelChunkPacket packet = ServerChunkPackets.decodeLevelChunkEnvelope(
                wire.getBuffer(), ProtocolInfo.v1_2_10,
                LevelChunkPacket.NETWORK_ID & 0xFF, 0);

        assertNotNull(packet);
        assertEquals(7, packet.chunkX);
        assertEquals(-11, packet.chunkZ);
        assertEquals(3, packet.subChunkCount);
        assertArrayEquals(new byte[]{3, 0, 1, 2}, packet.data);
    }

    @Test
    void decodesPre12VersionSpecificPacketIdWithoutSubClientHeader() {
        int legacyPacketId = 106;
        BinaryStream wire = new BinaryStream();
        wire.putByte(legacyPacketId);
        wire.putVarInt(-2);
        wire.putVarInt(4);
        wire.putByteArray(new byte[]{1, 0});

        LevelChunkPacket packet = ServerChunkPackets.decodeLevelChunkEnvelope(
                wire.getBuffer(), ProtocolInfo.v1_1_0,
                legacyPacketId, 0);

        assertNotNull(packet);
        assertEquals(-2, packet.chunkX);
        assertEquals(4, packet.chunkZ);
        assertEquals(1, packet.subChunkCount);
        assertNull(ServerChunkPackets.decodeLevelChunkEnvelope(
                wire.getBuffer(), ProtocolInfo.v1_1_0,
                legacyPacketId + 1, 0));
    }

    @Test
    void decodesLatestOptionalLevelChunkEnvelope() {
        BinaryStream wire = new BinaryStream();
        wire.putUnsignedVarInt(LEVEL_CHUNK_ID);
        wire.putVarInt(12);
        wire.putVarInt(13);
        wire.putVarInt(1);
        wire.putUnsignedVarInt(4);
        wire.putBoolean(true);
        wire.putVarInt(6);
        wire.putBoolean(true);
        wire.putUnsignedVarInt(1);
        wire.putLLong(0x1020304050607080L);
        wire.putByteArray(new byte[]{9, 0, 0});

        LevelChunkPacket packet = ServerChunkPackets.decodeLevelChunkEnvelope(
                wire.getBuffer(), ProtocolInfo.v1_26_40,
                LEVEL_CHUNK_ID, 0);

        assertNotNull(packet);
        assertEquals(1, packet.dimension);
        assertEquals(4, packet.subChunkCount);
        assertTrue(packet.requestSubChunks);
        assertEquals(6, packet.subChunkLimit);
        assertTrue(packet.cacheEnabled);
        assertArrayEquals(new long[]{0x1020304050607080L}, packet.blobIds);
        assertArrayEquals(new byte[]{9, 0, 0}, packet.data);
    }

    @Test
    void readsLegacyIdsAndBothMetadataNibbles() {
        byte[] section = new byte[4096 + 2048 + 1];
        section[0] = 5;
        section[1] = 6;
        section[4096] = (byte) 0xC3;
        section[section.length - 1] = 99;
        BinaryStream stream = new BinaryStream(section);

        int[][] layers = ServerChunkPackets.readLegacySection(
                stream, ProtocolInfo.v1_12_0, 0,
                (blockId, blockData) -> blockId * 100 + blockData);

        assertEquals(503, layers[0][0]);
        assertEquals(612, layers[0][1]);
        assertEquals(99, stream.getByte());
    }

    @Test
    void consumesPre12SkyAndBlockLightBeforeTheNextSection() {
        byte[] section = new byte[4096 + 2048 + 4096 + 1];
        Arrays.fill(section, 4096 + 2048,
                section.length - 1, (byte) 0x7F);
        section[section.length - 1] = 77;
        BinaryStream stream = new BinaryStream(section);

        ServerChunkPackets.readLegacySection(
                stream, ProtocolInfo.v1_1_0, 0,
                (blockId, blockData) -> blockId << 6 | blockData);

        assertEquals(77, stream.getByte());
        assertTrue(stream.feof());
    }
}
