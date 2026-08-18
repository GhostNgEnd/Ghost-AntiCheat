package ac.ghost.anticheat.check.impl.multiactions;

import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.math.Vector2;
import cn.nukkit.math.Vector2f;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.ItemStackRequestPacket;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.PlayerInputPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.AuthInputAction;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequest;
import cn.nukkit.network.protocol.v113.ContainerSetSlotPacket_v113;

import java.util.Set;


final class InventoryInteractionUtil {
    private InventoryInteractionUtil() {
    }

    static boolean hasDirectionalInput(final GhostPlayer player,
                                       final PlayerAuthInputPacket packet) {
        final long tick = packet.getTick();
        if (tick < 0L || tick <= player.entityContext
                .serverPlayerMovementComponent.getLastReceivedInputTick()) {
            return false;
        }

        if (player.getSession().protocol >= ProtocolInfo.v1_21_50) {
            final Vector2f rawMoveVector = packet.getRawMoveVector();
            if (rawMoveVector != null && isNonZero(rawMoveVector)) {
                return true;
            }
        }

        
        final Vector2 moveVector = packet.getMotion();
        if (moveVector != null && isNonZero(moveVector)) {
            return true;
        }

        if (player.getSession().protocol >= ProtocolInfo.v1_19_70_24) {
            final Vector2f analogMoveVector = packet.getAnalogMoveVector();
            if (analogMoveVector != null && isNonZero(analogMoveVector)) {
                return true;
            }
        }

        return hasDiscreteDirection(packet.getInputData());
    }

    static boolean isItemInteraction(final Object packet) {
        if (packet instanceof ContainerSetSlotPacket_v113) {
            return true;
        }
        if (packet instanceof InventoryTransactionPacket transaction) {
            return transaction.transactionType == InventoryTransactionPacket.TYPE_NORMAL
                    && transaction.actions != null
                    && transaction.actions.length != 0;
        }

        if (packet instanceof PlayerAuthInputPacket authInput) {
            final ItemStackRequest request = authInput.getItemStackRequest();
            return request != null && request.getActions() != null
                    && request.getActions().length != 0;
        }

        if (!(packet instanceof ItemStackRequestPacket stackRequest)
                || stackRequest.getRequests() == null) {
            return false;
        }
        for (final ItemStackRequest request : stackRequest.getRequests()) {
            if (request != null && request.getActions() != null
                    && request.getActions().length != 0) {
                return true;
            }
        }
        return false;
    }

    static void removeEmbeddedItemInteraction(
            final PlayerAuthInputPacket packet) {
        if (packet == null || packet.getItemStackRequest() == null) {
            return;
        }
        packet.setItemStackRequest(null);
        packet.getInputData().remove(
                AuthInputAction.PERFORM_ITEM_STACK_REQUEST);
    }

    static boolean hasDirectionalInput(final PlayerInputPacket packet) {
        return packet != null
                && (packet.motionX != 0.0F || packet.motionY != 0.0F);
    }

    private static boolean isNonZero(final Vector2 vector) {
        return vector.getX() != 0.0D || vector.getY() != 0.0D;
    }

    private static boolean isNonZero(final Vector2f vector) {
        return vector.getX() != 0.0F || vector.getY() != 0.0F;
    }

    private static boolean hasDiscreteDirection(
            final Set<AuthInputAction> inputData) {
        return inputData != null && (inputData.contains(AuthInputAction.UP)
                || inputData.contains(AuthInputAction.DOWN)
                || inputData.contains(AuthInputAction.LEFT)
                || inputData.contains(AuthInputAction.RIGHT)
                || inputData.contains(AuthInputAction.UP_LEFT)
                || inputData.contains(AuthInputAction.UP_RIGHT)
                || inputData.contains(AuthInputAction.DOWN_LEFT)
                || inputData.contains(AuthInputAction.DOWN_RIGHT));
    }
}
