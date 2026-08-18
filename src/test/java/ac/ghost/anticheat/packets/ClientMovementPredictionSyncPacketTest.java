package ac.ghost.anticheat.packets;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClientMovementPredictionSyncPacketTest {
    @Test
    void onePointTwentyOneSixtyLayoutEndsAtRuntimeId() {
        final ClientMovementPredictionSyncPacket packet = decode(
                ProtocolInfo.v1_21_60, false, false);

        assertEquals(1234L, packet.runtimeEntityId);
        assertFalse(packet.flying);
        assertTrue(packet.hasFlag(4));
    }

    @Test
    void onePointTwentyOneSeventyAddsFlying() {
        final ClientMovementPredictionSyncPacket packet = decode(
                ProtocolInfo.v1_21_70, true, false);

        assertEquals(1234L, packet.runtimeEntityId);
        assertTrue(packet.flying);
    }

    @Test
    void onePointTwentySixTwentyConsumesThreeScalarsBeforeRuntimeId() {
        final ClientMovementPredictionSyncPacket packet = decode(
                ProtocolInfo.v1_26_20, true, true);

        assertEquals(10.0F, packet.unknown1);
        assertEquals(11.0F, packet.unknown2);
        assertEquals(12.0F, packet.unknown3);
        assertEquals(1234L, packet.runtimeEntityId);
        assertTrue(packet.flying);
    }

    private static ClientMovementPredictionSyncPacket decode(
            final int protocol, final boolean flying,
            final boolean extraScalars) {
        final BinaryStream wire = new BinaryStream();
        wire.putUnsignedVarInt(1L << 4);
        for (int value = 1; value <= 9; value++) {
            wire.putLFloat(value);
        }
        if (extraScalars) {
            wire.putLFloat(10.0F);
            wire.putLFloat(11.0F);
            wire.putLFloat(12.0F);
        }
        wire.putUnsignedVarLong(1234L);
        if (protocol >= ProtocolInfo.v1_21_70) {
            wire.putBoolean(flying);
        }

        final ClientMovementPredictionSyncPacket packet =
                new ClientMovementPredictionSyncPacket();
        packet.protocol = protocol;
        packet.setBuffer(wire.getBuffer());
        packet.decode();
        return packet;
    }
}
