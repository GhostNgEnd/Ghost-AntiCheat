package ac.ghost.anticheat.check.impl.breaking;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.v113.RemoveBlockPacket_v113;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BreakingUtilTest {
    @Test
    void protocolOnePointOneRemoveBlockNormalizesToFinish() {
        final RemoveBlockPacket_v113 packet = new RemoveBlockPacket_v113();
        packet.x = 4;
        packet.y = 65;
        packet.z = -7;

        assertTrue(BreakingUtil.isLegacyBreakPacket(packet));
        assertEquals(BreakingUtil.Kind.FINISH,
                BreakingUtil.legacyKind(packet));
        assertEquals(new BlockVector3(4, 65, -7),
                BreakingUtil.legacyPosition(packet));
    }

    @Test
    void commonPlayerActionStillUsesItsOwnActionKind() {
        final PlayerActionPacket packet = new PlayerActionPacket();
        packet.action = PlayerActionPacket.ACTION_START_BREAK;
        packet.x = 1;
        packet.y = 2;
        packet.z = 3;

        assertTrue(BreakingUtil.isLegacyBreakPacket(packet));
        assertEquals(BreakingUtil.Kind.START,
                BreakingUtil.legacyKind(packet));
        assertEquals(new BlockVector3(1, 2, 3),
                BreakingUtil.legacyPosition(packet));
    }
}
