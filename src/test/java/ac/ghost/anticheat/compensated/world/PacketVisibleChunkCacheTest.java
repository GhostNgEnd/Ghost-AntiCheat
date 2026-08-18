package ac.ghost.anticheat.compensated.world;

import cn.nukkit.level.Level;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PacketVisibleChunkCacheTest {

    @Test
    void overworldHeightChangesAtThe118ProtocolBoundary() {
        assertEquals(0, PacketVisibleChunkCache.minYForDimension(
                Level.DIMENSION_OVERWORLD, ProtocolInfo.v1_17_40));
        assertEquals(256, PacketVisibleChunkCache.maxYForDimension(
                Level.DIMENSION_OVERWORLD, ProtocolInfo.v1_17_40));

        assertEquals(-64, PacketVisibleChunkCache.minYForDimension(
                Level.DIMENSION_OVERWORLD, ProtocolInfo.v1_18_0));
        assertEquals(320, PacketVisibleChunkCache.maxYForDimension(
                Level.DIMENSION_OVERWORLD, ProtocolInfo.v1_18_0));
    }

    @Test
    void fixedHeightDimensionsDoNotShiftWithTheOverworld() {
        assertEquals(0, PacketVisibleChunkCache.minYForDimension(
                Level.DIMENSION_NETHER, ProtocolInfo.v1_17_40));
        assertEquals(128, PacketVisibleChunkCache.maxYForDimension(
                Level.DIMENSION_NETHER, ProtocolInfo.v1_26_40));
        assertEquals(0, PacketVisibleChunkCache.minYForDimension(
                Level.DIMENSION_THE_END, ProtocolInfo.v1_18_0));
        assertEquals(256, PacketVisibleChunkCache.maxYForDimension(
                Level.DIMENSION_THE_END, ProtocolInfo.v1_26_40));
    }
}
