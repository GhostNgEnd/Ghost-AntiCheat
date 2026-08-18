package ac.ghost.anticheat.check.impl.breaking;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.types.PlayerActionType;
import cn.nukkit.network.protocol.types.PlayerBlockActionData;

import java.util.ArrayList;
import java.util.Map;

@CheckInfo(name = "InvalidBreak")
public final class InvalidBreak extends PacketCheck {
    public InvalidBreak(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final DataPacketReceiveEvent event) {
        final Object packet = event.getPacket();
        if (packet instanceof PlayerAuthInputPacket authInput) {
            if (authInput.getBlockActionData() == null
                    || authInput.getBlockActionData().isEmpty()) {
                return;
            }
            for (final Map.Entry<PlayerActionType, PlayerBlockActionData> entry
                    : new ArrayList<>(authInput.getBlockActionData().entrySet())) {
                final BreakingUtil.Kind kind = BreakingUtil.kind(entry.getKey());
                final PlayerBlockActionData data = entry.getValue();
                
                
                if (kind == null || kind == BreakingUtil.Kind.CANCEL || data == null) {
                    continue;
                }
                final int face = data.getFacing();
                if (face < 0 || face > 5) {
                    fail("face=" + face + ", action=" + kind);
                    BreakingUtil.removeAction(authInput, entry.getKey());
                }
            }
            return;
        }

        if (packet instanceof PlayerActionPacket actionPacket) {
            final BreakingUtil.Kind kind = BreakingUtil.legacyKind(actionPacket.action);
            if (kind == null || kind == BreakingUtil.Kind.CANCEL) {
                return;
            }
            if (actionPacket.face < 0 || actionPacket.face > 5) {
                fail("face=" + actionPacket.face + ", action=" + kind);
                event.setCancelled(true);
            }
        }
    }
}
