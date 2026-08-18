package ac.ghost.anticheat.check.impl.breaking;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.annotations.Experimental;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.types.PlayerActionType;
import cn.nukkit.network.protocol.types.PlayerBlockActionData;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

@Experimental
@CheckInfo(name = "FarBreak")
public final class FarBreak extends PacketCheck {
    private static final float REACH_EPSILON = 0.15F;

    public FarBreak(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final DataPacketReceiveEvent event) {
        if (BreakingUtil.inVehicle(player)) {
            return;
        }
        final float maxReach = BreakingUtil.maxBlockReach(player);
        if (!Float.isFinite(maxReach)) {
            return;
        }

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
                if (!BreakingUtil.isBreakProgressAction(kind)
                        || data == null || data.getPosition() == null) {
                    continue;
                }
                if (checkDistance(data.getPosition(), maxReach, authInput)) {
                    BreakingUtil.removeAction(authInput, entry.getKey());
                }
            }
            return;
        }

        if (BreakingUtil.isLegacyBreakPacket(packet)) {
            final BreakingUtil.Kind kind = BreakingUtil.legacyKind(packet);
            if (!BreakingUtil.isBreakProgressAction(kind)) {
                return;
            }
            if (checkDistance(BreakingUtil.legacyPosition(packet), maxReach, null)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean checkDistance(final BlockVector3 position,
                                  final float maxReach,
                                  final PlayerAuthInputPacket packet) {
        final float distance = BreakingUtil.distanceToBlock(player, position, packet);
        if (distance <= maxReach + REACH_EPSILON) {
            return false;
        }
        fail(String.format(Locale.ROOT, "distance=%.3f, max=%.3f, pos=%s",
                distance, maxReach, BreakingUtil.pos(position)));
        return true;
    }
}
