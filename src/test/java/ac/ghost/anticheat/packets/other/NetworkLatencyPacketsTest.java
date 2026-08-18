package ac.ghost.anticheat.packets.other;

import cn.nukkit.network.protocol.ProtocolInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NetworkLatencyPacketsTest {

    @Test
    void onlyPreOnePointNineteenSixtyUsesOrderedResponses() {
        assertTrue(NetworkLatencyPackets.usesOrderedResponse(
                ProtocolInfo.v1_18_30));
        assertTrue(NetworkLatencyPackets.usesOrderedResponse(
                ProtocolInfo.v1_19_50));
        assertFalse(NetworkLatencyPackets.usesOrderedResponse(
                ProtocolInfo.v1_19_60));
        assertFalse(NetworkLatencyPackets.usesOrderedResponse(
                ProtocolInfo.v1_21_50));
    }

    @Test
    void highVersionsKeepOriginalTimestampNormalization() {
        final long id = 37L;
        assertEquals(id, NetworkLatencyPackets.resolveModernResponseId(
                id * NetworkLatencyPackets.LATENCY_MAGNITUDE,
                NetworkLatencyPackets.LATENCY_MAGNITUDE));
        assertEquals(id, NetworkLatencyPackets.resolveModernResponseId(
                id * NetworkLatencyPackets.PS5_LATENCY_MAGNITUDE,
                NetworkLatencyPackets.PS5_LATENCY_MAGNITUDE));
    }
}
