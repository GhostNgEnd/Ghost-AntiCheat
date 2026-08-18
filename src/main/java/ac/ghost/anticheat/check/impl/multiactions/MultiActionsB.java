package ac.ghost.anticheat.check.impl.multiactions;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.annotations.Experimental;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.types.AuthInputAction;
import cn.nukkit.network.protocol.types.PlayerActionType;
import cn.nukkit.network.protocol.types.PlayerBlockActionData;
import cn.nukkit.network.protocol.v113.RemoveBlockPacket_v113;

import java.util.LinkedHashMap;
import java.util.Map;


@Experimental
@CheckInfo(name = "MultiActionsB")
public final class MultiActionsB extends PacketCheck {
    public MultiActionsB(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final DataPacketReceiveEvent event) {
        if (!MultiActionUtil.isUsingSlowdownItem(player)) {
            return;
        }

        final Object packet = event.getPacket();
        if (packet instanceof RemoveBlockPacket_v113) {
            fail("action=FINISH_BREAK, item="
                    + MultiActionUtil.usedItemName(player));
            event.setCancelled(true);
            return;
        }
        if (packet instanceof PlayerActionPacket actionPacket) {
            if (!isLegacyBreakAction(actionPacket.action)) {
                return;
            }

            fail("action=" + legacyActionName(actionPacket.action)
                    + ", item=" + MultiActionUtil.usedItemName(player));
            event.setCancelled(true);
            return;
        }

        if (!(packet instanceof PlayerAuthInputPacket authInput)
                || !authInput.getInputData().contains(AuthInputAction.PERFORM_BLOCK_ACTIONS)
                || authInput.getBlockActionData() == null
                || authInput.getBlockActionData().isEmpty()) {
            return;
        }

        PlayerActionType firstBreakAction = null;
        for (final Map.Entry<PlayerActionType, PlayerBlockActionData> entry
                : authInput.getBlockActionData().entrySet()) {
            final PlayerActionType type = entry.getKey();
            if (isAuthInputBreakAction(type)) {
                firstBreakAction = type;
                break;
            }
        }
        if (firstBreakAction == null) {
            return;
        }

        fail("action=" + firstBreakAction
                + ", item=" + MultiActionUtil.usedItemName(player));

        
        
        
        final Map<PlayerActionType, PlayerBlockActionData> sanitized =
                new LinkedHashMap<>(authInput.getBlockActionData());
        sanitized.entrySet().removeIf(entry -> isAuthInputBreakAction(entry.getKey()));
        authInput.setBlockActionData(sanitized);
        if (sanitized.isEmpty()) {
            authInput.getInputData().remove(AuthInputAction.PERFORM_BLOCK_ACTIONS);
        }
    }

    private static boolean isLegacyBreakAction(final int action) {
        return action == PlayerActionPacket.ACTION_START_BREAK
                || action == PlayerActionPacket.ACTION_STOP_BREAK
                || action == PlayerActionPacket.ACTION_ABORT_BREAK;
    }

    private static String legacyActionName(final int action) {
        if (action == PlayerActionPacket.ACTION_START_BREAK) {
            return "START_BREAK";
        }
        if (action == PlayerActionPacket.ACTION_STOP_BREAK) {
            return "STOP_BREAK";
        }
        if (action == PlayerActionPacket.ACTION_ABORT_BREAK) {
            return "ABORT_BREAK";
        }
        return Integer.toString(action);
    }

    private static boolean isAuthInputBreakAction(final PlayerActionType action) {
        return action == PlayerActionType.START_DESTROY_BLOCK
                || action == PlayerActionType.ABORT_DESTROY_BLOCK
                || action == PlayerActionType.PREDICT_DESTROY_BLOCK
                || action == PlayerActionType.CONTINUE_DESTROY_BLOCK;
    }
}
