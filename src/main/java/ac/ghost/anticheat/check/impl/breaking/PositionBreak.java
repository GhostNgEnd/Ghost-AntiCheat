package ac.ghost.anticheat.check.impl.breaking;

import ac.ghost.anticheat.check.api.annotations.CheckInfo;
import ac.ghost.anticheat.check.api.annotations.Experimental;
import ac.ghost.anticheat.check.api.impl.PacketCheck;
import ac.ghost.anticheat.data.block.BlockLegacy;
import ac.ghost.anticheat.player.GhostPlayer;
import ac.ghost.anticheat.util.math.Box;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.types.InputMode;
import cn.nukkit.network.protocol.types.PlayerActionType;
import cn.nukkit.network.protocol.types.PlayerBlockActionData;

import java.util.ArrayList;
import java.util.Map;

@Experimental
@CheckInfo(name = "PositionBreak")
public final class PositionBreak extends PacketCheck {
    private static final float PLANE_EPSILON = 0.02F;

    public PositionBreak(final GhostPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final DataPacketReceiveEvent event) {
        if (BreakingUtil.inVehicle(player)) {
            return;
        }

        final Object packet = event.getPacket();
        if (packet instanceof PlayerAuthInputPacket authInput) {
            final InputMode mode = authInput.getInputMode() == null
                    ? InputMode.UNDEFINED : authInput.getInputMode();
            
            
            
            if (mode == InputMode.MOTION_CONTROLLER) {
                return;
            }
            if (authInput.getBlockActionData() == null
                    || authInput.getBlockActionData().isEmpty()) {
                return;
            }
            for (final Map.Entry<PlayerActionType, PlayerBlockActionData> entry
                    : new ArrayList<>(authInput.getBlockActionData().entrySet())) {
                final BreakingUtil.Kind kind = BreakingUtil.kind(entry.getKey());
                final PlayerBlockActionData data = entry.getValue();
                if (!BreakingUtil.isBreakProgressAction(kind)
                        || data == null || data.getPosition() == null
                        || data.getFacing() < 0 || data.getFacing() > 5) {
                    continue;
                }
                if (impossible(data.getPosition(), data.getFacing(), authInput)) {
                    fail("action=" + kind + ", face=" + data.getFacing()
                            + ", pos=" + BreakingUtil.pos(data.getPosition())
                            + ", input=" + mode);
                    BreakingUtil.removeAction(authInput, entry.getKey());
                }
            }
            return;
        }

        if (packet instanceof PlayerActionPacket actionPacket) {
            if (BreakingUtil.inputMode(player) == InputMode.MOTION_CONTROLLER) {
                return;
            }
            final BreakingUtil.Kind kind = BreakingUtil.legacyKind(actionPacket.action);
            if (!BreakingUtil.isBreakProgressAction(kind)
                    || actionPacket.face < 0 || actionPacket.face > 5) {
                return;
            }
            final BlockVector3 position = BreakingUtil.legacyPosition(actionPacket);
            if (impossible(position, actionPacket.face, null)) {
                fail("action=" + kind + ", face=" + actionPacket.face
                        + ", pos=" + BreakingUtil.pos(position));
                event.setCancelled(true);
            }
        }
    }

    private boolean impossible(final BlockVector3 position, final int face,
                               final PlayerAuthInputPacket packet) {
        final BlockLegacy state = player.entityContext.blockSource
                .getBlockState(position, 0);
        if (state == null || state.getBlock() == null || state.isAir()) {
            return false;
        }

        
        
        
        final Box block = state.getCollisionShape(player, position);
        if (block == null || !block.isValid()) {
            return false;
        }
        final Box eyes = BreakingUtil.possibleEyeBox(player, packet);
        if (eyes.intersects(block)) {
            return false;
        }

        return switch (face) {
            case 2 -> eyes.minZ > block.minZ + PLANE_EPSILON; 
            case 3 -> eyes.maxZ < block.maxZ - PLANE_EPSILON; 
            case 5 -> eyes.maxX < block.maxX - PLANE_EPSILON; 
            case 4 -> eyes.minX > block.minX + PLANE_EPSILON; 
            case 1 -> eyes.maxY < block.maxY - PLANE_EPSILON; 
            case 0 -> eyes.minY > block.minY + PLANE_EPSILON; 
            default -> false;
        };
    }
}
