package ac.ghost.anticheat.packets;

import cn.nukkit.network.protocol.ProtocolInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ServerNetworkHandlerTest {

    @Test
    void latencyAckCompletesTeleportForPreHandleTeleportProtocols() {
        assertFalse(ServerNetworkHandler.shouldResendTeleport(
                ProtocolInfo.v1_18_0, false, 4.0F, false));
    }

    @Test
    void modernClientMustAcknowledgeTeleportInAuthInput() {
        assertTrue(ServerNetworkHandler.shouldResendTeleport(
                ProtocolInfo.v1_19_60, false, 0.0F, false));
    }

    @Test
    void modernClientMustReachTeleportTarget() {
        assertTrue(ServerNetworkHandler.shouldResendTeleport(
                ProtocolInfo.v1_19_60, true, 0.01F, false));
        assertFalse(ServerNetworkHandler.shouldResendTeleport(
                ProtocolInfo.v1_19_60, true, 1.0E-4F, false));
    }

    @Test
    void laterQueuedTeleportSuppressesResend() {
        assertFalse(ServerNetworkHandler.shouldResendTeleport(
                ProtocolInfo.v1_21_50, false, 4.0F, true));
    }

    @Test
    void legacyLoadingCannotBeBypassedByTickEndMotion() {
        assertFalse(ServerNetworkHandler.shouldRunPrediction(
                ProtocolInfo.v1_18_30, true, 36, 0.0784F));
        assertFalse(ServerNetworkHandler.shouldRunPrediction(
                ProtocolInfo.v1_18_30, true, 36, 64.0F));
        assertFalse(ServerNetworkHandler.shouldRunPrediction(
                ProtocolInfo.v1_18_30, false, 1, 64.0F));
        assertFalse(ServerNetworkHandler.shouldTrackUnloadedChunk(
                ProtocolInfo.v1_18_30, true, 36));
        assertTrue(ServerNetworkHandler.shouldRunPrediction(
                ProtocolInfo.v1_18_30, false, 2, 0.0F));
        assertTrue(ServerNetworkHandler.shouldTrackUnloadedChunk(
                ProtocolInfo.v1_18_30, false, 2));
    }

    @Test
    void modernLoadingPredictionPathRemainsUnchanged() {
        assertTrue(ServerNetworkHandler.shouldRunPrediction(
                ProtocolInfo.v1_19_60, true, 36, 0.0784F));
        assertTrue(ServerNetworkHandler.shouldTrackUnloadedChunk(
                ProtocolInfo.v1_19_60, true, 36));
    }

    @Test
    void modernPredictionTruthTableMatchesOriginalExpression() {
        for (boolean loading : new boolean[]{false, true}) {
            for (int ticks : new int[]{0, 1, 2, 36}) {
                for (float tickEndSquared : new float[]{0.0F, 0.0784F, 64.0F}) {
                    final boolean original = (!loading && ticks >= 2)
                            || tickEndSquared > 0.0F;
                    assertEquals(original, ServerNetworkHandler.shouldRunPrediction(
                            ProtocolInfo.v1_21_50, loading, ticks,
                            tickEndSquared));
                    assertTrue(ServerNetworkHandler.shouldTrackUnloadedChunk(
                            ProtocolInfo.v1_21_50, loading, ticks));
                }
            }
        }
    }

    @Test
    void modernTeleportResendTruthTableMatchesOriginalExpression() {
        for (boolean handled : new boolean[]{false, true}) {
            for (boolean hardTeleporting : new boolean[]{false, true}) {
                for (float distance : new float[]{0.0F, 1.0E-4F, 0.01F}) {
                    final boolean original = (!handled || distance > 1.0E-3F)
                            && !hardTeleporting;
                    assertEquals(original, ServerNetworkHandler.shouldResendTeleport(
                            ProtocolInfo.v1_21_50, handled, distance,
                            hardTeleporting));
                }
            }
        }
    }

    @Test
    void positionAcknowledgementMatchesNukkitForceMovementTolerance() {
        assertTrue(ServerNetworkHandler.shouldAcceptPositionTeleport(0.0F));
        assertTrue(ServerNetworkHandler.shouldAcceptPositionTeleport(
                0.006F * 0.006F));
        assertTrue(ServerNetworkHandler.shouldAcceptPositionTeleport(0.1F));
        assertFalse(ServerNetworkHandler.shouldAcceptPositionTeleport(0.1001F));
        assertFalse(ServerNetworkHandler.shouldAcceptPositionTeleport(
                Float.NaN));
    }

    @Test
    void legacyFirstSpawnPositionIsNotAQueuedGameplayTeleport() {
        assertTrue(ServerNetworkHandler.isLegacyInitialPositionPacket(
                ProtocolInfo.v1_18_30, false));
        assertFalse(ServerNetworkHandler.isLegacyInitialPositionPacket(
                ProtocolInfo.v1_18_30, true));
        assertFalse(ServerNetworkHandler.isLegacyInitialPositionPacket(
                ProtocolInfo.v1_19_60, false));
    }

}
