package ac.ghost.anticheat.util;

import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Vec3;
import cn.nukkit.math.Vector2f;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.ProtocolInfo;

public class InputUtil {
    






    public static Vector2f getPredictionMoveVector(final GhostPlayer player,
                                                    final PlayerAuthInputPacket packet) {
        if (player.getSession().protocol < ProtocolInfo.v1_21_50) {
            return null;
        }

        return packet.getRawMoveVector();
    }

    public static boolean hasPredictionMoveVector(final GhostPlayer player,
                                                  final PlayerAuthInputPacket packet) {
        return getPredictionMoveVector(player, packet) != null;
    }

    



    public static boolean hasInteractRotation(final GhostPlayer player,
                                              final PlayerAuthInputPacket packet) {
        return player.getSession().protocol >= ProtocolInfo.v1_21_40
                && packet.getInteractRotation() != null;
    }

    





    public static boolean processInput(final GhostPlayer player,
                                       final PlayerAuthInputPacket packet) {
        Vec3 input = Vec3.ZERO.clone();
        final Vector2f moveVector = getPredictionMoveVector(player, packet);

        if (moveVector == null) {
            player.entityContext.mobTravelComponent.setInput(input);
            return false;
        }

        
        if (player.entityContext.serverPlayerInventoryTransactionComponent.processing) {
            player.entityContext.mobTravelComponent.setInput(input);
            return true;
        }

        input = new Vec3(MathUtil.clamp(moveVector.getX(), -1, 1),
                0,
                MathUtil.clamp(moveVector.getY(), -1, 1));
        if (MathUtil.sign(input.x) == input.x && MathUtil.sign(input.z) == input.z && input.x != 0 && input.z != 0) {
            
            input = input.multiply(0.70710677F);
        } else {
            double length = input.horizontalLength();
            
            if (length >= 1) {
                input = new Vec3(input.x / length, 0, input.z / length);
            }
        }

        player.entityContext.mobTravelComponent.setInput(input);
        return true;
    }
}
