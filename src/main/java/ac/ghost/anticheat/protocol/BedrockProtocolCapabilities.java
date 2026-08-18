package ac.ghost.anticheat.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;










public final class BedrockProtocolCapabilities {
    private BedrockProtocolCapabilities() {
    }

    
    public static boolean hasNetworkStackLatency(final int protocol) {
        return protocol >= ProtocolInfo.v1_2_0;
    }

    
    public static boolean usesLegacyEntityInteraction(final int protocol) {
        return protocol < ProtocolInfo.v1_2_0;
    }

    
    public static boolean usesLegacyUseItem(final int protocol) {
        return protocol < ProtocolInfo.v1_2_0;
    }

    
    public static boolean usesLegacyPlayerActionIds(final int protocol) {
        return protocol < ProtocolInfo.v1_2_0;
    }

    



    public static boolean hasLocalPlayerInitializedPacket(final int protocol) {
        return protocol >= ProtocolInfo.v1_6_0_5;
    }

    




    public static boolean hasCorrectPlayerMovePrediction(final int protocol) {
        return protocol >= ProtocolInfo.v1_16_100_0;
    }

    



    public static boolean hasMovePlayerFrame(final int protocol) {
        return protocol >= ProtocolInfo.v1_16_100;
    }

    






    public static boolean supportsCorrectPlayerMovePredictionSetback(
            final int protocol) {
        return hasCorrectPlayerMovePrediction(protocol)
                && hasHandleTeleportAuthInput(protocol);
    }

    





    public static boolean hasPlayerAuthInputMovement(final int protocol) {
        return protocol >= ProtocolInfo.v1_17_0;
    }

    





    public static boolean usesPreHandleTeleportAuthInput(final int protocol) {
        return hasPlayerAuthInputMovement(protocol)
                && protocol < ProtocolInfo.v1_19_60;
    }

    




    public static boolean hasHandleTeleportAuthInput(final int protocol) {
        return protocol >= ProtocolInfo.v1_19_60;
    }

    




    public static boolean usesLegacyLoadingMovementGate(final int protocol) {
        return !hasHandleTeleportAuthInput(protocol);
    }

    




    public static boolean hasServerboundLoadingScreen(final int protocol) {
        return protocol >= ProtocolInfo.v1_21_20;
    }

    
    public static boolean hasMovementEffect(final int protocol) {
        return protocol >= ProtocolInfo.v1_21_40;
    }

    
    public static boolean hasMovementPredictionSync(final int protocol) {
        return protocol >= ProtocolInfo.v1_21_60;
    }

    
    public static boolean movementPredictionSyncHasFlying(final int protocol) {
        return protocol >= ProtocolInfo.v1_21_70;
    }

    
    public static boolean movementPredictionSyncHasExtraScalars(final int protocol) {
        return protocol >= ProtocolInfo.v1_26_20;
    }
}
