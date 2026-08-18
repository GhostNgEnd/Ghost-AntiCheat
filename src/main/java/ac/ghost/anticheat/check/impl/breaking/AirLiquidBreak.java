package ac.ghost.anticheat.check.impl.breaking;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.player.GhostPlayer;
import cn.nukkit.block.BlockID;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.types.GameType;
import cn.nukkit.network.protocol.types.PlayerActionType;
import cn.nukkit.network.protocol.types.PlayerBlockActionData;

import java.util.ArrayList;
import java.util.Map;

@CheckInfo(name = "AirLiquidBreak")
public final class AirLiquidBreak extends PacketCheck {
    public AirLiquidBreak(final GhostPlayer player) {
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
                
                
                
                
                if ((kind != BreakingUtil.Kind.START && kind != BreakingUtil.Kind.FINISH)
                        || data == null || data.getPosition() == null) {
                    continue;
                }
                if (isInvalid(data.getPosition(), kind)) {
                    fail("action=" + kind + ", pos=" + BreakingUtil.pos(data.getPosition()));
                    BreakingUtil.removeAction(authInput, entry.getKey());
                }
            }
            return;
        }

        if (BreakingUtil.isLegacyBreakPacket(packet)) {
            final BreakingUtil.Kind kind = BreakingUtil.legacyKind(packet);
            if (kind != BreakingUtil.Kind.START && kind != BreakingUtil.Kind.FINISH) {
                return;
            }
            final BlockVector3 position = BreakingUtil.legacyPosition(packet);
            if (isInvalid(position, kind)) {
                fail("action=" + kind + ", pos=" + BreakingUtil.pos(position));
                event.setCancelled(true);
            }
        }
    }

    private boolean isInvalid(final BlockVector3 position,
                              final BreakingUtil.Kind kind) {
        final BlockLegacy state = player.entityContext.blockSource
                .getBlockState(position, 0);
        if (state == null || state.getBlock() == null) {
            return false;
        }
        final int id = state.getBlock().getId();
        final boolean airOrLiquid = state.isAir()
                || id == BlockID.WATER
                || id == BlockID.STILL_WATER
                || id == BlockID.LAVA
                || id == BlockID.STILL_LAVA
                || state.getNetworkState().is("minecraft:bubble_column")
                || state.getNetworkState().is("minecraft:moving_block");
        if (airOrLiquid) {
            return true;
        }

        return kind == BreakingUtil.Kind.FINISH
                && state.getBlock().getHardness() == -1.0D
                && player.entityContext.actorGameTypeComponent.value != GameType.CREATIVE;
    }
}
