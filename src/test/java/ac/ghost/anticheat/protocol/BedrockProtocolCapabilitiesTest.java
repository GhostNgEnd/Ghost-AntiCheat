package ac.ghost.anticheat.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BedrockProtocolCapabilitiesTest {
    @Test
    void networkLatencyAndLegacyPacketFamiliesChangeAtOnePointTwo() {
        assertFalse(BedrockProtocolCapabilities.hasNetworkStackLatency(
                ProtocolInfo.v1_1_0));
        assertTrue(BedrockProtocolCapabilities.usesLegacyEntityInteraction(
                ProtocolInfo.v1_1_0));
        assertTrue(BedrockProtocolCapabilities.usesLegacyUseItem(
                ProtocolInfo.v1_1_0));
        assertTrue(BedrockProtocolCapabilities.usesLegacyPlayerActionIds(
                ProtocolInfo.v1_1_0));

        assertTrue(BedrockProtocolCapabilities.hasNetworkStackLatency(
                ProtocolInfo.v1_2_0));
        assertFalse(BedrockProtocolCapabilities.usesLegacyEntityInteraction(
                ProtocolInfo.v1_2_0));
        assertFalse(BedrockProtocolCapabilities.usesLegacyUseItem(
                ProtocolInfo.v1_2_0));
        assertFalse(BedrockProtocolCapabilities.usesLegacyPlayerActionIds(
                ProtocolInfo.v1_2_0));
    }

    @Test
    void loadingHandshakeChangesAtOnePointSixBeta() {
        assertFalse(BedrockProtocolCapabilities.hasLocalPlayerInitializedPacket(
                ProtocolInfo.v1_6_0_5 - 1));
        assertTrue(BedrockProtocolCapabilities.hasLocalPlayerInitializedPacket(
                ProtocolInfo.v1_6_0_5));
    }

    @Test
    void correctionPacketAndSetbackTransportUseExactBoundaries() {
        assertFalse(BedrockProtocolCapabilities.hasCorrectPlayerMovePrediction(
                ProtocolInfo.v1_16_100_0 - 1));
        assertTrue(BedrockProtocolCapabilities.hasCorrectPlayerMovePrediction(
                ProtocolInfo.v1_16_100_0));
        assertTrue(BedrockProtocolCapabilities.hasCorrectPlayerMovePrediction(
                ProtocolInfo.v1_18_30));
        assertFalse(BedrockProtocolCapabilities
                .supportsCorrectPlayerMovePredictionSetback(
                        ProtocolInfo.v1_18_30));
        assertFalse(BedrockProtocolCapabilities
                .supportsCorrectPlayerMovePredictionSetback(
                        ProtocolInfo.v1_19_50));
        assertTrue(BedrockProtocolCapabilities
                .supportsCorrectPlayerMovePredictionSetback(
                        ProtocolInfo.v1_19_60));
    }

    @Test
    void movePlayerFrameStartsAtFinalOneSixteenOneHundred() {
        assertFalse(BedrockProtocolCapabilities.hasMovePlayerFrame(
                ProtocolInfo.v1_16_100 - 1));
        assertTrue(BedrockProtocolCapabilities.hasMovePlayerFrame(
                ProtocolInfo.v1_16_100));
    }

    @Test
    void teleportAcknowledgementTracksAllThreeMovementGenerations() {
        assertFalse(BedrockProtocolCapabilities.hasPlayerAuthInputMovement(
                ProtocolInfo.v1_16_100));
        assertFalse(BedrockProtocolCapabilities.usesPreHandleTeleportAuthInput(
                ProtocolInfo.v1_16_100));

        assertTrue(BedrockProtocolCapabilities.hasPlayerAuthInputMovement(
                ProtocolInfo.v1_17_0));
        assertTrue(BedrockProtocolCapabilities.usesPreHandleTeleportAuthInput(
                ProtocolInfo.v1_17_0));
        assertTrue(BedrockProtocolCapabilities.usesPreHandleTeleportAuthInput(
                ProtocolInfo.v1_19_50_20));
        assertFalse(BedrockProtocolCapabilities.hasHandleTeleportAuthInput(
                ProtocolInfo.v1_19_50_20));
        assertTrue(BedrockProtocolCapabilities.usesLegacyLoadingMovementGate(
                ProtocolInfo.v1_19_50_20));

        assertFalse(BedrockProtocolCapabilities.usesPreHandleTeleportAuthInput(
                ProtocolInfo.v1_19_60));
        assertTrue(BedrockProtocolCapabilities.hasHandleTeleportAuthInput(
                ProtocolInfo.v1_19_60));
        assertFalse(BedrockProtocolCapabilities.usesLegacyLoadingMovementGate(
                ProtocolInfo.v1_19_60));
    }

    @Test
    void movementEffectStartsAtOnePointTwentyOneForty() {
        assertFalse(BedrockProtocolCapabilities.hasMovementEffect(
                ProtocolInfo.v1_21_40 - 1));
        assertTrue(BedrockProtocolCapabilities.hasMovementEffect(
                ProtocolInfo.v1_21_40));
    }

    @Test
    void movementPredictionSyncTracksAllThreeWireRevisions() {
        assertFalse(BedrockProtocolCapabilities.hasMovementPredictionSync(
                ProtocolInfo.v1_21_60 - 1));
        assertTrue(BedrockProtocolCapabilities.hasMovementPredictionSync(
                ProtocolInfo.v1_21_60));

        assertFalse(BedrockProtocolCapabilities.movementPredictionSyncHasFlying(
                ProtocolInfo.v1_21_70_24));
        assertTrue(BedrockProtocolCapabilities.movementPredictionSyncHasFlying(
                ProtocolInfo.v1_21_70));

        assertFalse(BedrockProtocolCapabilities.movementPredictionSyncHasExtraScalars(
                ProtocolInfo.v1_26_20_26));
        assertTrue(BedrockProtocolCapabilities.movementPredictionSyncHasExtraScalars(
                ProtocolInfo.v1_26_20));
    }
}
